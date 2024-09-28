package com.fmum;

import com.fmum.ammo.AmmoType;
import com.fmum.animation.SoundFrame;
import com.fmum.gun.GunType;
import com.fmum.gunpart.GunPartType;
import com.fmum.input.JsonKeyBinding;
import com.fmum.item.IItem;
import com.fmum.item.ItemCategory;
import com.fmum.item.JsonItemStack;
import com.fmum.load.FolderPack;
import com.fmum.load.IContentLoader;
import com.fmum.load.ILoadContext;
import com.fmum.load.IMeshLoadContext;
import com.fmum.load.IPackFactory;
import com.fmum.load.IPackInfo;
import com.fmum.load.IPackLoadCallback;
import com.fmum.load.IPostLoadContext;
import com.fmum.load.IPreLoadContext;
import com.fmum.mag.MagType;
import com.fmum.module.IModule;
import com.fmum.paintjob.IPaintjob;
import com.fmum.paintjob.JsonPaintjob;
import com.fmum.paintjob.Paintjob;
import com.fmum.player.PlayerPatch;
import com.fmum.render.AnimatedModel;
import com.fmum.render.ModelPath;
import com.fmum.render.Texture;
import com.fmum.tab.JsonCreativeTab;
import com.google.common.collect.Iterators;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mojang.realmsclient.util.Pair;
import gsf.util.animation.Animation;
import gsf.util.animation.IAnimation;
import gsf.util.math.AxisAngle4f;
import gsf.util.math.Quat4f;
import gsf.util.math.Vec3f;
import gsf.util.render.Mesh;
import gsf.util.render.MeshBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

final class PackLoader
{
	private final LinkedList< Pair< IPackInfo, IPackLoadCallback > > packs = new LinkedList<>();
	
	private final Registry< IContentLoader< ? > > content_loaders = new Registry<>();
	
	private Gson gson;
	
	private final HashMap< ResourceLocation, SoundEvent > sound_pool = new HashMap<>();
	
	@SideOnly( Side.CLIENT )
	private HashMap< ModelPath, Optional< Mesh > > mesh_pool; {
		FMUM.SIDE.runIfClient( () -> {
			this.mesh_pool = new HashMap<>();
			this.mesh_pool.put( ModelPath.NONE, Optional.of( Mesh.NONE ) );
		} );
	}
	
	
	PackLoader( ModContainer fmum_container, List< ModContainer > active_mod_list )
	{
		// Collect loadable content packs. Do not forget the FMUM core pack.
		this.packs.add( new IPackFactory() { }.create( fmum_container ) );
		
//		int a = Dev.DEV_MARK;
//		this.packs.add( this.__createDevPack() );
		
		// Check mods folder for content packs.
		active_mod_list.stream()
			.filter( container -> container.getRequirements().stream().anyMatch( req -> req.getLabel().equals( FMUM.MODID ) ) )
			.map( container -> ( ( IPackFactory ) container.getMod() ).create( container ) )
			.forEachOrdered( this.packs::add );
		FMUM.LOGGER.info( "fmum.total_packs_found", packs.size() );
	}
	
	private Pair< IPackInfo, IPackLoadCallback > __createDevPack()
	{
		final IPackInfo info = new IPackInfo() {
			@Override
			public String getName() {
				return "dev_only";
			}
			
			@Override
			public String getAuthor() {
				return "gsf";
			}
			
			@Override
			public String getNamespace() {
				return "fmum";
			}
			
			@Override
			public String getSourceName() {
				return "dev_only";
			}
		};
		
		final File dir = new File( Minecraft.getMinecraft().gameDir, "dev_pack" );
		return Pair.of( info, new FolderPack( dir, info ) );
	}
	
