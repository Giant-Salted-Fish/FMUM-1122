package com.fmum.client;

import com.fmum.client.ammo.JsonAmmoModel;
import com.fmum.client.camera.CameraAnimator;
import com.fmum.client.gun.JsonCarGripModel;
import com.fmum.client.gun.JsonGripModel;
import com.fmum.client.gun.JsonGunModel;
import com.fmum.client.gun.JsonGunPartModel;
import com.fmum.client.gun.JsonOpticalSightModel;
import com.fmum.client.input.InputHandler;
import com.fmum.client.input.JsonKeyBind;
import com.fmum.client.item.ItemModel;
import com.fmum.client.mag.JsonMagModel;
import com.fmum.client.player.PlayerPatchClient;
import com.fmum.client.render.Model;
import com.fmum.common.FMUM;
import com.fmum.common.FMUMResource;
import com.fmum.common.load.BuildableLoader;
import com.fmum.common.load.IContentProvider;
import com.fmum.common.load.IMeshLoadSubscriber;
import com.fmum.common.meta.Registry;
import com.fmum.util.Animation;
import com.fmum.util.BoneAnimation;
import com.fmum.util.Mat4f;
import com.fmum.util.Mesh;
import com.fmum.util.ObjMeshBuilder;
import com.fmum.util.Quat4f;
import com.fmum.util.Vec3f;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import org.lwjgl.opengl.GLContext;

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
import java.util.function.Supplier;

