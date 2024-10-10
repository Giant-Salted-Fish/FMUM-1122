package com.fmum.gun;

import com.fmum.FMUM;
import com.fmum.gunpart.GunPartType;
import com.fmum.gunpart.IHandSetup;
import com.fmum.gunpart.IPreparedRenderer;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import com.fmum.item.IItemType;
import com.fmum.load.IContentBuildContext;
import com.fmum.load.IContentLoader;
import com.fmum.load.JsonData;
import com.fmum.mag.IMag;
import com.fmum.module.IModifyPreview;
import com.fmum.module.IModule;
import com.fmum.module.IModuleType;
import com.fmum.paintjob.IPaintableType;
import com.fmum.render.ModelPath;
import gsf.util.animation.IAnimator;
import gsf.util.lang.Error;
import gsf.util.lang.Result;
import gsf.util.lang.Success;
import gsf.util.math.Vec3f;
import gsf.util.render.IPose;
import gsf.util.render.Mesh;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GunType extends GunPartType
{
	public static final IContentLoader< GunType > LOADER = IContentLoader.of(
		GunType::new,
		IItemType.REGISTRY, IModuleType.REGISTRY, IPaintableType.REGISTRY
	);
	
	
	protected GunOpConfig op_take_out;
	protected GunOpConfig op_put_away;
	
	protected GunOpConfig op_load_mag;
	protected GunOpConfig op_unload_mag;
	
	@SideOnly( Side.CLIENT )
	protected GunOpConfig op_inspect;
	
	protected int left_hand_prio;
	protected int right_hand_prio;
	
	@SideOnly( Side.CLIENT )
	protected Vec3f left_shoulder_pos;
	
	@SideOnly( Side.CLIENT )
	protected Vec3f right_shoulder_pos;
	
	@SideOnly( Side.CLIENT )
	protected Vec3f left_grip_pos;
	
	@SideOnly( Side.CLIENT )
	protected Vec3f right_grip_pos;
	
	@SideOnly( Side.CLIENT )
	protected Mesh alex_arm;
	
	@SideOnly( Side.CLIENT )
	protected Mesh steve_arm;
	
	
	@Override
	public void reload( JsonData data, IContentBuildContext ctx )
	{
		super.reload( data, ctx );
		
		this.op_take_out = data.get( "op_take_out", GunOpConfig.class ).orElse( GunOpConfig.DEFAULT );
		this.op_put_away = data.get( "op_put_away", GunOpConfig.class ).orElse( GunOpConfig.DEFAULT );
		this.op_load_mag = data.get( "op_load_mag", GunOpConfig.class ).orElse( GunOpConfig.DEFAULT );
		this.op_unload_mag = data.get( "op_unload_mag", GunOpConfig.class ).orElse( GunOpConfig.DEFAULT );
		this.left_hand_prio = data.getInt( "left_hand_prio" ).orElse( 0 );
		this.right_hand_prio = data.getInt( "right_hand_prio" ).orElse( 0 );
		FMUM.SIDE.runIfClient( () -> {
			this.op_inspect = data.get( "op_inspect", GunOpConfig.class ).orElse( GunOpConfig.DEFAULT );
			this.left_shoulder_pos = data.get( "left_shoulder_pos", Vec3f.class ).orElseGet( () -> new Vec3f( 5.0F / 16.0F, -4.0F / 16.0F, 0.0F ) );
			this.right_shoulder_pos = data.get( "right_shoulder_pos", Vec3f.class ).orElseGet( () -> new Vec3f( -3.0F / 16.0F, -4.0F / 16.0F, -2.5F / 16.0F ) );
			this.left_grip_pos = data.get( "left_grip_pos", Vec3f.class ).orElse( Vec3f.ORIGIN );
			this.right_grip_pos = data.get( "right_grip_pos", Vec3f.class ).orElse( Vec3f.ORIGIN );
			ctx.regisMeshLoadCallback( c -> {
				this.alex_arm = c.loadMesh( ModelPath.ALEX_ARM ).orElse( Mesh.NONE );
				this.steve_arm = c.loadMesh( ModelPath.STEVE_ARM ).orElse( Mesh.NONE );
			} );
		} );
	}
	
	@Override
	protected GunPart _createRawModule()
	{
		final Optional< Short > opt = IModuleType.REGISTRY.lookupID( this );
		return new Gun( opt.orElseThrow( IllegalStateException::new ) );
	}
	
	@Override
	public IModule takeAndDeserialize( NBTTagCompound nbt ) {
		return new Gun( nbt );
	}
	
	@Override
	protected IEquippedItem _newEquipped( EnumHand hand, IItem item, EntityPlayer player )
	{
		final EquippedGun equipped = new EquippedGun();
		return (
			player.world.isRemote
			? new CEquippedTakeOut( equipped, item )
			: new SEquippedTakeOut( equipped, item )
		);
	}
	
	
	protected class Gun extends GunPart implements IGun
	{
		protected Gun( short id ) {
			super( id );
		}
		
		protected Gun( NBTTagCompound nbt ) {
			super( nbt );
		}
		
		@Override
		public Optional< ? extends IMag > getMag()
		{
			final boolean has_mag = this.countModuleInSlot( 0 ) > 0;
			return has_mag ? Optional.of( ( IMag ) this.getInstalled( 0, 0 ) ) : Optional.empty();
		}
		
		@Override
		public Result< Runnable, String > checkMagForLoad( IMag mag )
		{
			final IModifyPreview< Integer > preview = this.tryInstall( 0, mag );
			final Optional< String > error = preview.getPreviewError();
			return (
				error.isPresent()
				? new Error<>( error.get() )
				: new Success<>( preview::apply )
			);
		}
		
		@Override
		public IMag popMag() {
			return ( IMag ) this.tryRemove( 0, 0 ).apply();
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		protected void _enqueueRenderer(
			IPose pose,
			IAnimator animator,
			Consumer< IPreparedRenderer > render_queue,
			BiConsumer< Integer, IHandSetup > left_hand,
			BiConsumer< Integer, IHandSetup > right_hand
		) {
			super._enqueueRenderer( pose, animator, render_queue, left_hand, right_hand );
			
			left_hand.accept( GunType.this.left_hand_prio, IHandSetup.of( pose, GunType.this.left_grip_pos ) );
			right_hand.accept( GunType.this.right_hand_prio, IHandSetup.of( pose, GunType.this.right_grip_pos ) );
		}
	}
}
