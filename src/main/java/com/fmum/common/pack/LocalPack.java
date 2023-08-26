package com.fmum.common.pack;

import com.fmum.common.FMUM;
import com.fmum.common.load.IContentBuildContext;
import com.fmum.common.load.LoaderNotFoundException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public abstract class LocalPack implements IContentPackFactory, IContentPack
{
	protected static final String ERROR_LOADING_TYPE = "fmum.error_loading_type";
	
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
	
	protected abstract void _loadPackContent( ILoadContext ctx );
	
	@SideOnly( Side.CLIENT )
	protected void _loadKeyBinds( ILoadContext ctx )
	{
		final File config_dir = Loader.instance().getConfigDir();
		final File key_bind_dir = new File(
			config_dir, this.mod_container.getModId() + "-key_bind" );
		if ( key_bind_dir.exists() )
		{
			final String fallback_type = "key_bind";
			final String parent_path = config_dir.getName()
				+ "/" + key_bind_dir.getName();
			this._tryLoadFromDir(
				key_bind_dir, fallback_type, parent_path, ctx );
			return;
		}
		
		final Map< String, ? > key_binds = this._createDefaultKeyBinds();
		if ( key_binds.isEmpty() ) {
			return;
		}
		
		key_bind_dir.mkdirs();
		final Gson gson = ctx.gson();
		key_binds.forEach( ( id, kb ) -> {
			final String file_name = id + ".json";
			final File file = new File( key_bind_dir, file_name );
			try ( FileWriter out = new FileWriter( file ) ) {
				out.write( gson.toJson( kb ) );
			}
			catch ( IOException e ) {
				// TODO: Handle exception.
			}
		} );
	}
	
	@SideOnly( Side.CLIENT )
	protected Map< String, ? > _createDefaultKeyBinds() {
		return Collections.emptyMap();
	}
	
	protected void _tryLoadFromDir(
		File search_in_dir,
		String fallback_type,
		String parent_path,
		ILoadContext ctx
	) {
		for ( final File file : search_in_dir.listFiles() )
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
				final int start = file_path.lastIndexOf( '/' );
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
	
	protected static class PackMetadataTemplate
	{
		public String name;
		public String author;
		public Set< String > ignored_entries = Collections.emptySet();
	}
}
