package com.fmum.gun;

import com.fmum.FMUM;
import com.fmum.animation.AttrArmBlend;
import com.fmum.gunpart.EquippedGunPart;
import com.fmum.gunpart.IGunPart;
import com.fmum.gunpart.IHandSetup;
import com.fmum.gunpart.IPreparedRenderer;
import com.fmum.input.IInput;
import com.fmum.input.Inputs;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import com.mojang.realmsclient.util.Pair;
import gsf.util.animation.IAnimator;
import gsf.util.render.GLUtil;
import gsf.util.render.IPose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;

public class EquippedGun extends EquippedGunPart
{
	@SideOnly( Side.CLIENT )
	protected static Method EntityRenderer$getFOVModifier;
	@SideOnly( Side.CLIENT )
	protected static Field EntityRenderer$farPlaneDistance;
	
	@SideOnly( Side.CLIENT )
	protected static float ARM_LEN;
	
	static
	{
		FMUM.SIDE.runIfClient( () -> {
			//noinspection deprecation
			EntityRenderer$getFOVModifier = ReflectionHelper.findMethod(
				EntityRenderer.class,
				"getFOVModifier",
				"func_78481_a",
				float.class, boolean.class
			);
			//noinspection deprecation
			EntityRenderer$farPlaneDistance = ReflectionHelper.findField(
				EntityRenderer.class,
				"farPlaneDistance",
				"field_78530_s"
			);
			ARM_LEN = 10.0F / 16.0F;
		} );
	}
	
	
	@Override
	public Optional< IEquippedItem > tickPutAway( IItem item, EnumHand hand, EntityPlayer player )
	{
		return Optional.of(
			player.world.isRemote
			? new CEquippedPutAway( this, item )
			: new SEquippedPutAway( this, item )
		);
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void EquippedGunPart$doPrepareRenderInHand( IGunPart self, IAnimator animator )
	{
		final GunType type = ( GunType ) self.getType();
		
		// Clear previous in hand queue.
		this.in_hand_queue.clear();
		
		// Collect render callback.
		final ArrayList< IPreparedRenderer > renderers = new ArrayList<>();
		final int[] hand_priority = { Integer.MIN_VALUE, Integer.MIN_VALUE };
		final IHandSetup[] hand_setup = { null, null };
		final IPose pose = animator.getChannel( CHANNEL_ITEM );
		self.IGunPart$prepareRender(
			pose,
			animator,
			renderers::add,
			( prio, setup ) -> {
				if ( prio > hand_priority[ 0 ] )
				{
					hand_priority[ 0 ] = prio;
					hand_setup[ 0 ] = setup;
				}
			},
			( prio, setup ) -> {
				if ( prio > hand_priority[ 1 ] )
				{
					hand_priority[ 1 ] = prio;
					hand_setup[ 1 ] = setup;
				}
			}
		);
		
		final IPose left_grip = hand_setup[ 0 ].get( type.left_shoulder_pos, ARM_LEN, ARM_LEN );
		final IPose left_anim = IPose.compose( pose, animator.getChannel( CHANNEL_LEFT_ARM ) );
		final Float left_alpha = animator.getAttr( AttrArmBlend.LEFT_ARM ).orElse( 0.0F );
		final IPose left = IPose.blend( left_grip, left_anim, left_alpha );
		renderers.add( cam -> Pair.of( 0.0F, () -> {
			GL11.glPushMatrix();
			left.glApply();
			final EntityPlayerSP player = Minecraft.getMinecraft().player;
			GLUtil.bindTexture( player.getLocationSkin() );
			type.alex_arm.draw();
			GL11.glPopMatrix();
		} ) );
		
		final IPose right_grip = hand_setup[ 1 ].get( type.right_shoulder_pos, ARM_LEN, ARM_LEN );
		final IPose right_anim = IPose.compose( pose, animator.getChannel( CHANNEL_RIGHT_ARM ) );
		final Float right_alpha = animator.getAttr( AttrArmBlend.RIGHT_ARM ).orElse( 0.0F );
		final IPose right = IPose.blend( right_grip, right_anim, right_alpha );
		renderers.add( cam -> Pair.of( 0.0F, () -> {
			GL11.glPushMatrix();
			right.glApply();
			final EntityPlayerSP player = Minecraft.getMinecraft().player;
			GLUtil.bindTexture( player.getLocationSkin() );
			type.alex_arm.draw();
			GL11.glPopMatrix();
		} ) );
		
		renderers.stream()
			.map( pr -> pr.with( IPose.EMPTY ) )
			.sorted( Comparator.comparing( Pair::first ) )  // TODO: Reverse or not?
			.map( Pair::second )
			.forEachOrdered( this.in_hand_queue::add );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	protected void _doRenderInHand( IItem item, EnumHand hand )
	{
		final Minecraft mc = Minecraft.getMinecraft();
		final EntityRenderer renderer = mc.entityRenderer;
		final float smoother = mc.getRenderPartialTicks();
		final float fov_y;
		final float aspect = ( float ) mc.displayWidth / mc.displayHeight;
		final float z_near = 0.05F;
		final float z_far;
		try {
			fov_y = ( float ) EntityRenderer$getFOVModifier.invoke( renderer, smoother, true );
			z_far = ( float ) EntityRenderer$farPlaneDistance.get( renderer );
		}
		catch ( InvocationTargetException | IllegalAccessException e ) {
			throw new RuntimeException( e );
		}
		GL11.glMatrixMode( GL11.GL_PROJECTION );
		GL11.glLoadIdentity();
		Project.gluPerspective( fov_y, aspect, z_near, z_far );
		GL11.glMatrixMode( GL11.GL_MODELVIEW );
		
		super._doRenderInHand( item, hand );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public IEquippedItem onInputUpdate( IItem item, String name, IInput input )
	{
		if ( input.getAsBool() )
		{
			switch ( name )
			{
			case Inputs.LOAD_OR_UNLOAD_MAG:
				return (
					IGun.from( item ).getMag().isPresent()
					? new CEquippedUnloadMag( this, item )
					: new CEquippedLoadMag( this, item )
				);
			case Inputs.INSPECT_WEAPON:
				return new CEquippedInspect( this, item );
			}
		}
		return super.onInputUpdate( item, name, input );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean getViewBobbing( IItem item, boolean original ) {
		return false;
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public boolean shouldDisableCrosshair( IItem item ) {
		return true;
	}
}
