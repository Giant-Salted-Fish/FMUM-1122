package com.mcwb.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GLContext;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mcwb.client.gun.GunPartRenderer;
import com.mcwb.client.gun.GunRenderer;
import com.mcwb.client.input.InputHandler;
import com.mcwb.client.render.IRenderer;
import com.mcwb.client.render.Renderer;
import com.mcwb.common.MCWB;
import com.mcwb.common.MCWBResource;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.load.IRequireMeshLoad;
import com.mcwb.common.meta.Registry;
import com.mcwb.common.pack.IContentProvider;
import com.mcwb.util.Mesh;
import com.mcwb.util.ObjMeshBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.discovery.ContainerType;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public final class MCWBClient extends MCWB
	implements IAutowirePlayerChat, IAutowireBindTexture, IAutowireSmoother
{
	/// Easy referencing ///
	public static final Minecraft MC = Minecraft.getMinecraft();
	public static final GameSettings SETTINGS = MC.gameSettings;
	
	public static final MCWBClient MOD = new MCWBClient();
	
	public static byte[] modifyLoc;
	
	public static float freeViewLimitSquared;
	
	public static float camDropCycle;
	public static float camDropAmpl;
	
	public static float camDropImpact;
	
	public static final Registry< BuildableLoader< ? extends IRenderer > >
		MODEL_LOADERS = new Registry<>();
	
	/**
	 * For those require mesh load
	 */
	final LinkedList< IRequireMeshLoad > meshLoadSubscribers = new LinkedList<>();
	
	final HashMap< String, IRenderer > rendererPool = new HashMap<>();
	
	final HashMap< String, Mesh > meshPool = new HashMap<>();
	
	/**
	 * Where key binds will be saved to
	 */
	File keyBindsFile;
	
	/**
	 * Buffered textures
	 * 
	 * TODO: clear this maybe?
	 */
	private final HashMap< String, ResourceLocation > texturePool = new HashMap<>();
	
	private MCWBClient() { }
	
	@Override
	public void preLoad()
	{
		// Check OpenGL compatibility
		if( !GLContext.getCapabilities().OpenGL30 )
			throw new RuntimeException( I18n.format( "mcwb.opengl_version_too_low" ) );
		
		// Do prepare work
		super.preLoad();
		
		// Register model loaders
		MODEL_LOADERS.regis( GunPartRenderer.LOADER );
		MODEL_LOADERS.regis( GunRenderer.LOADER );
		
		// Register default textures
		this.texturePool.put( Renderer.TEXTURE_RED.getPath(), Renderer.TEXTURE_RED );
		this.texturePool.put( Renderer.TEXTURE_GREEN.getPath(), Renderer.TEXTURE_RED );
		this.texturePool.put( Renderer.TEXTURE_BLUE.getPath(), Renderer.TEXTURE_RED );
	}
	
	@Override
	public void load()
	{
		// Load key binds before the content load
		this.keyBindsFile = new File( GAME_DIR, "config/mcwb-keys.json" );
		if( !this.keyBindsFile.exists() )
		{
			try { this.keyBindsFile.createNewFile(); }
			catch( IOException e ) { this.except( e, "mcwb.error_creating_key_binds_file" ); }
			InputHandler.saveTo( this.keyBindsFile );
		}
		else InputHandler.readFrom( this.keyBindsFile );
		
		// Do load content packs
		super.load();
	}
	
	@Override
	public void regisMeshLoad( IRequireMeshLoad subscriber ) {
		this.meshLoadSubscribers.add( subscriber );
	}
	
	@Override
	public void regisResourceDomain( File resource )
	{
		super.regisResourceDomain( resource );
		
		// Register it as a resource pack to load textures and sounds
		// Mainly referenced Flan's Mod for this part
		final TreeMap< String, Object > descriptor = new TreeMap<>();
		descriptor.put( "modid", MODID );
		descriptor.put( "name", NAME + ":" + resource.getName() );
		descriptor.put( "version", "1" ); // TODO: from pack info maybe
		final FMLModContainer container = new FMLModContainer(
			MCWB.class.getName(),
			new ModCandidate(
				resource, resource,
				resource.isDirectory() ? ContainerType.DIR : ContainerType.JAR
			),
			descriptor
		);
		container.bindMetadata( MetadataCollection.from( null, "" ) );
		FMLClientHandler.instance().addModAsResource( container );
	}
	
	/**
	 * Load .json or .class renderer from the given path.
	 * 
	 * @param path Path of the renderer to load
	 * @param fallBackType Renderer type to use if "__type__" field does not exist in ".json" file
	 * @return {@code null} if required loader does not exist or an exception was thrown
	 * TODO: maybe a null object to avoid null pointer
	 */
	@Nullable
	public IRenderer loadRenderer( String path, String fallbackType, IContentProvider provider )
	{
		return this.rendererPool.computeIfAbsent( path, key -> {
			try
			{
				// Handle ".json" renderer
				if( key.endsWith( ".json" ) )
				{
					try(
						IResource res = MC.getResourceManager()
							.getResource( new MCWBResource( path ) )
					) {
						// Parse from input reader
						final JsonObject obj = GSON.fromJson(
							new InputStreamReader( res.getInputStream() ),
							JsonObject.class
						);
						final JsonElement eEntry = obj.get( "__type__" );
						
						// Try get required loader and load
						final String entry = eEntry != null
							? eEntry.getAsString().toLowerCase() : fallbackType;
						final BuildableLoader< ? extends IRenderer >
							loader = MODEL_LOADERS.get( entry );
						if( loader != null )
							return loader.parser.apply( obj ).build( path, provider );
						
						this.error( "mcwb.model_loader_not_found", path, entry );
					}
				}
				
				// Handle ".class" renderer
				// TODO: maybe also require to pass two arguments( merge with type load )
				else if( key.endsWith( ".class" ) )
					return ( IRenderer ) this.loadClass( key.substring( 0, key.length() - 6 ) )
						.getConstructor().newInstance();
				
				// Unknown renderer type
				else throw new RuntimeException( "Unsupported renderer file type" );
			}
			catch( Exception e ) { this.except( e, "mcwb.error_loading_renderer", key ); }
			return null;
		} );
	}
	
	@Override
	public IRenderer loadRenderer( String path, String fallBackType ) {
		return this.loadRenderer( path, fallBackType, this );
	}
	
	/**
	 * @param attrSetter Use this to set attribute when loading .obj model
	 * @return {@link Mesh#NONE} if any error has occurred
	 */
	@Override
	public Mesh loadMesh( String path, Function< Mesh.Builder, Mesh.Builder > attrSetter )
	{
		return this.meshPool.computeIfAbsent( path, key -> {
			try
			{
				// Handle .obj mesh
				if( key.endsWith( ".obj" ) )
					return attrSetter.apply( new ObjMeshBuilder().load( key ) ).quickBuild();
				
				// Handle .class mesh
				if( key.endsWith( ".class" ) )
					return ( Mesh ) this.loadClass( key.substring( 0, key.length() - 6 ) )
						.getConstructor().newInstance();
				
				// Unknown mesh type
				throw new RuntimeException( "Unsupported mesh file type" );
			}
			catch( Exception e ) { this.except( e, "mcwb.error_loading_mesh", key ); }
			return Mesh.NONE;
		} );
	}
	
	@Override
	public ResourceLocation loadTexture( String path ) {
		return this.texturePool.computeIfAbsent( path, key -> new MCWBResource( key ) );
	}
	
	@Override
	public boolean isClient() { return true; }
	
	@Override
	public void clientOnly( Runnable task ) { task.run(); }
	
	@Override
	public String format( String translateKey, Object... parameters ) {
		return I18n.format( translateKey, parameters );
	}
	
//	@Override
//	protected void setupSideDependentLoaders() { TYPE_LOADERS.regis( KeyBind.LOADER ); }
	
	@Override
	protected void reloadResources() // TODO: make this part more clear?
	{
		// Force resource reload to load those in domain of content packs
		// TODO: maybe check if is only mod based content pack
		FMLClientHandler.instance().refreshResources(
			VanillaResourceType.MODELS,
			VanillaResourceType.TEXTURES,
			VanillaResourceType.SOUNDS,
			VanillaResourceType.LANGUAGES
		);
	}
	
	@Override
	protected GsonBuilder newGsonBuilder()
	{
		final GsonBuilder builder = super.newGsonBuilder();
		
		final JsonDeserializer< ResourceLocation > TEXTURE_ADAPTER =
			( json, typeOfT, context ) -> this.loadTexture( json.getAsString() );
		builder.registerTypeAdapter( ResourceLocation.class, TEXTURE_ADAPTER );
		
		return builder;
	}
}
