package com.fmum.client;

import com.fmum.client.input.JsonKeyBindType;
import com.fmum.common.FMUM;
import com.fmum.common.load.BuildableType;
import com.fmum.common.load.ContentBuildContext;
import com.fmum.common.network.Packet;
import com.fmum.common.pack.ContentPack;
import com.fmum.common.pack.ContentPackFactory;
import com.fmum.common.pack.ContentPackFactory.IPrepareContext;
import com.fmum.common.pack.FolderPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GLContext;

import java.io.File;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
	protected void _gatherSelfKeyBindPack(
		ModContainer container,
		BiConsumer< ContentPackFactory, String > visitor
	) {
		final FolderPack pack = new FolderPack( container )
		{
			@Override
			protected void _loadPackContent( ILoadContext ctx )
			{
				final File dir = new File(
					FMUMClient.this.config_dir, "fmum-key_bind" );
				this._tryLoadFrom( dir, "key_bind", dir.getPath(), ctx );
			}
		};
		visitor.accept( pack, MOD_NAME );
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
	protected void _regisGsonAdapter( IPrepareContext ctx )
	{
		super._regisGsonAdapter( ctx );
		
		ctx.regisGsonAdapter(
			ResourceLocation.class,
			( json, type_of_T, context ) -> {
				final String path = json.getAsString();
				return this.texture_pool.computeIfAbsent(
					path, ResourceLocation::new );
			}
		);
		
		ctx.regisGsonAdapter(
			KeyModifier.class,
			( json, type_of_T, context ) ->
				KeyModifier.valueFromString( json.getAsString() )
		);
	}
	
	@Override
	protected void _doRegisContentLoader(
		BiConsumer< String, Class< ? extends BuildableType > > regis
	) {
		super._doRegisContentLoader( regis );
		
		regis.accept( "key_bind", JsonKeyBindType.class );
	}
	
	@Override
	protected void _callContentBuildOnSide(
		BuildableType buildable,
		ContentBuildContext ctx
	) { buildable.buildClientSide( ctx ); }
	
	@Override
	protected Function< ContentPackFactory, ContentPack >
		_callCreateOnSide( IPrepareContext ctx )
	{ return pack -> pack.createClientSide( ctx ); }
	
	/**
	 * Send packet to server.
	 */
	public void sendPacketC2S( Packet packet ) {
		this.packet_handler.sendToServer( packet );
	}
	
	@Override
	public boolean isClient() {
		return true;
	}
	
	@Override
	public String format( String translate_key, Object... parameters ) {
		return I18n.format( translate_key, parameters );
	}
}
