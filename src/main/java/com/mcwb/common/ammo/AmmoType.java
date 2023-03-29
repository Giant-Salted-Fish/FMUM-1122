package com.mcwb.common.ammo;

import javax.annotation.Nullable;

import com.mcwb.client.ammo.IAmmoModel;
import com.mcwb.client.item.IEquippedItemRenderer;
import com.mcwb.client.item.IItemRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.MCWB;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.item.IItem;
import com.mcwb.common.item.ItemType;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.load.IContentProvider;
import com.mcwb.common.meta.IMeta;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class AmmoType<
	C extends IItem,
	M extends IAmmoModel<
		? super IAmmoType,
		? extends IItemRenderer<
			? super IItem,
			? extends IEquippedItemRenderer< ? super IEquippedItem< ? > >
		>
	>
> extends ItemType< C, M > implements IAmmoType
{
	public static final BuildableLoader< IMeta >
		LOADER = new BuildableLoader<>( "ammo", JsonAmmoType.class );
	
	protected String category;
	
	protected boolean isCase = false;
	
	protected int maxStackSize = 60;
	
	@Override
	public IMeta build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		IAmmoType.REGISTRY.regis( this );
		
		if ( this.category == null )
			this.category = this.name;
		return this;
	}
	
	@Override
	public String category() { return this.category; }
	
	@Override
	public boolean isCase() { return this.isCase; }
	
	@Override
	@SideOnly( Side.CLIENT )
	public void render() { this.model.render( this ); }
	
	@Override
	@SideOnly( Side.CLIENT )
	public ResourceLocation texture() { return this.texture; }
	
	@Override
	protected IMeta loader() { return LOADER; }
	
	@Override
	protected Item createItem()
	{
		return this.new VanillaItem( this.maxStackSize, 0 )
		{
			@Override
			public ICapabilityProvider initCapabilities(
				ItemStack stack,
				@Nullable NBTTagCompound capTag
			) { return AmmoType.this.new Ammo(); }
		};
	}
	
	protected class Ammo implements IItem, ICapabilityProvider
	{
		@Override
		public boolean hasCapability( Capability< ? > capability, @Nullable EnumFacing facing ) {
			return capability == IMeta.CONTEXTED;
		}
		
		@Override
		public < T > T getCapability( Capability< T > capability, @Nullable EnumFacing facing ) {
			return IMeta.CONTEXTED.cast( this );
		}
		
		@Override
		public int stackId() { return this.hashCode(); }
		
		@Override
		public IEquippedItem< ? > onTakeOut( EntityPlayer player, EnumHand hand ) {
			return this.newEquipped( hand );
		}
		
		@Override
		public IEquippedItem< ? > onStackUpdate(
			IEquippedItem< ? > prevEquipped,
			EntityPlayer player,
			EnumHand hand
		) { return this.newEquipped( hand ); }
		
		@Override
		@SideOnly( Side.CLIENT )
		public ResourceLocation texture() { return AmmoType.this.texture; }
		
		final IEquippedItem< ? > newEquipped( EnumHand hand )
		{
			return new IEquippedItem< IItem >()
			{
				IEquippedItemRenderer< ? super IEquippedItem< ? > > renderer;
				{
					// TODO: this will create instance on local server
					MCWB.MOD.clientOnly(
						() -> this.renderer = AmmoType.this.model.newRenderer().onTakeOut( hand )
					);
				}
				
				@Override
				public IItem item() { return Ammo.this; }
				
				@Override
				@SideOnly( Side.CLIENT )
				public boolean renderInHandSP( EnumHand hand ) {
					return this.renderer.renderInHandSP( this, hand );
				}
				
				@Override
				@SideOnly( Side.CLIENT )
				public boolean onRenderSpecificHandSP( EnumHand hand ) {
					return this.renderer.onRenderSpecificHandSP( this, hand );
				}
				
				@Override
				@SideOnly( Side.CLIENT )
				public IAnimator animator() { return this.renderer.animator(); }
			};
		}
	}
}
