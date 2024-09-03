package com.fmum.load;

import com.fmum.FMUM;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import gsf.util.animation.IAnimation;
import gsf.util.lang.Error;
import gsf.util.lang.Result;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class LocalPack implements IPackLoadCallback
{
	protected final File source;
	
	protected final IPackInfo pack_info;
	
	protected final LinkedList< Consumer< IPostLoadContext > > post_load_callbacks = new LinkedList<>();
	
	@SideOnly( Side.CLIENT )
	protected LinkedList< Consumer< IMeshLoadContext > > mesh_load_callbacks; {
		FMUM.SIDE.runIfClient( () -> this.mesh_load_callbacks = new LinkedList<>() );
	}
	
	protected LocalPack( File source, IPackInfo pack_info )
	{
		this.source = source;
		this.pack_info = pack_info;
	}
	
	@Override
	public void onPreLoad( IPreLoadContext ctx ) {
		// Pass.
	}
	
	@Override
	public void onPostLoad( IPostLoadContext ctx ) {
		this.post_load_callbacks.forEach( cb -> cb.accept( ctx ) );
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void onMeshLoad( IMeshLoadContext ctx ) {
		this.mesh_load_callbacks.forEach( cb -> cb.accept( ctx ) );
	}
	
	@Override
	public String toString()
	{
		final String pack_type = this.getClass().getSimpleName();
		final String source_name = this.pack_info.getSourceName();
		return String.format( "%s@%s", pack_type, source_name );
	}
	
	protected IContentBuildContext _buildContext( ILoadContext ctx )
	{
		return new IContentBuildContext() {
			@Override
			public Gson getGson() {
				return ctx.getGson();
			}
			
			@Override
			public Optional< IContentLoader > lookupContentLoader( String entry ) {
				return ctx.lookupContentLoader( entry );
			}
			
			@Override
			public Optional< IAnimation > loadAnimation( ResourceLocation location ) {
				return ctx.loadAnimation( location );
			}
			
			@Override
			public IPackInfo getPackInfo() {
				return LocalPack.this.pack_info;
			}
			
			@Override
			public void regisPostLoadCallback( Consumer< IPostLoadContext > callback ) {
				LocalPack.this.post_load_callbacks.add( callback );
			}
			
			@Override
			@SideOnly( Side.CLIENT )
			public void regisMeshLoadCallback( Consumer< IMeshLoadContext > callback ) {
				LocalPack.this.mesh_load_callbacks.add( callback );
			}
		};
	}
	
	@SideOnly( Side.CLIENT )
	protected void _loadKeyBinding(
		InputStream key_binding_json,
		IContentBuildContext ctx
	) throws IOException
	{
		final File config_dir = Loader.instance().getConfigDir();
		final String file_name = this.pack_info.getNamespace() + "-key_binding.json";
		final File key_binding_file = new File( config_dir, file_name );
		if ( !key_binding_file.exists() ) {
			Files.copy( key_binding_json, key_binding_file.toPath() );
		}
		
		final Gson gson = ctx.getGson();
		final Path file_path = Paths.get( config_dir.getName(), file_name );
		final String trace_prefix = String.format( "mc:%s#", file_path );
		try ( FileReader reader = new FileReader( key_binding_file ) )
		{
			final JsonObject data = gson.fromJson( reader, JsonObject.class );
			data.entrySet().forEach( entry -> {
				final JsonObject obj = entry.getValue().getAsJsonObject();
				final String fallback_name = entry.getKey();
				this._loadFromJson( obj, "key_binding", fallback_name, ctx )
					.matchAnyError( e -> {
						final String source_trace = trace_prefix + fallback_name;
						FMUM.LOGGER.exception( e, "fmum.content_load_error", source_trace );
					} )
					.exhaustive();
			} );
		}
		catch ( JsonSyntaxException e ) {
			FMUM.LOGGER.exception( e, "Bad key binding json format." );
		}
	}
	
	protected Result< ?, Exception > _loadFromJson(
		JsonObject json_obj,
		String fallback_type,
		String fallback_name,
		IContentBuildContext ctx
	) {
		// Check if it has its type specified.
		final JsonElement type_attr = json_obj.get( "__type__" );
		final String type = type_attr != null ? type_attr.getAsString() : fallback_type;
		return (
			ctx.lookupContentLoader( type )
			.map( loader -> Result.of( () -> loader.loadFrom( json_obj, fallback_name, ctx ) ) )
			.orElseGet( () -> {
				final String msg = FMUM.I18N.format( "fmum.content_loader_not_found", type );
				return new Error<>( new RuntimeException( msg ) );
			} )
		);
	}
	
	protected Result< ?, Exception > _loadJsonEntry(
		Reader entry_reader,
		String fallback_type,
		String fallback_name,
		IContentBuildContext ctx
	) {
		final Gson gson = ctx.getGson();
		return (
			Result.of( () -> gson.fromJson( entry_reader, JsonObject.class ) )
			.flatMap( obj -> this._loadFromJson( obj, fallback_type, fallback_name, ctx ) )
		);
	}
	
	protected Consumer< Exception > _logLoadError( String... path_fragments )
	{
		return e -> {
			final String pack_source = this.pack_info.getSourceName();
			final String source_trace = Paths.get( pack_source, path_fragments ).toString();
			FMUM.LOGGER.exception( e, "fmum.content_load_error", source_trace );
		};
	}
}
