package gsf.devtool;

import com.fmum.input.Inputs;
import com.fmum.player.ChatBoxUtil;
import com.fmum.FMUM;
import com.fmum.render.ModelPath;
import com.fmum.render.Texture;
import com.google.common.collect.Lists;
import com.kbp.client.KBPMod;
import gsf.util.animation.SpringLikeRotation;
import gsf.util.math.Mat4f;
import gsf.util.math.MoreMath;
import gsf.util.math.Quat4f;
import gsf.util.math.Vec3f;
import gsf.util.render.GLUtil;
import gsf.util.render.Mesh;
import gsf.util.render.MeshBuilder;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Matrix3f;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * A helper class for test and debug in dev phase. Should not be shipped in
 * production release.
 */
public class Dev
{
	public static int DEV_MARK = 0;
	
	public static final Consumer< Boolean > DEBUG_BOX = new Consumer< Boolean >() {
		private Mesh mesh;
		
		private final Texture texture = new Texture( FMUM.MODID, "textures/debug_box.png" );
		
		@Override
		public void accept( Boolean use_default_texture )
		{
			if ( this.mesh == null )
			{
				final ModelPath mesh_path = new ModelPath( FMUM.MODID, "models/debug_box.obj" );
				this.mesh = MeshBuilder.fromObjModel( mesh_path ).unwrap().build();
			}
			
			if ( use_default_texture ) {
				GLUtil.bindTexture( this.texture );
			}
			this.mesh.draw();
		}
	};
	
	public static TestPosRot cur() {
		return test_list.get( test_ins_idx );
	}
	
	public static TestPosRot get( int idx ) {
		return test_list.get( idx );
	}
	
	public static void toggleFlagPrint( String... msg ) {
		toggleFlagDo( () -> ChatBoxUtil.addMessage( msg ) );
	}
	
	public static void toggleFlagDo( Runnable task )
	{
		if ( flag )
		{
			flag = false;
			task.run();
		}
	}
	
	private static final SpringLikeRotation rot = new SpringLikeRotation();
	private static final Matrix3f inertia = new Matrix3f(
		0.025F, 0.0F, 0.0F,
		0.0F, 0.025F, 0.0F,
		0.0F, 0.0F, 0.025F
	);
	private static float radian_clamp = MoreMath.PI / 8.0F;
	private static float damping = 0.95F;
	
	private static void scaleMat( Matrix3f mat, float scale )
	{
		mat.m00 *= scale;
		mat.m11 *= scale;
		mat.m22 *= scale;
	}
	
	public static void renderDebugBox()
	{
		toggleFlagDo( () -> {
			final Vec3f r = cur().getRot();
			rot.resetRot( Quat4f.ofEulerRot( r.x, r.y, r.z ) );
		} );
		
		GLUtil.glTranslateV3f( cur().getPos() );
		rot.update( inertia, radian_clamp, damping );
		final Quat4f quat = Quat4f.allocate();
		rot.getRot( Minecraft.getMinecraft().getRenderPartialTicks(), quat );
		final Mat4f mat = Mat4f.allocate();
		mat.set( quat );
		GLUtil.glMultMatrix( mat );
		
		DEBUG_BOX.accept( true );
	}
	
	private static final Quat4f ori = Quat4f.ofEulerRot( 30, 0, 0 );
	private static final Quat4f delta = Quat4f.ofEulerRot( 0, 180, 0 );
	private static final Vec3f av = new Vec3f( 0, MoreMath.PI, 0 );
	public static void render8DebugBox()
	{
		final Vec3f dev_pos = cur().getPos();
		GL11.glTranslatef( 0.0F, 0.0F, dev_pos.z );
//		GLUtil.glTranslateV3f( Dev.cur().getPos() );
//		GLUtil.glEulerRotateYXZ( Dev.cur().getRot() );
//		GLUtil.bindTexture( Texture.GREEN );
		final Mat4f mat = Mat4f.allocate();
		final Quat4f quat = Quat4f.allocate();
		final Vec3f vec = Vec3f.allocate();
		for ( int i = 0; i < 8; i += 1 )
		{
			quat.set( ori );
			quat.addRot( av, cur().getRot().x / 180F );

//			quat.set( this.delta );
//			quat.scaleAngle( Dev.cur().getRot().x / 180F );

//			if ( Dev.flag ) {
//				quat.mul( quat, this.ori );
//			}
//			else {
//				quat.mul( this.ori, quat );
//			}
			
			// First pass.
			mat.set( quat );
			
			vec.set( ( i & 1 ) - 0.5F, ( i >>> 1 & 1 ) - 0.5F, ( i >>> 2 & 1 ) - 0.5F );
			vec.scale( 1F / 16F );
			mat.translate( vec );
			
			GL11.glPushMatrix();
			GL11.glTranslatef( dev_pos.x, 0.0F, 0.0F );
			GLUtil.glMultMatrix( mat );
			GLUtil.glScale1f( 1F / 16F );
			DEBUG_BOX.accept( true );
			GL11.glPopMatrix();
			
			// Second pass.
			GL11.glPushMatrix();
			GL11.glTranslatef( -dev_pos.x, 0.0F, 0.0F );
			
			quat.transform( vec, vec );
			GLUtil.glTranslateV3f( vec );
			GLUtil.glScale1f( 1F / 16F );
			DEBUG_BOX.accept( true );
			
			GL11.glPopMatrix();
		}
		Vec3f.release( vec );
		Quat4f.release( quat );
		Mat4f.release( mat );
	}
	
	
	public static boolean flag;
	
