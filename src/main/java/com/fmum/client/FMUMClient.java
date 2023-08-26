package com.fmum.client;

import com.fmum.client.input.JsonKeyBindType;
import com.fmum.client.input.KeyBindManager;
import com.fmum.client.input.KeyBindType;
import com.fmum.common.FMUM;
import com.fmum.common.load.BuildableType;
import com.fmum.common.load.IContentBuildContext;
import com.fmum.common.network.IPacket;
import com.fmum.common.pack.IContentPack;
import com.fmum.common.pack.IContentPackFactory;
import com.fmum.common.pack.IContentPackFactory.IPrepareContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GLContext;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@SideOnly( Side.CLIENT )
public final class FMUMClient extends FMUM
{
	public static final FMUMClient MOD = new FMUMClient();
	public static final Minecraft MC = Minecraft.getMinecraft();
	public static final GameSettings SETTINGS = MC.gameSettings;
	
	public static final ResourceLocation
		TEXTURE_RED = new ResourceLocation( MODID, "textures/0xff0000.png" ),
		TEXTURE_GREEN = new ResourceLocation( MODID, "textures/0x00ff00.png" ),
		TEXTURE_BLUE = new ResourceLocation( MODID, "textures/0x0000ff.png" );
	
	private final HashMap< String, ResourceLocation >
		texture_pool = new HashMap<>();
	
	private FMUMClient() { }
	
	@Override
	public boolean isClient() {
		return true;
	}
	
	@Override
	public String format( String translate_key, Object... parameters ) {
		return I18n.format( translate_key, parameters );
	}
	
	/**
	 * Send packet to server.
	 */
	public void sendPacketC2S( IPacket packet ) {
		this.packet_handler.sendToServer( packet );
	}
	
	@Override
	protected void _loadContentPacks()
	{
		// Check render device capabilities.
		if ( !GLContext.getCapabilities().OpenGL30 )
		{
			final String err_msg = I18n.format( "fmum.opengl_version_too_low" );
			throw new RuntimeException( err_msg );
		}
		
		final Framebuffer framebuffer = MC.getFramebuffer();
		if ( !framebuffer.isStencilEnabled() && !framebuffer.enableStencil() )
		{
			final String err_msg = I18n.format( "fmum.stencil_not_supported" );
			throw new RuntimeException( err_msg );
		}
		
		// Do load content packs!
		super._loadContentPacks();
	}
	
	@Override
	protected void _loadKeyBindSettings()
	{
		final String file_name = MODID + "-key_bind-settings.json";
		final File settings_file = new File( this.config_dir, file_name );
		if ( settings_file.exists() )
		{
			KeyBindManager.loadSettingsFrom( settings_file );
			return;
		}
		
		try
		{
			settings_file.createNewFile();
		}
		catch ( IOException e )
		{
			// TODO: Handle io exception
		}
		
		KeyBindManager.saveSettingsTo( settings_file );
	}
	
	@Override
	protected void _regisGsonAdapter( IPrepareContext ctx )
	{
		super._regisGsonAdapter( ctx );
		
		ctx.regisGsonDeserializer(
			ResourceLocation.class,
			( json, type_of_T, context ) -> {
				final String path = json.getAsString();
				return this.texture_pool.computeIfAbsent(
					path, ResourceLocation::new );
			}
		);
		
		ctx.regisGsonDeserializer(
			KeyModifier.class,
			( json, type_of_T, context ) ->
				KeyModifier.valueFromString( json.getAsString() )
		);
		
		ctx.regisGsonDeserializer(
			IKeyConflictContext.class,
			( json, type_of_T, context ) -> {
				try {
					return KeyConflictContext.valueOf( json.getAsString() );
				}
				catch ( NullPointerException | IllegalArgumentException e ) {
					return KeyConflictContext.UNIVERSAL;
				}
			}
		);
	}
	
	@Override
	protected void _callContentBuildOnSide(
		BuildableType buildable,
		IContentBuildContext ctx
	) { buildable.buildClientSide( ctx ); }
	
	@Override
	protected void _doRegisContentLoader(
		BiConsumer< String, Class< ? extends BuildableType > > regis
	) {
		super._doRegisContentLoader( regis );
		
		regis.accept( "key_bind", JsonKeyBindType.class );
	}
	
	@Override
	protected IContentPack _callCreatePackOnSide(
		IContentPackFactory factory, IPrepareContext ctx
	) { return factory.createClientSide( ctx ); }
	
	@Override
	protected Map< String, ? > _createDefaultKeyBinds()
	{
		final HashMap< String, Object > default_key_binds = new HashMap<>();
		final String category_common = "fmum.key_category.common";
		
		default_key_binds.put(
			"free_view",
			new KeyBindType(
				"free_view",
				category_common,
				"free_view",
				"",
				Keyboard.KEY_Z,
				KeyModifier.NONE,
				KeyConflictContext.IN_GAME
			)
		);
		return default_key_binds;
	}
}