	void _preLoadPacks()
	{
		final GsonBuilder gson_builder = new GsonBuilder();
		gson_builder.setLenient();
		gson_builder.setPrettyPrinting();
		gson_builder.excludeFieldsWithoutExposeAnnotation();
		
		// Prepare pack load.
		final IPreLoadContext pre_load_ctx = new IPreLoadContext() {
			@Override
			public void regisGsonDeserializer( Type type, JsonDeserializer< ? > adapter ) {
				gson_builder.registerTypeAdapter( type, adapter );
			}
			
			@Override
			public void regisContentLoader( String entry, IContentLoader< ? > loader ) {
				PackLoader.this.content_loaders.regis( entry, loader );
			}
		};
		
		this.__regisCapability();
		this.__regisTypeAdapter( pre_load_ctx );
		this.__regisContentLoader( pre_load_ctx );
		this.packs.forEach( pair -> {
			final IPackInfo info = pair.first();
			FMUM.LOGGER.info( "fmum.pre_load_pack", info.getName() );
			pair.second().onPreLoad( pre_load_ctx );
		} );
		
		this.gson = gson_builder.create();
	}
	
	private void __regisCapability()
	{
		this.__regisCapability( PlayerPatch.class );
		this.__regisCapability( IItem.class );  // See IItem#CAPABILITY.
		this.__regisCapability( IModule.class );
	}
	
