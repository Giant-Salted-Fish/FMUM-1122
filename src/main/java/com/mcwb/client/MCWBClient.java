package com.mcwb.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GLContext;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mcwb.client.ammo.AmmoModel;
import com.mcwb.client.gun.CarGripModel;
import com.mcwb.client.gun.GripModel;
import com.mcwb.client.gun.GunModel;
import com.mcwb.client.gun.GunPartModel;
import com.mcwb.client.gun.MagModel;
import com.mcwb.client.gun.OpticSightModel;
import com.mcwb.client.input.InputHandler;
import com.mcwb.client.input.KeyBind;
import com.mcwb.client.item.ItemModel;
import com.mcwb.client.render.Model;
import com.mcwb.common.MCWB;
import com.mcwb.common.MCWBResource;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.load.IContentProvider;
import com.mcwb.common.load.IMeshLoadSubscriber;
import com.mcwb.common.meta.Registry;
import com.mcwb.util.Animation;
import com.mcwb.util.BoneAnimation;
import com.mcwb.util.Mat4f;
import com.mcwb.util.Mesh;
import com.mcwb.util.ObjMeshBuilder;
import com.mcwb.util.Quat4f;
import com.mcwb.util.Vec3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResource;
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

@SideOnly( Side.CLIENT )
public final class MCWBClient extends MCWB
	implements IAutowirePlayerChat, IAutowireBindTexture, IAutowireSmoother
{
	/// *** For easy referencing *** ///
	public static final Minecraft MC = Minecraft.getMinecraft();
	public static final GameSettings SETTINGS = MC.gameSettings;
	
	public static final MCWBClient MOD = new MCWBClient();
	
	public static final Registry< BuildableLoader< ? > > MODEL_LOADERS = new Registry<>();
	
	// TODO: mesh loaders? to support other types of model
	
	public static final String MODIFY_INDICATOR = "modify_indicator";
	
	public static byte[] modifyLoc;
	
	public static float freeViewLimitSquared;
	
	public static float camDropCycle;
	public static float camDropAmpl;
	
	public static float camDropImpact;
	
	final LinkedList< IMeshLoadSubscriber > meshLoadSubscribers = new LinkedList<>();
	
	final HashMap< String, Object > modelPool = new HashMap<>();
	
	final HashMap< String, Mesh > meshPool = new HashMap<>();
	
	/**
	 * Where key binds will be saved to
	 */
	File keyBindsFile;
	
	/**
	 * Buffered textures
	 * 
	 * TODO: clear pools after use maybe?
	 */
	private final HashMap< String, ResourceLocation > texturePool = new HashMap<>();
	
	private final HashMap< String, Animation > animationPool = new HashMap<>();
	
	private MCWBClient() { }
	
	@Override
	public void preLoad()
	{
		// Check OpenGL support
		if( !GLContext.getCapabilities().OpenGL30 )
			throw new RuntimeException( I18n.format( "mcwb.opengl_version_too_low" ) );
		
		final Framebuffer framebuffer = MC.getFramebuffer();
		if( !framebuffer.isStencilEnabled() && !framebuffer.enableStencil() )
			throw new RuntimeException( I18n.format( "mcwb.stencil_not_supported" ) );
		
		// Do prepare load
		super.preLoad();
		
		// Register model loaders
		MODEL_LOADERS.regis( GunPartModel.LOADER );
		MODEL_LOADERS.regis( GunModel.LOADER );
		MODEL_LOADERS.regis( MagModel.LOADER );
		MODEL_LOADERS.regis( GripModel.LOADER );
		MODEL_LOADERS.regis( CarGripModel.LOADER );
		MODEL_LOADERS.regis( OpticSightModel.LOADER );
		MODEL_LOADERS.regis( AmmoModel.LOADER );
		
		// Register default textures
		this.texturePool.put( Model.TEXTURE_RED.getPath(), Model.TEXTURE_RED );
		this.texturePool.put( Model.TEXTURE_GREEN.getPath(), Model.TEXTURE_RED );
		this.texturePool.put( Model.TEXTURE_BLUE.getPath(), Model.TEXTURE_RED );
		this.texturePool.put( ItemModel.TEXTURE_STEVE.getPath(), ItemModel.TEXTURE_STEVE );
		this.texturePool.put( ItemModel.TEXTURE_ALEX.getPath(), ItemModel.TEXTURE_ALEX );
		
		// The default NONE mesh and animation
		this.meshPool.put( null, Mesh.NONE );
		this.animationPool.put( null, Animation.NONE );
	}
	
	@Override
	public void load()
	{
		// Load key binds before the content load
		this.keyBindsFile = new File( this.gameDir, "config/mcwb-keys.json" );
		if( !this.keyBindsFile.exists() )
		{
			try { this.keyBindsFile.createNewFile(); }
			catch( IOException e ) { this.except( e, "mcwb.error_creating_key_binds_file" ); }
			InputHandler.saveTo( this.keyBindsFile );
		}
		else InputHandler.readFrom( this.keyBindsFile );
		
		// Construct a default indicator
		final JsonObject indicator = new JsonObject();
		indicator.addProperty( "creativeTab", MCWB.HIDE_TAB.name() );
		indicator.addProperty( "model", "models/modify_indicator.json" );
		indicator.addProperty( "texture", "textures/0x00ff00.png" );
		TYPE_LOADERS.get( "gun_part" ).parser.apply( indicator ).build( MODIFY_INDICATOR, this );
		
		// Do load content packs!
		super.load();
	}
	
	@Override
	public void regis( IMeshLoadSubscriber subscriber ) {
		this.meshLoadSubscribers.add( subscriber );
	}
	
	@Override
	public void addResourceDomain( File resource )
	{
		super.addResourceDomain( resource );
		
		// Register it as a resource pack to load textures and sounds
		// Reference: Flan's Mod content pack load
		final TreeMap< String, Object > descriptor = new TreeMap<>();
		descriptor.put( "modid", ID );
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
	 * @param path Path of the model to load
	 * @param fallBackType Model type to use if "__type__" field does not exist in ".json" file
	 * @return {@code null} if required loader does not exist or an exception was thrown
	 * TODO: maybe a null object to avoid null pointer
	 */
	@Nullable
	public Object loadModel( String path, String fallbackType, IContentProvider provider )
	{
		return this.modelPool.computeIfAbsent( path, key -> {
			try
			{
				if( key.endsWith( ".json" ) )
				{
					final MCWBResource identifier = new MCWBResource( key );
					try( IResource res = MC.getResourceManager().getResource( identifier ) )
					{
						final InputStreamReader in = new InputStreamReader( res.getInputStream() );
						final JsonObject obj = GSON.fromJson( in, JsonObject.class );
						
						// Try get required loader and load
						final JsonElement type = obj.get( "__type__" );
						final String entry = type != null
							? type.getAsString().toLowerCase() : fallbackType;
						final BuildableLoader< ? > loader = MODEL_LOADERS.get( entry );
						if( loader != null )
							return loader.parser.apply( obj ).build( key, provider );
						
						throw new RuntimeException(
							this.format( "mcwb.model_loader_not_found", key, entry )
						);
					}
				}
				else if( key.endsWith( ".class" ) )
				{
					return this.loadClass( key.substring( 0, key.length() - 6 ) )
						.getConstructor( String.class, IContentProvider.class )
							.newInstance( key, provider );
				}
				
				// Unknown renderer type
				else throw new RuntimeException( "Unsupported renderer file type" ); // TODO: format this?
			}
			catch( Exception e ) { this.except( e, "mcwb.error_loading_renderer", key ); }
			return null;
		} );
	}
	
	@Override
	public Object loadModel( String path, String fallBackType ) {
		return this.loadModel( path, fallBackType, this );
	}
	
	/**
	 * @param processor Use this to process .obj model before compiling it to mesh
	 * @return {@link Mesh#NONE} if any error has occurred
	 */
	@Override
	public Mesh loadMesh( String path, Function< Mesh.Builder, Mesh.Builder > processor )
	{
		return this.meshPool.computeIfAbsent( path, key -> {
			try
			{
				// TODO: switch by suffix to support other types of models?
				if( key.endsWith( ".obj" ) )
					return processor.apply( new ObjMeshBuilder().load( key ) ).quickBuild();
//				if( key.endsWith( ".class" ) )
//					return ( Mesh ) this.loadClass( key.substring( 0, key.length() - 6 ) )
//						.getConstructor().newInstance();
				
				// Unknown mesh type
				throw new RuntimeException( "Unsupported model file type" ); // TODO: format this?
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
	public Animation loadAnimation( String path )
	{
		return this.animationPool.computeIfAbsent( path, key -> {
			try
			{
				if( key.endsWith( ".json" ) )
				{
					// For animation exported from blockbench
					final MCWBResource identifier = new MCWBResource( key );
					try( IResource res = MC.getResourceManager().getResource( identifier ) )
					{
						final InputStreamReader in = new InputStreamReader( res.getInputStream() );
						final BBAnimationJson json = GSON.fromJson( in, BBAnimationJson.class );
						
						final Animation ani = new Animation();
						// <Bone> : <Its parent>
						final LinkedList< Entry< String, String > >
							bone2Parent = new LinkedList<>();
						
						final float timeFactor = 1F / json.animation_length;
						final float scale = json.positionScale;
						final Mat4f mat = Mat4f.locate();
						json.bones.forEach( ( channel, bbBone ) -> {
							final BoneAnimation bone = new BoneAnimation();
							bbBone.position.forEach( ( time, pos ) -> {
//								pos.z = -pos.z; // TODO: remove this
								pos.scale( scale );
								bone.pos.put( time * timeFactor, pos );
							} );
							bbBone.rotation.forEach( ( time, rot ) -> {
								mat.setIdentity();
//								mat.rotateZ( -rot.z );
//								mat.rotateY( -rot.y );
								mat.rotateZ( rot.z );
								mat.rotateY( rot.y );
								mat.rotateX( rot.x );
								bone.rot.put( time * timeFactor, new Quat4f( mat ) );
							} );
							bbBone.alpha.forEach(
								( time, alpha ) -> bone.alpha.put( time * timeFactor, alpha )
							);
							bone.addGuard();
							
							ani.channels.put( channel, bone );
							bone2Parent.add( new SimpleEntry<>( channel, bbBone.parent ) );
						} );
						mat.release();
						
						// Setup bone & parent dependency
						bone2Parent.forEach( e -> {
							final String parent = e.getValue();
							final BoneAnimation bone = ani.channels.get( e.getKey() );
							if( parent == null ) ani.rootBones.add( bone );
							else ani.channels.get( parent ).children.add( bone );
						} );
						return ani;
					}
				}
				
				if( key.endsWith( ".class" ) );
					// TODO: class animation?
				
				throw new RuntimeException( "Unsupported animation file type" );
			}
			catch( Exception e ) { this.except( e, "mcwb.error_loading_animation", key ); }
			return Animation.NONE;
		} );
	}
	
	@Override
	public boolean isClient() { return true; }
	
	@Override
	public void clientOnly( Runnable task ) { task.run(); }
	
	@Override
	public String format( String translateKey, Object... parameters ) {
		return I18n.format( translateKey, parameters );
	}
	
	@Override
	protected void regisSideDependentLoaders() { TYPE_LOADERS.regis( KeyBind.LOADER ); }
	
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
	protected GsonBuilder gsonBuilder()
	{
		final GsonBuilder builder = super.gsonBuilder();
		
		final JsonDeserializer< ResourceLocation > TEXTURE_ADAPTER =
			( json, typeOfT, context ) -> this.loadTexture( json.getAsString() );
		builder.registerTypeAdapter( ResourceLocation.class, TEXTURE_ADAPTER );
		
		return builder;
	}
	
	private static class BBAnimationJson
	{
//		boolean loop = false;
		float animation_length;
		Map< String, BBBoneJson > bones = Collections.emptyMap();
		
		/**
		 * Additional scale applied on animation upon load
		 */
		float positionScale = 1F;
	}
	
	private static class BBBoneJson
	{
		String parent;
		Map< Float, Vec3f > position = Collections.emptyMap();
		Map< Float, Float > alpha = Collections.emptyMap();
		
		// Notice that the euler rotation order of bb animation is actually xyz
		Map< Float, Vec3f > rotation = Collections.emptyMap();
	}
}
