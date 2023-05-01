package com.fmum.devtool;

import java.util.Collection;
import java.util.LinkedList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.fmum.client.FMUMClient;
import com.fmum.client.input.IKeyBind;
import com.fmum.client.input.InputHandler;
import com.fmum.client.input.Key;
import com.fmum.client.input.KeyBind;
import com.fmum.common.FMUM;
import com.fmum.util.Vec3f;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * A temporary class that helps with development. Should be removed on release.
 * 
 * @author Giant_Salted_Fish
 */
@EventBusSubscriber( modid = FMUM.MODID, value = Side.CLIENT )
public class Dev
{
	public static int dirtyMark() { return 0; }
	
//	public static final Consumer< Boolean > DEBUG_BOX = new Consumer< Boolean >()
//	{
//		private final Model renderer = new Model( "models/debug_box.obj", 0.0625F, true );
//		
//		private final ResourceLocation texture = FMUMClient.MOD.loadTexture( "textures/debug_box.png" );
//		
//		@Override
//		public void accept( Boolean useDefaultTexture )
//		{
//			if ( useDefaultTexture ) {
//				FMUMClient.MOD.bindTexture( this.texture );
//			}
//			this.renderer.render();
//		}
//	};
	
//	public static boolean flag = false;
//	
//	private static boolean tu = false, td = false, tl = false, tr = false, te = false, tq = false;
//	
//	private static int testNum = 0, testInsNum = 0;
//	
//	private static final LinkedList<TestPosRot> testList = new LinkedList<>();
//	static
//	{
//		testList.add( new TestPosRot( "Primary Pos Rot" ) );
//		testList.add( new TestPosRot( "Left Arm Pos Rot" ) );
//		testList.add( new TestPosRot( "Right Arm Pos Rot" ) );
//	}
//	
//	public static TestPosRot cur() { return testList.get( testInsNum ); }
//	
//	public static TestPosRot get( int i ) { return testList.get( i ); }
//	
//	static
//	{
//		if ( FMUM.MOD.isClient() )
//		{
//			final String group = "test";
//			final Collection< IKeyBind > updateGroup = InputHandler.GLOBAL_KEYS;
//			new KeyBind( "test_up", group, Keyboard.KEY_UP, updateGroup ) {
//				@Override
//				protected void onFire() { tu = true; }
//			};
//			new KeyBind( "test_down", group, Keyboard.KEY_DOWN, updateGroup ) {
//				@Override
//				protected void onFire() { td = true; }
//			};
//			new KeyBind( "test_left", group, Keyboard.KEY_LEFT, updateGroup ) {
//				@Override
//				protected void onFire() { tl = true; }
//			};
//			new KeyBind( "test_right", group, Keyboard.KEY_RIGHT, updateGroup ) {
//				@Override
//				protected void onFire() { tr = true; }
//			};
//			new KeyBind( "test_enter", group, Keyboard.KEY_NUMPAD5, updateGroup ) {
//				@Override
//				protected void onFire() { te = true; }
//			};
//			new KeyBind( "test_quit", group, Keyboard.KEY_NUMPAD2, updateGroup )
//			{
//				@Override
//				protected void onFire() { tq = true; }
//			};
//			new KeyBind( "test_flag", group, Keyboard.KEY_F10, updateGroup )
//			{
//				@Override
//				protected void onFire() { flag = !flag; }
//			};
//			
//			MinecraftForge.EVENT_BUS.register( new Object() {
//				@SubscribeEvent
//				public void onGuiOpen( GuiOpenEvent evt )
//				{
//					InputHandler.updateMappers();
//					MinecraftForge.EVENT_BUS.unregister( this );
//				}
//			} );
//		}
//	}
//	
//	public static void tick()
//	{
//		final TestPosRot instance = testList.get( testInsNum );
//		final boolean co = Key.ASSIST.down;
//		
//		if ( tu || td )
//			instance.testValue[ testNum ] += ( tu ? 1F : -1F ) * ( co ? 0.5F : 5F );
//		
//		else if ( co )
//		{
//			if ( tl || tr )
//			{
//				final int size = testList.size();
//				testInsNum = ( testInsNum + ( tl ? size - 1 : 1 ) ) % size;
//				FMUMClient.MOD.sendPlayerMsg( "Move upon " + testList.get( testInsNum ).name );
//			}
//			else if ( te )
//			{
//				for ( int i = instance.testValue.length; i-- > 0; instance.testValue[ i ] = 0F );
//				FMUMClient.MOD.sendPlayerMsg( "Reset " + instance.name );
//			}
//		}
//		
//		else if ( tl || tr )
//		{
//			final int size = instance.testValue.length;
//			testNum = ( testNum + ( tl ? size - 1 : 1 ) ) % size;
//			FMUMClient.MOD.sendPlayerMsg( "Switch to " + instance.getTestString( testNum ) );
//		}
//		else if ( te )
//			FMUMClient.MOD.sendPlayerMsg(
//				"On " + instance.name + " :: " + instance.getTestString( testNum ),
//				instance.toString()
//			);
//		
//		tu = td = tl = tr = te = tq = false;
//	}
//	
//	public static void toggleFlagTell( String... msg )
//	{
//		if ( flag )
//		{
//			FMUMClient.MOD.sendPlayerMsg( msg );
//			flag = false;
//		}
//	}
//	
//	public static void toggleFlagDo( Runnable task )
//	{
//		if ( flag )
//		{
//			task.run();
//			flag = false;
//		}
//	}
//	
//	@SubscribeEvent
//	public static void onTick( ClientTickEvent evt ) { tick(); }
//	
////	@SubscribeEvent
////	public static void onPlayerRender( RenderPlayerEvent.Pre evt )
////	{
////		FMUMClient.MC.renderEngine.bindTexture( new FMUMResource( "textures/debug_box.png" ) );
////		
////		GL11.glEnable( GL11.GL_STENCIL_TEST );
////		GL11.glStencilFunc( GL11.GL_NEVER, 1, 0xFF );
////		
////		GL11.glPushMatrix();
////		{
////			final float s = 16F; //3F * 16F;
////			GL11.glScalef( s, s, s );
////			
////			final Vec3f v = cur().getPos();
////			GL11.glTranslatef( v.x, v.y, v.z );
////			
////			GL11.glDisable( GL11.GL_DEPTH_TEST );
////			DEBUG_BOX.render();
////			GL11.glEnable( GL11.GL_DEPTH_TEST );
////		}
////		GL11.glPopMatrix();
////		
////		GL11.glStencilFunc( GL11.GL_ALWAYS, 1, 0xFF );
////		GL11.glDisable( GL11.GL_STENCIL_TEST );
////	}
//	
//	public static final class TestPosRot
//	{
//		public final String name;
//		
//		public final float[] testValue = new float[ 6 ];
//		
//		public TestPosRot( String name ) { this.name = name; }
//		
//		public Vec3f getPos()
//		{
//			return new Vec3f(
//				this.testValue[ 0 ] / 160F,
//				this.testValue[ 1 ] / 160F,
//				this.testValue[ 2 ] / 160F
//			);
//		}
//		
//		public void applyPos()
//		{
//			final Vec3f p = this.getPos();
//			GL11.glTranslatef( p.x, p.y, p.z );
//		}
//		
//		public Vec3f getRot() {
//			return new Vec3f( this.testValue[ 3 ], this.testValue[ 4 ], this.testValue[ 5 ] );
//		}
//		
//		public String getTestString( int num )
//		{
//			switch ( num )
//			{
//			case 0: return "Translate - x";
//			case 1: return "Translate - y";
//			case 2: return "Translate - z";
//			case 3: return "Rotate - x";
//			case 4: return "Rotate - y";
//			case 5: return "Rotate - z";
//			default: return "undefined - " + ( num - 6 );
//			}
//		}
//		
//		@Override
//		public String toString()
//		{
//			final Vec3f pos = new Vec3f(
//				this.testValue[ 0 ],
//				this.testValue[ 1 ],
//				this.testValue[ 2 ]
//			);
//			return "pos: " + pos + ", rot: " + this.getRot();
//		}
//	}
}
