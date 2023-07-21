package com.fmum.client;

import com.fmum.common.FMUM;
import com.fmum.common.FMUMResource;
import com.fmum.common.network.IPacket;
import com.fmum.common.pack.ILoadablePack;
import com.fmum.common.pack.ILoadablePack.IPrepareContext;
import com.fmum.common.tab.CreativeTab;
import com.google.gson.JsonDeserializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.discovery.ContainerType;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GLContext;

import java.io.File;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.function.Consumer;

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
	protected void _regisResourceDomain( File file )
	{
		super._regisResourceDomain( file );
		
		// Register it as a resource pack to load textures and sounds.
		// See Flan's Mod content pack load.
		final TreeMap< String, Object > descriptor = new TreeMap<>();
		descriptor.put( "modid", MODID );
		descriptor.put( "name", MOD_NAME + ":" + file.getName() );
		descriptor.put( "version", "1" ); // TODO: from pack info maybe
		final ContainerType container_type = file.isFile() ? ContainerType.JAR : ContainerType.DIR;
		final ModCandidate candidate = new ModCandidate( file, file, container_type );
		final FMLModContainer container = new FMLModContainer(
			FMUM.class.getName(), candidate, descriptor );
		container.bindMetadata( MetadataCollection.from( null, "" ) );
		FMLClientHandler.instance().addModAsResource( container );
	}
	
	@Override
	protected void _regisGsonAdapter(IPrepareContext ctx)
	{
		super._regisGsonAdapter(ctx);
		
		final JsonDeserializer< ResourceLocation > textureAdapter = ( json, typeOfT, context ) -> {
			final String path = json.getAsString();
			return this.texture_pool.computeIfAbsent( path, FMUMResource::new );
		};
		ctx.regisGsonAdapter( ResourceLocation.class, textureAdapter );
	}
	
	@Override
	protected void _regisContentLoader( IPrepareContext ctx )
	{
		ctx.regisContentLoader( "creative_tab", CreativeTab.class, CreativeTab::buildClientSide );
	}
	
	@Override
	protected Consumer< ILoadablePack > _callPrepareLoadPack( IPrepareContext ctx ) {
		return pack -> pack.prepareLoadClientSide( ctx );
	}
	
	@Override
	protected void _reloadResources()
	{
		// Force resource reload to load those in domain of content packs.
		// TODO: maybe check if is only mod based content pack
		FMLClientHandler.instance().refreshResources(
			VanillaResourceType.MODELS,
			VanillaResourceType.TEXTURES,
			VanillaResourceType.SOUNDS,
			VanillaResourceType.LANGUAGES
		);
	}
	
	@Override
	public String format( String translate_key, Object... parameters ) {
		return net.minecraft.client.resources.I18n.format( translate_key, parameters );
	}
	
	public static void sendToServer( IPacket packet ) {
		PACKET_HANDLER.sendToServer( packet );
	}
	
	@Override
	public boolean isClient() {
		return true;
	}
}
