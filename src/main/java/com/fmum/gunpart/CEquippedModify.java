package com.fmum.gunpart;

import com.fmum.input.IInput;
import com.fmum.input.InputManager;
import com.fmum.input.Inputs;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import com.fmum.module.IModule;
import com.fmum.module.ModifySession;
import com.mojang.realmsclient.util.Pair;
import gsf.util.animation.IAnimator;
import gsf.util.math.Mat4f;
import gsf.util.math.Vec3f;
import gsf.util.render.IPose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;

@SideOnly( Side.CLIENT )
public class CEquippedModify extends CEquippedWrapRender
{
	protected final ModifySession session;
	
	protected IItem item;
	
	protected CEquippedModify( EquippedGunPart wrapped, IItem item )
	{
		super( wrapped );
		
		this.session = new GunPartModifySession( this._newFactory( item ) );
		this.item = item;
	}
	
	protected Supplier< ? extends IModule > _newFactory( IItem item )
	{
		final IGunPart self = IGunPart.from( item );
		final NBTTagCompound nbt = self.getBoundNBT();
		return () -> IModule.takeAndDeserialize( nbt.copy() );
	}
	
	@Override
	public IEquippedItem tickInHand( IItem held_item, EnumHand hand, EntityPlayer player )
	{
		if ( held_item == this.item ) {
			return this;
		}
		
		final boolean has_conflict = this.session.refresh( this._newFactory( held_item ), i -> {
			final InventoryPlayer inv = Minecraft.getMinecraft().player.inventory;
			return (
				IItem.ofOrEmpty( inv.getStackInSlot( i ) )
				.flatMap( it -> it.lookupCapability( IModule.CAPABILITY ) )
				// Copy to avoid side effect.
				.map( IModule::getBoundNBT )
				.map( NBTTagCompound::copy )
				.map( IModule::takeAndDeserialize )
			);
		} );
		if ( has_conflict ) {
			return this.wrapped;  // Terminate session if conflict occurs.
		}
		
		this.item = held_item;
		return this;
	}
	
	@Override
	protected IGunPart _getRenderDelegate( EnumHand hand, IItem item ) {
		return ( IGunPart ) this.session.getRoot();
	}
	
	@Override
	protected IAnimator _getInHandAnimator( EnumHand hand, IItem item )
	{
		// TODO: blend with original animator.
		final EquippedGunPart eq = ( EquippedGunPart ) this.wrapped;
		final IAnimator wrapped = eq.EquippedGunPart$getInHandAnimator( hand, item );
		
		final EntityPlayerSP player = Minecraft.getMinecraft().player;
		final GunPartType type = ( GunPartType ) item.getType();
		final Vec3f modify_pos = type.modify_pos;
		final Mat4f mat = new Mat4f();
		mat.setIdentity();
		mat.translate( 0.0F, 0.0F, modify_pos.z );
		mat.rotateX( -player.rotationPitch );
		mat.rotateY( 90.0F + player.rotationYaw );
		mat.translate( modify_pos.x, modify_pos.y, 0.0F );
		final IPose in_hand_setup = IPose.ofMat( mat );
		return channel -> channel.equals( CHANNEL_ITEM ) ? in_hand_setup : IPose.EMPTY;
	}
	
	@Override
	public IEquippedItem onInputUpdate( IItem item, String name, IInput input )
	{
		final boolean is_activation = input.getAsBool();
		if ( !is_activation ) {
			return this;
		}
		
		switch ( name )
		{
		case Inputs.OPEN_MODIFY_VIEW:
			return this.wrapped;
		
		case Inputs.NEXT_MODIFY_MODE:
			this.session.loopModifyMode();
			break;
		case Inputs.ENTER_LAYER:
			this.session.enterLayer();
			break;
		case Inputs.QUIT_LAYER:
			this.session.quitLayer( this._newFactory( item ) );
			break;
		case Inputs.LAST_SLOT:
		case Inputs.NEXT_SLOT:
			this.session.loopSlot( name.equals( Inputs.NEXT_SLOT ) );
			break;
		case Inputs.LAST_MODULE:
		case Inputs.NEXT_MODULE:
			this.session.loopModule( name.equals( Inputs.NEXT_MODULE ) );
			break;
		case Inputs.LAST_PREVIEW:
		case Inputs.NEXT_PREVIEW:
			this.session.loopPreview( prev_idx -> {
				final InventoryPlayer inv = Minecraft.getMinecraft().player.inventory;
				final int size = inv.getSizeInventory();
				final IntStream inv_indices = (
					name.equals( Inputs.NEXT_PREVIEW )
					? IntStream.range( prev_idx + 1, size )
					: IntStream.range( size - ( prev_idx + size + 1 ) % ( size + 1 ), size )
						.map( i -> size - i - 1 )
				);
				return (
					inv_indices
					.mapToObj( i -> (
						IItem.ofOrEmpty( inv.getStackInSlot( i ) )
						.filter( it -> !it.equals( item ) )
						.flatMap( it -> it.lookupCapability( IModule.CAPABILITY ) )
						// Copy to avoid side effect.
						.map( IModule::getBoundNBT )
						.map( NBTTagCompound::copy )
						.map( IModule::takeAndDeserialize )
						.map( mod -> Pair.of( i, mod ) )
					) )
					.filter( Optional::isPresent )
					.map( Optional::get )
					.iterator()
				);
			} );
			break;
		case Inputs.LAST_CHANGE:
		case Inputs.NEXT_CHANGE:
			this.session.loopChange( name.equals( Inputs.NEXT_CHANGE ) );
			break;
		case Inputs.CONFIRM_CHANGE:
			this.session.confirmChange();
			break;
		case Inputs.REMOVE_MODULE:
			this.session.removeModule();
			break;
		}
		return this;
	}
	
	@Override
	public boolean shouldDisableCrosshair( IItem item ) {
		return !InputManager.getBoolState( Inputs.FREE_VIEW );
	}
}