	private void __regisTypeAdapter( IPreLoadContext ctx )
	{
		ctx.regisGsonDeserializer(
			( new TypeToken< Supplier< Optional< ItemStack > > >() { } ).getType(),
			( json, type, context ) -> {
				final JsonItemStack factory = (
					json.isJsonPrimitive()
					? new JsonItemStack( json.getAsString() )
					: context.deserialize( json, JsonItemStack.class )
				);
				return ( Supplier< Optional< ItemStack > > ) factory::create;
			}
		);
		
		ctx.regisGsonDeserializer(
			ItemCategory.class,
			( json, type, context ) -> ItemCategory.parse( json.getAsString() )
		);
		
		ctx.regisGsonDeserializer(
			new TypeToken< Predicate< ItemCategory > >() { }.getType(),
			( json, type, context ) -> {
				final ItemCategory[] whitelist;
				final ItemCategory[] blacklist;
				if ( json.isJsonObject() )
				{
					final Map< Boolean, List< ItemCategory > > lists = (
						json.getAsJsonObject().entrySet().stream()
						.map( p -> Pair.of( ItemCategory.parse( p.getKey() ), p.getValue().getAsBoolean() ) )
						.collect( Collectors.partitioningBy(
							Pair::second,
							Collectors.mapping( Pair::first, Collectors.toList() )
						) )
					);
					whitelist = lists.get( true ).toArray( new ItemCategory[ 0 ] );
					blacklist = lists.get( false ).toArray( new ItemCategory[ 0 ] );
				}
				else
				{
					final Iterator< JsonElement > elements = (
						json.isJsonArray()
						? json.getAsJsonArray().iterator()
						: Iterators.forArray( json )
					);
					final Iterator< ItemCategory > categories = Iterators.transform(
						elements,
						elem -> context.deserialize( elem, ItemCategory.class )
					);
					whitelist = Iterators.toArray( categories, ItemCategory.class );
					blacklist = new ItemCategory[ 0 ];
				}
				return ItemCategory.createFilterBy( whitelist, blacklist );
			}
		);
		
		ctx.regisGsonDeserializer(
			IPaintjob.class,
			( json, type, context ) -> context.deserialize( json, Paintjob.class )
		);
		
		ctx.regisGsonDeserializer(
			Vec3f.class,
			( json, type, context ) -> {
				final JsonArray arr = json.getAsJsonArray();
				return new Vec3f(
					arr.get( 0 ).getAsFloat(),
					arr.get( 1 ).getAsFloat(),
					arr.get( 2 ).getAsFloat()
				);
			}
		);
		
		ctx.regisGsonDeserializer(
			AxisAngle4f.class,
			( json, type, context ) -> {
				final JsonArray arr = json.getAsJsonArray();
				final float f0 = arr.get( 0 ).getAsFloat();
				final float f1 = arr.get( 1 ).getAsFloat();
				final float f2 = arr.get( 2 ).getAsFloat();
				return (
					arr.size() < 4
					? AxisAngle4f.ofEulerRot( f0, f1, f2 )
					: new AxisAngle4f( f0, f1, f2, arr.get( 3 ).getAsFloat() )
				);
			}
		);
		
		ctx.regisGsonDeserializer(
			Quat4f.class,
			( json, type, context ) -> {
				final JsonArray arr = json.getAsJsonArray();
				if ( arr.size() < 4 )
				{
					return Quat4f.ofEulerRot(
						arr.get( 0 ).getAsFloat(),
						arr.get( 1 ).getAsFloat(),
						arr.get( 2 ).getAsFloat()
					);
				}
				
				final AxisAngle4f aa = context.deserialize( json, AxisAngle4f.class );
				return Quat4f.ofAxisAngle( aa );
			}
		);
		
		ctx.regisGsonDeserializer(
			IAnimation.class,
			( json, type, context ) -> {
				final String resource = json.getAsString();
				if ( resource.endsWith( ".json" ) )
				{
					// Load animation exported from BlockBench.
					final ResourceLocation location = new ResourceLocation( resource );
					final IResourceManager res_mgr = Minecraft.getMinecraft().getResourceManager();
					try ( IResource res = res_mgr.getResource( location ) )
					{
						final InputStreamReader in = new InputStreamReader( res.getInputStream() );
						final JsonObject obj = gson.fromJson( in, JsonObject.class );
						return Animation.fromBBJson( obj, gson );
					}
					catch ( IOException e )
					{
						// TODO
					}
				}
				return IAnimation.EMPTY;
			}
		);
		
		ctx.regisGsonDeserializer(
			SoundEvent.class,
			( json, type, context ) -> {
				final ResourceLocation loc = new ResourceLocation( json.getAsString() );
				return this.sound_pool.computeIfAbsent( loc, l -> new SoundEvent( l ).setRegistryName( l ) );
			}
		);
		
		ctx.regisGsonDeserializer(
			SoundFrame[].class,
			( json, type, context ) -> (
				json.getAsJsonObject().entrySet().stream()
				.map( e -> {
					final float time = Float.parseFloat( e.getKey() );
					final SoundEvent sound = context.deserialize( e.getValue(), SoundEvent.class );
					return new SoundFrame( time, sound );
				} )
				.sorted( Comparator.comparing( skf -> skf.time ) )  // TODO: Sort maybe unnecessary.
				.toArray( SoundFrame[]::new )
			)
		);
		
		FMUM.SIDE.runIfClient( () -> {
			// TODO: maybe pool it?
			ctx.regisGsonDeserializer(
				Texture.class,
				( json, type, context ) -> {
					final String identifier = json.getAsString();
					return new Texture( identifier );
				}
			);
			
			ctx.regisGsonDeserializer(
				ModelPath.class,
				( json, type, context ) -> {
					final String identifier = json.getAsString();
					return new ModelPath( identifier );
				}
			);
			
			ctx.regisGsonDeserializer(
				AnimatedModel[].class,
				( json, type, context ) -> {
					if ( json.isJsonPrimitive() )
					{
						// One element array.
						final ModelPath path = new ModelPath( json.getAsString() );
						final AnimatedModel model = new AnimatedModel( path );
						return new AnimatedModel[] { model };
					}
					
					final Iterator< AnimatedModel > models = Iterators.transform(
						json.getAsJsonArray().iterator(),
						elem -> context.deserialize( elem, AnimatedModel.class )
					);
					return Iterators.toArray( models, AnimatedModel.class );
				}
			);
			
			ctx.regisGsonDeserializer(
				IKeyConflictContext.class,
				( json, type, context ) -> KeyConflictContext.valueOf( json.getAsString() )
			);
		} );
	}
	
