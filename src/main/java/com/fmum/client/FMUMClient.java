package com.fmum.client;

import com.fmum.common.FMUM;
import com.fmum.common.FMUMResource;
import com.fmum.common.Registry;
import com.fmum.common.network.IPacket;
import com.fmum.common.pack.ILoadablePack.IPrepareContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GLContext;

import java.util.HashMap;

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
	protected void _prepareContentPackLoad( IPrepareContext ctx )
	{
		super._prepareContentPackLoad( ctx );
		
		ctx.regisGsonAdapter(
			ResourceLocation.class,
			( json, typeOfT, context ) -> {
				final String path = json.getAsString();
				return this.texture_pool.computeIfAbsent( path, FMUMResource::new );
			}
		);
	}
	
	@Override
	public String format( String translate_key, Object... parameters ) {
		return net.minecraft.client.resources.I18n.format( translate_key, parameters );
	}
	
	public static void sendPacketToServer( IPacket packet ) {
		PACKET_HANDLER.sendToServer( packet );
	}
	
	@Override
	public boolean isClient() {
		return true;
	}
}
