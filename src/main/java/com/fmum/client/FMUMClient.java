package com.fmum.client;

import com.fmum.common.FMUM;
import com.fmum.common.network.IPacket;
import com.fmum.common.pack.IContentPack;
import com.fmum.common.pack.ILoadablePack;
import com.fmum.common.pack.ILoadablePack.ILoadContext;
import com.fmum.common.pack.ILoadablePack.IPrepareContext;
import com.fmum.common.tab.CreativeTab;
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SideOnly( Side.CLIENT )
public final class FMUMClient extends FMUM
{
	public static final FMUMClient MOD = new FMUMClient();
	public static final Minecraft MC = Minecraft.getMinecraft();
	public static final GameSettings SETTINGS = MC.gameSettings;
	
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
	protected void _regisContentLoader( IPrepareContext ctx )
	{
		ctx.regisContentLoader( "creative_tab", CreativeTab.class, CreativeTab::buildClientSide );
	}
	
	@Override
	protected Function< ILoadablePack, Function< ILoadContext, Supplier< IContentPack > > >
		_callPackPrepareLoad( IPrepareContext ctx )
	{ return pack -> pack.prepareLoadClientSide( ctx ); }
	
	/**
	 * Send packet to server.
	 */
	public void sendPacketC2S( IPacket packet ) {
		this.packet_handler.sendToServer( packet );
	}
	
	@Override
	public String format( String translate_key, Object... parameters ) {
		return net.minecraft.client.resources.I18n.format( translate_key, parameters );
	}
	
	@Override
	public boolean isClient() {
		return true;
	}
}
