package com.fmum.gun;

import com.fmum.FMUM;
import com.fmum.gunpart.EquippedGunPart;
import com.fmum.input.IInput;
import com.fmum.input.Inputs;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import net.minecraft.client.Minecraft;
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
import java.util.Optional;

public class EquippedGun extends EquippedGunPart
{
	@SideOnly( Side.CLIENT )
	protected static Method EntityRenderer$getFOVModifier;
	@SideOnly( Side.CLIENT )
	protected static Field EntityRenderer$farPlaneDistance;
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
