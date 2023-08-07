package com.fmum.common;

import com.fmum.client.FMUMClient;
import com.fmum.common.network.PacketHandler;
import com.fmum.common.pack.IContentPack;
import com.fmum.common.pack.ILoadablePack;
import com.fmum.common.tab.ICreativeTab;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class FMUMV2
{
	public static final String MODID = "fmum";
	public static final String MOD_NAME = "FMUM 2.0";
	public static final String MOD_VERSION = "0.3.3-alpha";
	
	public static final FMUMV2 MOD;
	static
	{
		final Supplier< ? > client_mod = () -> FMUMClient.MOD; // Use generic will crash.
		final Side side = FMLCommonHandler.instance().getSide();
		MOD = side.isServer() ? new FMUMV2() : ( FMUMV2 ) client_mod.get();
	}
	@Mod.InstanceFactory
	private static FMUMV2 create() { return MOD; }
	
	/**
	 * You can use this to customized log if none of the util log function provided in this class
	 * fits your case.
	 *
	 * @see #logInfo(String, Object...)
	 * @see #logWarning(String, Object...)
	 * @see #logError(String, Object...)
	 * @see #logException(Throwable, String, Object...)
	 */
	public final Logger logger = LogManager.getLogger( MODID );
	
	public final Registry< IContentPack > content_packs = new Registry<>( IContentPack::name );
	
//	public final ICreativeTab default_creative_tab;
	
	protected final PacketHandler packet_handler = new PacketHandler( MODID );
	
	protected final File config_dir = Loader.instance().getConfigDir();
	
	protected FMUMV2() { }
	
	@Mod.EventHandler
	private void onPreInit( FMLPreInitializationEvent evt )
	{
		logInfo( "fmum.on_pre_init" );
		
		this._loadPacks();
		
		logInfo( "fmum.pre_init_complete" );
	}
	
	protected void _loadPacks()
	{
		final LinkedList< ILoadablePack > loadable_packs = new LinkedList<>();
		final BiConsumer< ILoadablePack, String > gather_pack = ( pack, source_name ) -> {
			loadable_packs.add( pack );
			this.logInfo( "fmum.detect_content_pack", source_name );
		};
	}
	
	private void __forEachPackInModFolder( BiConsumer< ILoadablePack, String > visitor )
	{
		Loader.instance().getActiveModList().forEach( mod_container -> {
			final boolean is_mod_based_pack = mod_container
				.getRequirements().contains( FMUMV2.MODID );
			if ( !is_mod_based_pack ) {
				return;
			}
			
			final Object pack_mod = mod_container.getMod();
			if ( pack_mod instanceof ILoadablePack )
			{
				visitor.accept( ( ILoadablePack ) pack_mod, mod_container.getSource().getName() );
				return;
			}
			
			this.logError(
				"fmum.invalid_mod_based_pack",
				mod_container.getName(), mod_container.getModId()
			);
		} );
	}
	
	private void __forEachPackInPackFolder( Consumer< ILoadablePack > visitor )
	{
		final File pack_dir = new File( this.config_dir.getParentFile(), MODID );
		if ( !pack_dir.exists() )
		{
			pack_dir.mkdirs();
			this.logInfo( "fmum.pack_dir_created", MODID );
		}
		
		final Pattern jarRegex = Pattern.compile( "(.+)\\.(zip|jar)$" );
		for ( final File file : pack_dir.listFiles() )
		{
			final boolean is_folder_pack = file.isDirectory();
			
		}
	}
	
	/**
	 * Use this to do localization if your code runs on both side to avoid crash.
	 */
	@SuppressWarnings( "deprecation" )
	public String format( String translate_key, Object... parameters )
	{
		return net.minecraft.util.text.translation
			.I18n.translateToLocalFormatted( translate_key, parameters );
	}
	
	public void logInfo( String translate_key, Object... parameters ) {
		this.logger.info( this.format( translate_key, parameters ) );
	}
	
	public void logWarning( String translate_key, Object... parameters ) {
		this.logger.warn( this.format( translate_key, parameters ) );
	}
	
	public void logError( String translate_key, Object... parameters ) {
		this.logger.error( this.format( translate_key, parameters ) );
	}
	
	public void logException( Throwable e, String translate_key, Object... parameters ) {
		this.logger.error( this.format( translate_key, parameters ), e );
	}
}
