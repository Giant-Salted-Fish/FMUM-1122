package com.fmum.common.meta;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.fmum.client.ResourceHandler;
import com.fmum.common.FMUM;
import com.fmum.common.Launcher.AutowireLogger;
import com.fmum.common.Meta;
import com.fmum.common.pack.ContentProvider;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Provides Basic information for a thing in {@link FMUM}, including the name(should be universally
 * unique) and the source that provide this thing
 * 
 * @author Giant_Salted_Fish
 */
public interface MetaBase extends Meta, AutowireLogger
{
	public static final HashMap< String, MetaBase > regis = new HashMap<>();
	
	/**
	 * This will be called notice a type has been parsed from a text file in a content pack. Or
	 * right after the instantiation of the type of it is .class based.
	 * 
	 * @warn
	 *     Override this method for a derived class with multiple meta implementation could have
	 *     cross call problem. Hence it is recommended to override
	 *     {@link #regisPostInitHandler(Set)} rather than this method to avoid this problem.
	 */
	public default void onPostInit()
	{
		final TreeSet< Runnable > tasks = new TreeSet<>();
		this.regisPostInitHandler( tasks );
		for( Runnable task : tasks )
			task.run();
	}
	
	/**
	 * Called after the load of types has been finished
	 * 
	 * @warn
	 *     Override this method for a derived class with multiple meta implementation could have
	 *     cross call problem. Hence it is recommended to override
	 *     {@link #regisPostLoadHandler(Set)} rather than this method to avoid this problem.
	 */
	public default void onPostLoad()
	{
		final TreeSet< Runnable > tasks = new TreeSet<>();
		this.regisPostLoadHandler( tasks );
		for( Runnable task : tasks )
			task.run();
	}
	
	/**
	 * Called when the first time the player enters a world to load model for rendering
	 * 
	 * @see TODO: loader function
	 */
	@SideOnly( Side.CLIENT )
	public default void onModelLoad() { }
	
	/**
	 * Register post initialization handlers that will be called in {@link #onPostInit()}. In
	 * default it register this type into FMUM. This can be critical as skip the register will cause
	 * other meta that wants to interact with this type fail to find you.
	 * 
	 * @see #onPostInit()
	 * @param tasks Add your task into this set
	 */
	public default void regisPostInitHandler( Set< Runnable > tasks ) {
		tasks.add( () -> this.regisTo( this, regis ) );
	}
	
	/**
	 * Register post load handlers that will be called in {@link #onPostLoad()}
	 * 
	 * @see #onPostLoad()
	 * @param tasks Add your task into this set
	 */
	public default void regisPostLoadHandler( Set< Runnable > tasks ) { }
	
	public default ContentProvider provider() { return FMUM.MOD; }
	
	/**
	 * @throws RuntimeException If provider has already been set
	 */
	public default void $provider( ContentProvider provider ) { }
	
	@Override
	public default String author() { return this.provider().author(); }
	
	@SideOnly( Side.CLIENT )
	public default String translationKey() { return this.name(); }
	
	@SideOnly( Side.CLIENT )
	public default ResourceLocation texture() { return ResourceHandler.TEXTURE_GREEN; }
	
	@SideOnly( Side.CLIENT )
	public default void render() { }
	
	public default EnumMeta enumMeta() { return EnumMeta.GENERAL; }
	
	/**
	 * @return
	 *     A human friendly name that shows the type, source and name of this instance. Mainly for
	 *     debug.
	 */
	public default String identifier() {
		return this.enumMeta() + "<" + this.provider().name() + ":" + this.name() + ">";
	}
	
	public default < V extends MetaBase > void regisTo( V tar, Map< String, V > dest )
	{
		final MetaBase meta = dest.put( tar.name(), tar );
		if( meta != null && meta != tar )
			this.log().warn( this.format(
				"fmum.duplicatemetaregistration",
				meta.toString(),
				this.toString()
			) );
	}
	
	public static MetaBase get( String name ) { return regis.get( name ); }
}