	private void __regisContentLoader( IPreLoadContext ctx )
	{
		ctx.regisContentLoader( "creative_tab", JsonCreativeTab.LOADER );
		ctx.regisContentLoader( "paintjob", JsonPaintjob.LOADER );
		ctx.regisContentLoader( "ammo", AmmoType.LOADER );
		ctx.regisContentLoader( "gun_part", GunPartType.LOADER );
		ctx.regisContentLoader( "gun", GunType.LOADER );
		ctx.regisContentLoader( "mag", MagType.LOADER );
		
		FMUM.SIDE.runIfClient( () -> {
			// Key bindings are client side only.
			ctx.regisContentLoader( "key_binding", JsonKeyBinding.LOADER );
		} );
	}
	
	void _loadPacks()
	{
		final ILoadContext load_ctx = new ILoadContext() {
			@Override
			public Gson getGson() {
				return PackLoader.this.gson;
			}
			
			@Override
			public Optional< IContentLoader< ? > > lookupContentLoader( String entry ) {
				return PackLoader.this.content_loaders.lookup( entry );
			}
		};
		
		this.packs.forEach( pair -> {
			final IPackInfo info = pair.first();
			FMUM.LOGGER.info( "fmum.load_pack", info.getName() );
			pair.second().onLoad( load_ctx );
		} );
	}
	
	void _postLoadPacks()
	{
		final IPostLoadContext post_load_ctx = new IPostLoadContext() {
			private CreativeTabs fallback_tab = null;
			
			@Override
			public CreativeTabs getFallbackCreativeTab()
			{
				// Only create default tab if it is actually used.
				if ( this.fallback_tab == null )
				{
					this.fallback_tab = new CreativeTabs( FMUM.MODID ) {
						@Nonnull
						@Override
						@SideOnly( Side.CLIENT )
						public ItemStack createIcon() {
							return new ItemStack( Items.FISH );
						}
					};
				}
				return this.fallback_tab;
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public ItemStack getFallbackTabIconItem() {
				return new ItemStack( Items.FISH );
			}
			
			@Override
			public Gson getGson() {
				return PackLoader.this.gson;
			}
		};
		
		this.packs.forEach( pair -> {
			final IPackInfo info = pair.first();
			FMUM.LOGGER.info( "fmum.post_load_pack", info.getName() );
			pair.second().onPostLoad( post_load_ctx );
		} );
	}
	
	void _regisSounds( IForgeRegistry< SoundEvent > registry )
	{
		this.sound_pool.values().forEach( registry::register );
		this.sound_pool.clear();
	}
	
	@SideOnly( Side.CLIENT )
	void _loadMeshForPacks()
	{
		final IMeshLoadContext ctx = path -> this.mesh_pool.computeIfAbsent(
			path,
			key -> (
				MeshBuilder.fromObjModel( key )
				.map( MeshBuilder::build )
				.map( Optional::of )
				.orElseGet( e -> {
					FMUM.LOGGER.exception( e, "fmum.mesh_load_error", key );
					return Optional.empty();
				} )
			)
		);
		
		this.packs.forEach( pair -> pair.second().onMeshLoad( ctx ) );
	}
	
	private < T > void __regisCapability( Class< T > cap_class )
	{
		final IStorage< T > def_serializer = new Capability.IStorage< T >() {
			@Nullable
			@Override
			public NBTBase writeNBT( Capability< T > capability, T instance, EnumFacing side ) {
				return null;
			}
			
			@Override
			public void readNBT( Capability< T > capability, T instance, EnumFacing side, NBTBase nbt ) {
				// Pass.
			}
		};
		final Callable< T > def_factory = () -> null;
		CapabilityManager.INSTANCE.register( cap_class, def_serializer, def_factory );
	}
}
