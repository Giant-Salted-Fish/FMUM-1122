package com.fmum.client;

import com.fmum.common.FMUM;
import com.fmum.common.load.BuildableType;
import com.fmum.common.load.IContentBuildContext;
import com.fmum.common.load.IContentLoader;
import com.fmum.common.network.IPacket;
import com.fmum.common.pack.IContentPack;
import com.fmum.common.pack.IContentPackFactory;
import com.fmum.common.pack.IContentPackFactory.IPrepareContext;
import com.google.gson.JsonDeserializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GLContext;

import java.util.HashMap;
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
	
	private final HashMap< String, ResourceLocation > texture_pool = new HashMap<>();
	
	private FMUMClient() { }
	
	@Override
	protected void _loadContentPacks()
	{
		// Check render device capabilities.
		if ( !GLContext.getCapabilities().OpenGL30 ) {
			throw new RuntimeException( I18n.format( "fmum.opengl_version_too_low" ) );
		}
		
		final Framebuffer framebuffer = MC.getFramebuffer();
		if ( !framebuffer.isStencilEnabled() && !framebuffer.enableStencil() ) {
			throw new RuntimeException( I18n.format( "fmum.stencil_not_supported" ) );
		}
		
		// Do load content packs!
		super._loadContentPacks();
	}
	
	@Override
	protected void _regisGsonAdapter( IPrepareContext ctx )
	{
		super._regisGsonAdapter( ctx );
		
		final JsonDeserializer< ResourceLocation > textureAdapter = ( json, typeOfT, context ) -> {
			final String path = json.getAsString();
			return this.texture_pool.computeIfAbsent( path, ResourceLocation::new );
		};
		ctx.regisGsonAdapter( ResourceLocation.class, textureAdapter );
	}
	
	@Override
	protected void _callContentBuild( BuildableType buildable, IContentBuildContext ctx ) {
		buildable.buildClientSide( ctx );
	}
	
	@Override
	protected Function< IContentPackFactory, IContentPack >
		_callSideBasedCreate( IPrepareContext ctx )
	{ return pack -> pack.createClientSide( ctx ); }
	
	/**
	 * Send packet to server.
	 */
	public void sendPacketC2S( IPacket packet ) {
		this.packet_handler.sendToServer( packet );
	}
	
	@Override
	public boolean isClient() {
		return true;
	}
	
	@Override
	public String format( String translate_key, Object... parameters ) {
		return net.minecraft.client.resources.I18n.format( translate_key, parameters );
	}
}
