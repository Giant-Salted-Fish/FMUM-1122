package com.mcwb.devtool;

import java.util.Collection;
import java.util.LinkedList;

import org.lwjgl.input.Keyboard;

import com.mcwb.client.IAutowirePlayerChat;
import com.mcwb.client.input.IKeyBind;
import com.mcwb.client.input.InputHandler;
import com.mcwb.client.input.KeyBind;
import com.mcwb.common.MCWB;
import com.mcwb.util.Vec3f;

import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * A temporary class that helps with development. Should be removed on release.
 * 
 * @author Giant_Salted_Fish
 */
@EventBusSubscriber( modid = MCWB.MODID, value = Side.CLIENT )
public class DevHelper implements IAutowirePlayerChat
{
	public static final DevHelper INS = new DevHelper();
	
	private boolean tu = false, td = false, tl = false, tr = false, te = false, tq = false;
	
	private boolean flag = false;
	
	private int testNum = 0, testInsNum = 0;
	
	private final LinkedList<TestPosRot> testList = new LinkedList<>(); {
		this.testList.add( new TestPosRot( "Primary Pos Rot" ) );
		this.testList.add( new TestPosRot( "Left Arm Pos Rot" ) );
		this.testList.add( new TestPosRot( "Right Arm Pos Rot" ) );
	}
	
	public TestPosRot cur() { return this.testList.get( this.testInsNum ); }
	
//	public HitBoxes hbs0 = null, hbs1 = null;
	
	{
		final String group = "test";
		final Collection< IKeyBind > updateGroup = InputHandler.GLOBAL_KEYS;
		new KeyBind( "test_up", group, Keyboard.KEY_UP, updateGroup ) {
			@Override
			protected void fire() { DevHelper.this.tu = true; }
		};
		new KeyBind( "test_down", group, Keyboard.KEY_DOWN, updateGroup ) {
			@Override
			protected void fire() { DevHelper.this.td = true; }
		};
		new KeyBind( "test_left", group, Keyboard.KEY_LEFT, updateGroup ) {
			@Override
			protected void fire() { DevHelper.this.tl = true; }
		};
		new KeyBind( "test_right", group, Keyboard.KEY_RIGHT, updateGroup ) {
			@Override
			protected void fire() { DevHelper.this.tr = true; }
		};
		new KeyBind( "test_enter", group, Keyboard.KEY_NUMPAD5, updateGroup ) {
			@Override
			protected void fire() { DevHelper.this.te = true; }
		};
		new KeyBind( "test_quit", group, Keyboard.KEY_NUMPAD2, updateGroup ) {
			@Override
			protected void fire() { DevHelper.this.tq = true; }
		};
		new KeyBind( "test_flag", group, Keyboard.KEY_F10, updateGroup ) {
			@Override
			protected void fire()
			{
				DevHelper.this.flag = !DevHelper.this.flag;
			}
		};
	}
	
	public void tick()
	{
		final TestPosRot instance = this.testList.get( this.testInsNum );
		final boolean co = InputHandler.CO.down;
		
		if( this.tu || this.td )
			instance.testValue[ this.testNum ] += ( this.tu ? 1F : -1F ) * ( co ? 0.5F : 5F );
		
		else if( co )
		{
			if( this.tl || this.tr )
			{
				final int size = this.testList.size();
				this.testInsNum = ( this.testInsNum + ( this.tl ? size - 1 : 1 ) ) % size;
				this.sendPlayerMsg( "Move upon " + this.testList.get( this.testInsNum ).name );
			}
			else if( this.te )
			{
				for( int i = instance.testValue.length; i-- > 0; instance.testValue[ i ] = 0F );
				this.sendPlayerMsg( "Reset " + instance.name + " to 0F" );
			}
		}
		
		else if( this.tl || this.tr )
		{
			final int size = instance.testValue.length;
			this.testNum = ( this.testNum + ( this.tl ? size - 1 : 1 ) ) % size;
			this.sendPlayerMsg( "Switch to " + instance.getTestString( this.testNum ) );
		}
		else if( this.te )
			this.sendPlayerMsg(
				"On " + instance.name + " :: " + instance.getTestString( this.testNum ),
				instance.toString()
			);
		
		this.tu = this.td = this.tl = this.tr = this.te = this.tq = false;
	}
	
	public void toggleFlagTell( String... msg )
	{
		if( this.flag )
		{
			this.sendPlayerMsg( msg );
			this.flag = false;
		}
	}
	
	@SubscribeEvent
	public static void onTick( ClientTickEvent evt ) { INS.tick(); }
	
	public static final class TestPosRot
	{
		public final String name;
		
		public final float[] testValue = new float[ 6 ];
		
		public TestPosRot( String name ) { this.name = name; }
		
		public Vec3f getPos()
		{
			return new Vec3f(
				this.testValue[ 0 ] / 16F,
				this.testValue[ 1 ] / 16F,
				this.testValue[ 2 ] / 16F
			);
		}
		
		public Vec3f getRot() {
			return new Vec3f( this.testValue[ 3 ], this.testValue[ 4 ], this.testValue[ 5 ] );
		}
		
		public String getTestString( int num )
		{
			switch( num )
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
			Vec3f pos = new Vec3f().set( this.getPos() );
			pos.scale( 16F );
			return "pos: " + pos + ", rot: " + this.getRot();
		}
	}
}