	private static int test_idx;
	private static int test_ins_idx;
	
	private static ArrayList< TestPosRot > test_list;
	
	private static void __switchInstance( boolean backward )
	{
		final int size = test_list.size();
		final int step = backward ? size - 1 : 1;
		test_ins_idx = ( test_ins_idx + step ) % size;
		ChatBoxUtil.addMessage( "Move upon " + cur().name );
	}
	
	private static void __switchValue( boolean backward )
	{
		final int size = cur().value.length;
		final int step = backward ? size - 1 : 1;
		test_idx = ( test_idx + step ) % size;
		ChatBoxUtil.addMessage( "Switch to " + cur().getTestString( test_idx ) );
	}
	
	public static void init()
	{
		FMUM.SIDE.runIfClient( () -> {
			test_list = Lists.newArrayList(
				new TestPosRot( "Primary Pos Rot" ),
				new TestPosRot( "Left Arm Pos Rot" ),
				new TestPosRot( "Right Arm Pos Rot" )
			);
			
			final String prefix = "key.";
			KBPMod.findByName( prefix + Inputs.LAST_SLOT ).get()
				  .addPressCallback( () -> cur().value[ test_idx ] += 5.0F );
			KBPMod.findByName( prefix + Inputs.NEXT_SLOT ).get()
				.addPressCallback( () -> cur().value[ test_idx ] -= 5.0F );
			KBPMod.findByName( prefix + Inputs.LAST_PREVIEW ).get()
				.addPressCallback( () -> cur().value[ test_idx ] += 0.5F );
			KBPMod.findByName( prefix + Inputs.NEXT_PREVIEW ).get()
				.addPressCallback( () -> cur().value[ test_idx ] -= 0.5F );
			KBPMod.findByName( prefix + Inputs.LAST_MODULE ).get()
				.addPressCallback( () -> __switchValue( true ) );
			KBPMod.findByName( prefix + Inputs.NEXT_MODULE ).get()
				.addPressCallback( () -> __switchValue( false ) );
			KBPMod.findByName( prefix + Inputs.LAST_CHANGE ).get()
				.addPressCallback( () -> __switchInstance( true ) );
			KBPMod.findByName( prefix + Inputs.NEXT_CHANGE ).get()
				.addPressCallback( () -> __switchInstance( false ) );
			KBPMod.findByName( prefix + Inputs.CHARGE_GUN ).get()
				.addPressCallback( () -> ChatBoxUtil.addMessage( "On " + cur().name + " :: " + cur().getTestString( test_idx ), cur().toString() ) );
			KBPMod.findByName( prefix + Inputs.CONFIRM_CHANGE ).get()
				.addPressCallback( () -> {
					IntStream.range( 0, cur().value.length ).forEach( i -> cur().value[ i ] = 0.0F );
					ChatBoxUtil.addMessage( "Reset " + cur().name );
				} );
			KBPMod.findByName( prefix + Inputs.NEXT_MODIFY_MODE ).get()
				.addPressCallback( () -> {
					flag = !flag;
					ChatBoxUtil.addMessage( "Toggle flag: " + flag );
				} );
		} );
	}
	
	public static final class TestPosRot
	{
		private final String name;
		
		private final float[] value = new float[ 6 ];
		
		public TestPosRot( String name ) {
			this.name = name;
		}
		
		public Vec3f getPos()
		{
			final float[] v = this.value;
			final Vec3f vec = new Vec3f( v[ 0 ], v[ 1 ], v[ 2 ] );
			vec.scale( 1.0F / 160.0F );
			return vec;
		}
		
		public void applyTransRot()
		{
			final TestPosRot cur = cur();
			GLUtil.glTranslateV3f( cur.getPos() );
			GLUtil.glEulerRotateYXZ( cur.getRot() );
		}
		
		public Vec3f getRot()
		{
			final float[] v = this.value;
			return new Vec3f( v[ 3 ], v[ 4 ], v[ 5 ] );
		}
		
		public String getTestString( int num )
		{
			switch ( num )
			{
			case 0: return "Translate - x";
			case 1: return "Translate - y";
			case 2: return "Translate - z";
			case 3: return "Rotate - x";
			case 4: return "Rotate - y";
			case 5: return "Rotate - z";
			default: return "undefined - " + ( num - 6 );
			}
		}
		
		@Override
		public String toString()
		{
			final float[] v = this.value;
			final Vec3f pos = new Vec3f( v[ 0 ], v[ 1 ], v[ 2 ] );
			return "pos: " + pos + ", rot: " + this.getRot();
		}
	}
}
