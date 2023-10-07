package com.fmum.common.pack;

import com.fmum.client.FMUMClient;
import com.fmum.common.FMUM;
import com.fmum.common.load.IContentBuildContext;
import com.fmum.common.load.LoaderNotFoundException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.AbstractResourcePack;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackFileNotFoundException;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class LocalPack implements IContentPackFactory, IContentPack
{
	protected static final String ERROR_LOADING_TYPE = "fmum.error_loading_type";
	
	@SideOnly( Side.CLIENT )
	protected static Method AbstractResourcePack_getInputStreamByName;
	static
	{
		if ( FMUM.MOD.isClient() )
		{
			final Class< ? > target = AbstractResourcePack.class;
			final String srg_name = "func_110591_a";
			final String mcp_name = "getInputStreamByName";
			final Class< ? > param_class = String.class;
			Method method;
			try {
				method = target.getDeclaredMethod( srg_name, param_class );
			}
			catch ( NoSuchMethodException e )
			{
				try {
					method = target.getDeclaredMethod( mcp_name, param_class );
				}
				catch ( NoSuchMethodException e_ )
				{
					final String err_msg = FMUM.MOD.format(
						"fmum.error_method_reflection",
						target.getName(), srg_name, mcp_name
					);
					throw new RuntimeException( err_msg, e_ );
				}
			}
			
			method.setAccessible( true );
			AbstractResourcePack_getInputStreamByName = method;
		}
	}
	
	protected final ModContainer mod_container;
	protected final HashSet< String > ignored_entries = new HashSet<>();
	
	protected LocalPack( ModContainer mod_container )
	{
		this.mod_container = mod_container;
		this.ignored_entries.add( "assets" );
	}
	
	@Override
	public String name() {
		return this.mod_container.getName();
	}
	
	@Override
	public String author() {
		return String.join( ", ", this.mod_container.getMetadata().authorList );
	}
	
	@Override
	public String resourceDomain() {
		return this.mod_container.getModId();
	}
	
	@Override
	public String sourceName() {
		return this.mod_container.getSource().getName();
	}
	
	@Override
	public IContentPack createServerSide( IPrepareContext ctx )
	{
		ctx.regisLoadCallback( ctx_ -> {
			FMUM.MOD.logInfo( "fmum.load_content_pack", this.name() );
			this._loadPackContent( ctx_ );
		} );
		return this;
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public IContentPack createClientSide( IPrepareContext ctx )
	{
		this.createServerSide( ctx );
		
		// Load key binds on client side.
		ctx.regisLoadCallback( this::_loadKeyBinds );
		return this;
	}
	
	@Override
	public String toString() {
		return this.name();
	}
	
	protected abstract void _loadPackContent( ILoadContext ctx );
	
	@SideOnly( Side.CLIENT )
	protected void _loadKeyBinds( ILoadContext ctx )
	{
		final File config_dir = Loader.instance().getConfigDir();
		final File key_bind_dir = new File(
			config_dir, this.mod_container.getModId() + "-key_bind" );
		if ( !key_bind_dir.exists() )
		{
			final Gson gson = ctx.gson();
			final Optional< JsonObject > data = this._defaultKeyBindJson( gson );
			if ( !data.isPresent() ) {
				return;
			}
			
			key_bind_dir.mkdirs();
			data.get().entrySet().forEach( entry -> {
				final String file_name = entry.getKey() + ".json";
				final File file = new File( key_bind_dir, file_name );
				try ( FileWriter out = new FileWriter( file ) )
				{
					out.write( gson.toJson( entry.getValue() ) );
				}
				catch ( IOException e )
				{
					// TODO: Handle exception.
				}
			} );
		}
		
		final String fallback_type = "key_bind";
		final String parent_path = config_dir.getName()
			+ "/" + key_bind_dir.getName();
		this._tryLoadFromDir( key_bind_dir, fallback_type, parent_path, ctx );
	}
	
	@SideOnly( Side.CLIENT )
	protected Optional< JsonObject > _defaultKeyBindJson( Gson gson )
	{
		final IResourcePack res_pack = FMLClientHandler.instance()
			.getResourcePackFor( this.mod_container.getModId() );
		final String path = "key_bind.json";
		final Consumer< Exception > err_log = e -> {
			final String translate_key = "fmum.error_reading_default_key_binds";
			FMUMClient.MOD.logException( e, translate_key, this.name() );
		};
		try (
			Reader in = new InputStreamReader( ( InputStream )
				AbstractResourcePack_getInputStreamByName.invoke( res_pack, path ) )
		) {
			final JsonObject o = gson.fromJson( in, JsonObject.class );
			return Optional.of( o );
		}
		catch ( InvocationTargetException e )
		{
			final boolean no_default_key_bind =
				e.getCause() instanceof ResourcePackFileNotFoundException;
			if ( !no_default_key_bind ) {
				err_log.accept( e );
			}
		}
		catch ( IOException e ){
			err_log.accept( e );
		}
		catch ( IllegalAccessException e ) {
			throw new RuntimeException( "This should never happen.", e );
		}
		return Optional.empty();
	}
	
	protected void _tryLoadFromDir(
		File search_in_dir,
		String fallback_type,
		String parent_path,
		ILoadContext ctx
	) {
		final File[] files = search_in_dir.listFiles();
		assert files != null;
		
		for ( File file : files )
		{
			final String file_name = file.getName();
			final String file_path = parent_path + "/" + file_name;
			if ( file.isDirectory() )
			{
				this._tryLoadFromDir( file, fallback_type, file_path, ctx );
				continue;
			}
			
			try
			{
				if ( file_name.endsWith( ".json" ) )
				{
					try ( FileReader in = new FileReader( file ) ) {
						this._loadJsonEntry( in, fallback_type, file_path, ctx );
					}
				}
				
//				else if ( file_name.endsWith( ".class" ) )
//				{
//					final String class_path = file_path.replace( '/', '.' );
//					this._loadClassEntry( class_path, context );
//				}
			}
			catch ( Exception e )
			{
				final String source_trace = this.sourceName() + "/" + file_path;
				FMUM.MOD.logException( e, ERROR_LOADING_TYPE, source_trace );
			}
		}
	}
	
	protected Optional< Object > _loadJsonEntry(
		Reader in,
		String fallback_type,
		String file_path,
		ILoadContext ctx
	) {
		// Check if it has specified a type(loader).
		final JsonObject obj = ctx.gson().fromJson( in, JsonObject.class );
		final String loader = Optional.ofNullable( obj.get( "__type__" ) )
			.map( JsonElement::getAsString ).orElse( fallback_type );
		
		final IContentBuildContext build_context = new IContentBuildContext()
		{
			@Override
			public String fallbackName()
			{
				final int start = file_path.lastIndexOf( '/' ) + 1;
				final int end = file_path.length() - ".json".length();
				return file_path.substring( start, end );
			}
			
			@Override
			public IContentPack contentPack() {
				return LocalPack.this;
			}
			
			@Override
			public Gson gson() {
				return ctx.gson();
			}
			
			@Override
			public void regisPostLoadCallback(
				Consumer< IPostLoadContext > callback
			) { ctx.regisPostLoadCallback( callback ); }
			
			@Override
			@SideOnly( Side.CLIENT )
			public void regisMeshLoadCallback(
				Consumer< IMeshLoadContext > callback
			) { ctx.regisMeshLoadCallback( callback ); }
		};
		
		try
		{
			return Optional.of(
				ctx.loadContent( loader, obj, build_context ) );
		}
		catch ( LoaderNotFoundException e )
		{
			final String path = this.sourceName() + "/" + file_path;
			FMUM.MOD.logError( "fmum.type_loader_not_found", path, loader );
		}
		return Optional.empty();
	}
}
