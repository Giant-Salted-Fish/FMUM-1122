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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mcwb.client.gun.GunModel;
import com.mcwb.client.gun.GunPartModel;
import com.mcwb.client.input.InputHandler;
import com.mcwb.client.input.KeyBind;
import com.mcwb.client.player.PlayerPatchClient;
import com.mcwb.client.render.IModel;
import com.mcwb.common.MCWB;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.load.IRequireMeshLoad;
import com.mcwb.common.load.TexturedMeta;
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
	
	public static final Registry< BuildableLoader< ? extends IModel > >
		MODEL_LOADERS = new Registry<>();
	
	public static int modifyLocLen;
	
	public static float freeViewLimitSquared;
	
	public static float camDropCycle;
	public static float camDropAmpl;
	
	public static float camDropImpact;
	
	/**
	 * For those require mesh load
	 */
	final LinkedList< IRequireMeshLoad > meshLoadSubscribers = new LinkedList<>();
	
	final HashMap< String, IModel > modelPool = new HashMap<>();
	
	final HashMap< String, Mesh > meshPool = new HashMap<>();
	
	/**
	 * Where key binds will be saved to
	 */
	File keyBindsFile;
	
	/**
	 * Buffered textures
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
		
		// Register capability
		this.regisCapability( PlayerPatchClient.class );
		
		// Register model loaders
		MODEL_LOADERS.regis( GunPartModel.LOADER );
		MODEL_LOADERS.regis( GunModel.LOADER );
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
	
	@Override
	public ResourceLocation loadTexture( String path ) {
		return this.texturePool.computeIfAbsent( path, key -> new ResourceLocation( MODID, key ) );
	}
	
	/**
	 * Load .json model or .class model from the given path.
	 * 
	 * @param path Path of the model to load
	 * @param fallBackType Model type to use if "__type__" field does not exist in ".json" model
	 * @return {@code null} if required loader does not exist or an exception was thrown
	 * TODO: maybe a null object to avoid null pointer
	 */
	@Nullable
	public IModel loadModel( String path, String fallbackType, IContentProvider provider )
	{
		return this.modelPool.computeIfAbsent( path, key -> {
			try
			{
				// Handle ".json" model
				if( key.endsWith( ".json" ) )
				{
					try(
						IResource res = MC.getResourceManager()
							.getResource( new ResourceLocation( MODID, path ) )
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
						final BuildableLoader< ? extends IModel >
							loader = MODEL_LOADERS.get( entry );
						if( loader != null )
							return loader.parser.apply( obj ).build( path, provider );
						
						this.error( "mcwb.model_loader_not_found", path, entry );
					}
				}
				
				// Handle ".class" model
				// TODO: maybe also require to pass two arguments( merge with type load )
				else if( key.endsWith( ".class" ) )
					return ( IModel ) this.loadClass( key.substring( 0, key.length() - 6 ) )
						.getConstructor().newInstance();
				
				// Unknown model type
				else throw new RuntimeException( "Unsupported model file type" );
			}
			catch( Exception e ) { this.except( e, "mcwb.error_loading_model", key ); }
			return null;
		} );
	}
	
	/**
	 * @param attrSetter Use this to set attribute when loading .obj model
	 */
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
	public boolean isClient() { return true; }
	
//	@Override
//	public < T > T sideOnly( Supplier< ? extends T > common, Supplier< ? extends T > client ) {
//		return client.get();
//	}
	
	@Override
	public String format( String translateKey, Object... parameters ) {
		return I18n.format( translateKey, parameters );
	}
	
	@Override
	protected void setupSideDependentLoaders() { TYPE_LOADERS.regis( KeyBind.LOADER ); }
	
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
		builder.registerTypeAdapter( ResourceLocation.class, TexturedMeta.TEXTURE_ADAPTER );
//		builder.registerTypeAdapter( RenderableItem.class, GunPartJson.GUN_PART_MODEL_ADAPTER );
		return builder;
	}
}
