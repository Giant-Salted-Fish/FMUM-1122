package com.fmum.client;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GLContext;

import com.fmum.client.input.InputHandler;
import com.fmum.common.FMUM;
import com.fmum.common.ModWrapper;
import com.fmum.common.item.HostItem;
import com.fmum.common.item.MetaItem;
import com.fmum.common.util.Messager;
import com.fmum.common.util.Vec3;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.discovery.ContainerType;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public final class FMUMClient extends FMUM
{
	public static FMUMClient MOD; { MOD = this; }
	
	public static final int FMUM_CHAT_ID_BASE = 'F' + 'M' + 'U' + 'M';
	
	/**
	 * Easy referencing
	 */
	public static final GameSettings settings = mc.gameSettings;
	
	public float oriMouseSensi = settings.mouseSensitivity;
	public boolean oriViewBobbing = settings.viewBobbing;
	
	public File keyBindsFile = null;
	
	public boolean manualMode = false;
	
	/**
	 * Game GUI in last tick
	 */
	public GuiScreen prevGUI = null;
	
	/**
	 * Inventory slot selected last tick TODO: maybe set default to -1 to trigger take out
	 */
	public int prevSlot = 0;
	
	/**
	 * Item stack that was holden last tick
	 */
	public ItemStack prevStack = ItemStack.EMPTY;
	
	public MetaItem prevMeta = MetaItem.NONE;
	
	/**
	 * Operation that is executing. Call {@link #launchOp(Operation, ItemStack)} rather than
	 * directly set this field if you want to launch a new operation.  
	 */
	public Operation operating = Operation.NONE;
	
	protected final MouseHelper MOUSE_HELPER_PROXY = new MouseHelper()
	{
		@Override
		public void mouseXYChange()
		{
			super.mouseXYChange();
			
			// Call render tick and do pre-render
			FMUMClient.this.prevMeta.onRenderTick( FMUMClient.this.prevStack, this );
		}
	};
	
	@Override
	public void checkOpenGLCapability()
	{
		if( !GLContext.getCapabilities().OpenGL30 )
			throw new RuntimeException( I18n.format( "fmum.openglversiontoolow" ) );
	}
	
	@Override
	public void loadKeyBinds()
	{
		// Create key bind file if not exist
		if( !keyBindsFile.exists() )
		{
			try { this.keyBindsFile.createNewFile(); }
			catch( IOException e ) {
				log.error( I18n.format( "fmum.errorcreatingkeybindsfile" ) );
			}
			InputHandler.saveTo( this.keyBindsFile );
		}
		
		// Otherwise, read key binds from the file
		// NOTICE: an empty key bind file may file to trigger lazy load of keys
		else InputHandler.readFrom( this.keyBindsFile );
	}
	
	@Override
	public void regisLocalResource( File source )
	{
		super.regisLocalResource( source );
		
		// Register it as a resource pack to load textures and sounds client side
		HashMap< String, Object > descriptor = new HashMap<>();
		descriptor.put( "modid", FMUM.MODID );
		descriptor.put( "name", FMUM.MOD_NAME + ":" + source.getName() );
		descriptor.put( "version", "1" );
		FMLModContainer container = new FMLModContainer(
			ModWrapper.class.getCanonicalName(),
			new ModCandidate(
				source,
				source,
				source.isDirectory() ? ContainerType.DIR : ContainerType.JAR
			),
			descriptor
		);
		container.bindMetadata( MetadataCollection.from( null, "" ) );
		FMLClientHandler.instance().addModAsResource( container );
	}
	
	@Nullable
	@Override
	public ResourceLocation loadTexture( String path ) {
		return ResourceHandler.getTexture( path );
	}
	
	@Override
	public String format( String translateKey, Object... parameters ) {
		return I18n.format( translateKey, parameters );
	}
	
	void tick()
	{
		// Check GUI change
		if( mc.currentScreen != this.prevGUI )
		{
			// Show key binds if control GUI is activated
			if( mc.currentScreen instanceof GuiControls )
			{
				InputHandler.showMCKeyBind();
				settings.mouseSensitivity = this.oriMouseSensi;
			}
			else if( this.prevGUI instanceof GuiControls )
			{
				InputHandler.clearMCKeyBind();
				this.oriMouseSensi = settings.mouseSensitivity;
			}
			
			// FIXME: validate if this works OK
			// Set back option values if options GUI is launched
			else if( mc.currentScreen instanceof GuiOptions )
			{
				settings.viewBobbing = this.oriViewBobbing;
//				settings.gammaSetting = this.oriGamma;
			}
			else if( this.prevGUI instanceof GuiOptions )
			{
				this.oriViewBobbing = settings.viewBobbing;
//				this.oriGamma = settings.gammaSetting;
			}
			
			this.prevGUI = mc.currentScreen;
		}
		
		// Abandon if have not entered a world yet
		EntityPlayerSP player = mc.player;
		if( player == null ) return;
		
		// Go through the items in player's inventory
		IInventory inv = player.inventory;
		for( int i = inv.getSizeInventory(); i-- > 0; )
		{
			ItemStack stack = inv.getStackInSlot( i );
			if( !( stack.getItem() instanceof HostItem ) ) continue;
			// TODO: maybe count the weight of traditional items via a map
			
			MetaItem meta = ( ( HostItem ) stack.getItem() ).meta();
			meta.onInvTick( stack, inv, i );
			
			// TODO: weight and volume
		}
		
		ItemStack stack = player.inventory.getCurrentItem();
		MetaItem meta = HostItem.getMeta( stack );
		
		// Check if item holding has changed
		if( player.inventory.currentItem != this.prevSlot || stack != this.prevStack )
		{
			settings.viewBobbing = this.oriViewBobbing && meta.allowViewBobbing();
			
			// Fire callback functions
			HostItem.getMeta( this.prevStack ).onPutAway( stack );
			this.operating = this.operating.onHoldingItemChange( stack );
			meta.onTakeOut( stack );
			
			// TODO: maybe move this to the tail
			this.prevSlot = player.inventory.currentItem;
			this.prevStack = stack;
			this.prevMeta = meta;
		}
		
		// Tick item and current operation TODO: figure out which one is better to be the first
		meta.onHandTick( stack );
		this.operating = this.operating.tick( stack );
		
		// Ensure mouse helper(Mods like Flan's Mod may change mouse help in certain conditions)
		mc.mouseHelper = this.MOUSE_HELPER_PROXY;
	}
	
	@Override
	protected void loadLocalizationFile( String fName ) { }
	
	@Override
	protected void parseConfig( Configuration config )
	{
		super.parseConfig( config );
		final String CATEGORY = "Client";
		
		// Parse client side only settings
		this.keyBindsFile = new File(
			this.mcDir,
			config.getString(
				"keyBindsFile",
				CATEGORY,
				"config/fmumoptions.txt",
				"File name where FMUM will save key binds to"
			)
		);
	}
	
	/**
	 * Try to launch a new operation
	 * 
	 * @param op New operation to launch
	 * @param stack Item stack that the new operation works on
	 * @return Executing operation after this call
	 */
	public Operation launchOp( Operation op, ItemStack stack ) {
		return this.operating = this.operating.onNewOpLaunch( op, stack );
	}
	
	public static void addPromptMsg( String... msg ) {
		for( int i = 0; i < msg.length; ++i ) addPromptMsg( msg[ i ], i );
	}
	
	public static void addPromptMsg( String msg, int id )
	{
		mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(
			new TextComponentString( msg ),
			FMUM_CHAT_ID_BASE + id
		);
	}
	
	public static void addChatMsg( String... msg )
	{
		for( String s : msg )
			mc.ingameGUI.getChatGUI().printChatMessage( new TextComponentString( s ) );
	}
	
	/** for test */
	public static boolean tu = false, td = false, tl = false, tr = false, te = false, tq = false;
	
	public static int testNum = 0, testInsNum = 0;
	
	public static final LinkedList<TestPosRot> testList = new LinkedList<>();
	static
	{
		testList.add( new TestPosRot() );
		testList.add( new TestPosRot() );
		testList.add( new TestPosRot() );
	}
	
	//public static HitBoxes hbs0 = null, hbs1 = null;
	public static EntityPlayerSP player;
	
	public static String getTestString( int num )
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
	
	public static String getTestInsString( int num )
	{
		switch( num )
		{
		case 0: return "Primary Pos Rot";
		case 1: return "Left Arm Pos Rot";
		case 2: return "Right Arm Pos Rot";
		default: return "Custom Pos Rot " + ( num - 2 );
		}
	}
	
	public static final class TestPosRot
	{
		public final double[] testValue = { 0D, 0D, 0D, 0D, 0D, 0D };
		
		public void getPos( Vec3 dst ) {
			dst.set( this.testValue[ 0 ], this.testValue[ 1 ], this.testValue[ 2 ] );
		}
		
		public void getRot( Vec3 dst ) {
			dst.set( this.testValue[ 3 ], this.testValue[ 4 ], this.testValue[ 5 ] );
		}
	}
	
	public static void toggleManualTell( Messager... msg )
	{
		if( !MOD.manualMode ) return;
		
		for( int i = 0; i < msg.length; ++i )
			addPromptMsg( msg[ i ].message(), i );
		MOD.manualMode = false;
	}
	/** for test */
}
