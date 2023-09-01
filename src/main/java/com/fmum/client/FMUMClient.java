package com.fmum.client;

import com.fmum.client.input.ToggleKeyBindType;
import com.fmum.client.input.KeyBindManager;
import com.fmum.client.input.KeyBindType;
import com.fmum.client.player.PlayerPatchClient;
import com.fmum.common.FMUM;
import com.fmum.common.load.BuildableType;
import com.fmum.common.load.IContentBuildContext;
import com.fmum.common.network.IPacket;
import com.fmum.common.pack.IContentPack;
import com.fmum.common.pack.IContentPackFactory;
import com.fmum.common.pack.IContentPackFactory.IPrepareContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GLContext;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.BiConsumer;

@SideOnly( Side.CLIENT )
public final class FMUMClient extends FMUM
{
	public static final FMUMClient MOD = new FMUMClient();
	public static final Minecraft MC = Minecraft.getMinecraft();
	
	public static final ResourceLocation
		TEXTURE_RED = new ResourceLocation( MODID, "textures/0xff0000.png" ),
		TEXTURE_GREEN = new ResourceLocation( MODID, "textures/0x00ff00.png" ),
		TEXTURE_BLUE = new ResourceLocation( MODID, "textures/0x0000ff.png" );
	
	
	// Synced Config settings.
	public static float free_view_limit_squared;
	
	public static float camera_drop_cycle;
	public static float camera_drop_amplitude;
	
	public static float camera_drop_impact;
	
	
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
		
		PlayerPatchClient.setMouseHelperStrategy(
			ModConfigClient.use_flan_compatible_mousehelper );
	}
	
	@Override
	protected void _loadKeyBindSetting()
	{
		final File settings_file = this._keyBindSettingFile();
		if ( settings_file.exists() )
		{
			KeyBindManager.loadSettingsFrom( settings_file );
			return;
		}
		
		try {
			settings_file.createNewFile();
		}
		catch ( IOException e )
		{
			this.logException( e, "fmum.error_creating_key_binds_file" );
			return;
		}
		
		KeyBindManager.saveSettingsTo( settings_file );
	}
	
	File _keyBindSettingFile()
	{
		final String file_name = MODID + "-key_bind-setting.json";
		return new File( this.config_dir, file_name );
	}
	
	@Override
	protected void _regisGsonAdapter( IPrepareContext ctx )
	{
		super._regisGsonAdapter( ctx );
		
		ctx.regisGsonDeserializer(
			ResourceLocation.class,
			( json, type_of_T, context ) -> {
				final String path = json.getAsString();
				return this.texture_pool
					.computeIfAbsent( path, ResourceLocation::new );
			}
		);
		
		ctx.regisGsonDeserializer(
			IKeyConflictContext.class,
			( json, type_of_T, context ) ->
				context.deserialize( json, KeyConflictContext.class )
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
		
		regis.accept( "key_bind", KeyBindType.class );
		regis.accept( "toggle_key_bind", ToggleKeyBindType.class );
	}
	
	@Override
	protected IContentPack _callCreatePackOnSide(
		IContentPackFactory factory, IPrepareContext ctx
	) { return factory.createClientSide( ctx ); }
}