// 做第三人称玩家移动动画时可以考虑让双脚运动轨迹等于错相的半圆然后通过 ik 控制腿部移动
@SideOnly( Side.CLIENT )
public final class FMUMClient extends FMUM
	implements IAutowirePlayerChat, IAutowireBindTexture, IAutowireSmoother
{
	/// *** For easy referencing. *** ///
	public static final Minecraft MC = Minecraft.getMinecraft();
	public static final GameSettings SETTINGS = MC.gameSettings;
	
	public static final FMUMClient MOD = new FMUMClient();
	
	public static final Registry< BuildableLoader< ? > > MODEL_LOADERS = new Registry<>();
	
	// TODO: mesh loaders? to support other types of model
	
	public static byte[] modifyLoc;
	
	public static float freeViewLimitSquared;
	
	public static float camDropCycle;
	public static float camDropAmpl;
	
	public static float camDropImpact;
	
	final LinkedList< IMeshLoadSubscriber > meshLoadSubscribers = new LinkedList<>();
	
	final HashMap< String, Object > modelPool = new HashMap<>();
	
	final HashMap< String, Mesh > meshPool = new HashMap<>();
	
	/**
	 * Where key binds will be saved to.
	 */
	File keyBindsFile;
	
	/**
	 * Buffered textures.
	 * TODO: clear pools after use maybe?
	 */
	private final HashMap< String, ResourceLocation > texturePool = new HashMap<>();
	
	private final HashMap< String, Animation > animationPool = new HashMap<>();
	
	private FMUMClient() { }
	
	@Override
	public void preLoad()
	{
		// Check render capabilities.
		if ( !GLContext.getCapabilities().OpenGL30 ) {
			throw new RuntimeException( I18n.format( "fmum.opengl_version_too_low" ) );
		}
		
		final Framebuffer framebuffer = MC.getFramebuffer();
		if ( !framebuffer.isStencilEnabled() && !framebuffer.enableStencil() ) {
			throw new RuntimeException( I18n.format( "fmum.stencil_not_supported" ) );
		}
		
		// Call super for preload.
		super.preLoad();
		
		// Register model loaders.
		MODEL_LOADERS.put( "gun_part", JsonGunPartModel.LOADER );
		MODEL_LOADERS.put( "gun_parts", JsonGunPartModel.LOADER );
		MODEL_LOADERS.put( "gun", JsonGunModel.LOADER );
		MODEL_LOADERS.put( "guns", JsonGunModel.LOADER );
		MODEL_LOADERS.put( "mag", JsonMagModel.LOADER );
		MODEL_LOADERS.put( "mags", JsonMagModel.LOADER );
		MODEL_LOADERS.put( "grip", JsonGripModel.LOADER );
		MODEL_LOADERS.put( "grips", JsonGripModel.LOADER );
		MODEL_LOADERS.put( "car_grip", JsonCarGripModel.LOADER );
		MODEL_LOADERS.put( "car_grips", JsonCarGripModel.LOADER );
		MODEL_LOADERS.put( "optical_sight", JsonOpticalSightModel.LOADER );
		MODEL_LOADERS.put( "optical_sights", JsonOpticalSightModel.LOADER );
		MODEL_LOADERS.put( "ammo", JsonAmmoModel.LOADER );
		
		// Register default textures.
		this.texturePool.put( Model.TEXTURE_RED.getPath(), Model.TEXTURE_RED );
		this.texturePool.put( Model.TEXTURE_GREEN.getPath(), Model.TEXTURE_RED );
		this.texturePool.put( Model.TEXTURE_BLUE.getPath(), Model.TEXTURE_RED );
		this.texturePool.put( ItemModel.TEXTURE_STEVE.getPath(), ItemModel.TEXTURE_STEVE );
		this.texturePool.put( ItemModel.TEXTURE_ALEX.getPath(), ItemModel.TEXTURE_ALEX );
		
		// The default NONE mesh and animation.
		// Null usually is not a good choice but have to do it for animation pool.
		this.meshPool.put( "", Mesh.NONE );
	}
	
	@Override
	public void load()
	{
		// Load key binds before the content load.
		this.keyBindsFile = new File( this.gameDir, "config/fmum-keys.json" );
		if ( !this.keyBindsFile.exists() )
		{
			try { this.keyBindsFile.createNewFile(); }
			catch ( IOException e ) { this.logException( e, "fmum.error_creating_key_binds_file" ); }
			InputHandler.saveTo( this.keyBindsFile );
		}
		else { InputHandler.readFrom( this.keyBindsFile ); }
		
		// Do load content packs!
		super.load();
		
		PlayerPatchClient.updateMouseHelperStrategy( ModConfigClient.useFlanCompatibleMouseHelper );
	}
	
	@Override
	public void regisMeshLoadSubscriber( IMeshLoadSubscriber subscriber ) {
		this.meshLoadSubscribers.add( subscriber );
	}
	
	@Override
	public void addResourceDomain( File resource )
	{
		super.addResourceDomain( resource );
		
		// Register it as a resource pack to load textures and sounds.
		// Reference: Flan's Mod content pack load.
		final TreeMap< String, Object > descriptor = new TreeMap<>();
		descriptor.put( "modid", MODID );
		descriptor.put( "name", MOD_NAME + ":" + resource.getName() );
		descriptor.put( "version", "1" ); // TODO: from pack info maybe
		final FMLModContainer container = new FMLModContainer(
			FMUM.class.getName(),
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
	public Object loadModel( String path, String fallbackModelType, Supplier< ? > fallbackModel ) {
		return this.loadModel( path, fallbackModelType, fallbackModel, this );
	}
	
	public Object loadModel(
		String path,
		String fallbackModelType,
		Supplier< ? > fallbackModel,
		IContentProvider provider
	) {
		return this.modelPool.computeIfAbsent( path, key -> {
			try
			{
				if ( key.endsWith( ".json" ) )
				{
					final FMUMResource identifier = new FMUMResource( key );
					try ( IResource res = MC.getResourceManager().getResource( identifier ) )
					{
						final InputStreamReader in = new InputStreamReader( res.getInputStream() );
						final JsonObject obj = GSON.fromJson( in, JsonObject.class );
						
						// Try get required loader and load.
						final JsonElement type = obj.get( "__type__" );
						final String entry = type != null
							? type.getAsString().toLowerCase() : fallbackModelType;
						final BuildableLoader< ? > loader = MODEL_LOADERS.get( entry );
						final boolean loadDoesNotExist = loader == null;
						if ( loadDoesNotExist )
						{
							final String formatter = "fmum.model_loader_not_found";
							throw new RuntimeException( this.format( formatter, key, entry ) );
						}
						
						return loader.parser.apply( obj ).build( key, provider );
					}
				}
				else if ( key.endsWith( ".class" ) )
				{
					final String classPath = key.substring( 0, key.length() - ".class".length() );
					return this.loadClass( classPath )
						.getConstructor( String.class, IContentProvider.class )
							.newInstance( key, provider );
				}
				
				// Unknown renderer type.
				else { throw new RuntimeException( "Unsupported renderer file type" ); } // TODO: format this?
			}
			catch ( Exception e ) { this.logException( e, "fmum.error_loading_renderer", key ); }
			return fallbackModel.get();
		} );
	}
	
	/**
	 * @param processor Use this to process .obj model before compiling it to mesh.
	 * @return {@link Mesh#NONE} if any error has occurred.
	 */
	@Override
	public Mesh loadMesh( String path, Function< Mesh.Builder, Mesh.Builder > processor )
	{
		return this.meshPool.computeIfAbsent( path, key -> {
			try
			{
				// TODO: switch by suffix to support other types of models?
				if ( key.endsWith( ".obj" ) ) {
					return processor.apply( new ObjMeshBuilder().load( key ) ).quickBuild();
				}
				
//				if ( key.endsWith( ".class" ) ) {
//					return ( Mesh ) this.loadClass( key.substring( 0, key.length() - 6 ) )
//						.getConstructor().newInstance();
//				}
				
				// Unknown mesh type.
				throw new RuntimeException( "Unsupported model file type" ); // TODO: format this?
			}
			catch ( Exception e ) { this.logException( e, "fmum.error_loading_mesh", key ); }
			return Mesh.NONE;
		} );
	}
	
	@Override
	public ResourceLocation loadTexture( String path ) {
		return this.texturePool.computeIfAbsent( path, FMUMResource::new );
	}
	
	@Override
	public Animation loadAnimation( String path )
	{
		return this.animationPool.computeIfAbsent( path, key -> {
			try
			{
				if ( key.endsWith( ".json" ) )
				{
					// For animation exported from BlockBench.
					final FMUMResource identifier = new FMUMResource( key );
					try ( IResource res = MC.getResourceManager().getResource( identifier ) )
					{
						final InputStreamReader in = new InputStreamReader( res.getInputStream() );
						final BBAnimationJson json = GSON.fromJson( in, BBAnimationJson.class );
						
						final Animation ani = new Animation();
						final LinkedList< Entry< String, String > >
							bone2Parent = new LinkedList<>();
						
						final float timeFactor = 1F / json.animation_length;
						final float scale = json.positionScale;
						final Mat4f mat = Mat4f.locate();
						
						// Camera is a bit of special, process it independently.
						final String cameraChannel = CameraAnimator.ANIMATION_CAHNNEL;
						final BBBoneJson cameraBone = json.bones.remove( cameraChannel );
						if ( cameraBone != null )
						{
							final BoneAnimation bone = new BoneAnimation();
							cameraBone.position.forEach( ( time, pos ) -> {
								pos.x = -pos.x;
								pos.y = -pos.y;
								pos.scale( scale );
								bone.pos.put( time * timeFactor, pos );
							} );
							cameraBone.rotation.forEach( ( time, rot ) -> {
								mat.setIdentity();
								mat.rotateZ( rot.z );
								mat.rotateY( rot.y );
								mat.rotateX( -rot.x );
								bone.rot.put( time * timeFactor, new Quat4f( mat ) );
							} );
							bone.addGuard();
							ani.channels.put( cameraChannel, bone );
							bone2Parent.add( new SimpleEntry<>( cameraChannel, cameraBone.parent ) );
						}
						
						// For other bones.
						json.bones.forEach( ( channel, bbBone ) -> {
							final BoneAnimation bone = new BoneAnimation();
							bbBone.position.forEach( ( time, pos ) -> {
								pos.x = -pos.x;
								pos.scale( scale );
								bone.pos.put( time * timeFactor, pos );
							} );
							bbBone.rotation.forEach( ( time, rot ) -> {
								mat.setIdentity();
								mat.rotateZ( rot.z );
								mat.rotateY( -rot.y );
								mat.rotateX( -rot.x );
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
						
						// Setup bone & parent dependency.
						bone2Parent.forEach( e -> {
							final String parent = e.getValue();
							final BoneAnimation bone = ani.channels.get( e.getKey() );
							if ( parent == null ) { ani.rootBones.add( bone ); }
							else { ani.channels.get( parent ).addChild( bone ); }
						} );
						return ani;
					}
				}
				
//				if ( key.endsWith( ".class" ) )
//				{
//					// TODO: class animation?
//				}
				
				throw new RuntimeException( "Unsupported animation file type" );
			}
			catch ( Exception e ) { this.logException( e, "fmum.error_loading_animation", key ); }
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
	protected void regisSideDependentLoaders()
	{
		TYPE_LOADERS.put( "key_binding", JsonKeyBind.LOADER );
		TYPE_LOADERS.put( "key_bindings", JsonKeyBind.LOADER );
	}
	
	@Override
	protected void reloadResources() // TODO: make this part more clear?
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
	protected GsonBuilder gsonBuilder()
	{
		final GsonBuilder builder = super.gsonBuilder();
		
		final JsonDeserializer< ResourceLocation > textureAdapter =
			( json, typeOfT, context ) -> this.loadTexture( json.getAsString() );
		builder.registerTypeAdapter( ResourceLocation.class, textureAdapter );
		
		final JsonDeserializer< Animation > animationAdapter =
			( json, typeOfT, context ) -> this.loadAnimation( json.getAsString() );
		builder.registerTypeAdapter( Animation.class, animationAdapter );
		
		return builder;
	}
	
	private static class BBAnimationJson
	{
//		boolean loop = false;
		float animation_length = 1F;
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
