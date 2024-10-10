package com.fmum.attachment;

import com.fmum.FMUM;
import com.fmum.gunpart.GunPartType;
import com.fmum.gunpart.IHandSetup;
import com.fmum.gunpart.IPreparedRenderer;
import com.fmum.item.IItemType;
import com.fmum.load.IContentBuildContext;
import com.fmum.load.IContentLoader;
import com.fmum.load.JsonData;
import com.fmum.module.IModule;
import com.fmum.module.IModuleType;
import com.fmum.paintjob.IPaintableType;
import gsf.util.animation.IAnimator;
import gsf.util.math.Vec3f;
import gsf.util.render.IPose;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GripType extends GunPartType
{
	public static final IContentLoader< GripType > LOADER = IContentLoader.of(
		GripType::new,
		IItemType.REGISTRY, IModuleType.REGISTRY, IPaintableType.REGISTRY
	);
	
	
	protected int left_hand_prio;
	protected int right_hand_prio;
	
	@SideOnly( Side.CLIENT )
	protected Vec3f left_grip_pos;
	
	@SideOnly( Side.CLIENT )
	protected Vec3f right_grip_pos;
	
	
	@Override
	public void reload( JsonData data, IContentBuildContext ctx )
	{
		super.reload( data, ctx );
		
		this.left_hand_prio = data.getInt( "left_hand_prio" ).orElse( Integer.MIN_VALUE );
		this.right_hand_prio = data.getInt( "right_hand_prio" ).orElse( Integer.MIN_VALUE );
		FMUM.SIDE.runIfClient( () -> {
			this.left_grip_pos = data.get( "left_grip_pos", Vec3f.class ).orElse( Vec3f.ORIGIN );
			this.right_grip_pos = data.get( "right_grip_pos", Vec3f.class ).orElse( Vec3f.ORIGIN );
		} );
	}
	
	@Override
	protected GunPart _createRawModule()
	{
		final Optional< Short > opt = IModuleType.REGISTRY.lookupID( this );
		return new Grip( opt.orElseThrow( IllegalStateException::new ) );
	}
	
	@Override
	public IModule takeAndDeserialize( NBTTagCompound nbt ) {
		return new Grip( nbt );
	}
	
	
	protected class Grip extends GunPart
	{
		protected Grip( short id ) {
			super( id );
		}
		
		protected Grip( NBTTagCompound nbt ) {
			super( nbt );
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
			
			left_hand.accept( GripType.this.left_hand_prio, IHandSetup.of( pose, GripType.this.left_grip_pos ) );
			right_hand.accept( GripType.this.right_hand_prio, IHandSetup.of( pose, GripType.this.right_grip_pos ) );
		}
	}
}
