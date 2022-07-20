package com.fmum.common.meta;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.fmum.client.ResourceManager;
import com.fmum.common.AutowireLogger;
import com.fmum.common.FMUM;
import com.fmum.common.Meta;
import com.fmum.common.item.MetaItem;
import com.fmum.common.pack.ContentProvider;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
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
		final TreeMap< String, Runnable > tasks = new TreeMap<>();
		this.regisPostInitHandler( tasks );
		for( Runnable task : tasks.values() )
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
		final TreeMap< String, Runnable > tasks = new TreeMap<>();
		this.regisPostLoadHandler( tasks );
		for( Runnable task : tasks.values() )
			task.run();
	}
	
	/**
	 * Called when the time you should load your model for rendering. It is delayed till the first
	 * time the player enters a world as building a VBO at any initialization phase provided by
	 * {@link MinecraftForge} could cause display problems.
	 * 
	 * @see FMUM#loadModel(String)
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
	public default void regisPostInitHandler( Map< String, Runnable > tasks ) {
		tasks.put( "REGIS_BASE", () -> this.regisTo( this, regis ) );
	}
	
	/**
	 * Register post load handlers that will be called in {@link #onPostLoad()}
	 * 
	 * @see #onPostLoad()
	 * @param tasks Add your task into this set
	 */
	public default void regisPostLoadHandler( Map< String, Runnable > tasks ) { }
	
	public default ContentProvider provider() { return FMUM.MOD; }
	
	/**
	 * @throws RuntimeException If provider has already been set
	 */
	public default void $provider( ContentProvider provider ) { }
	
	@Override
	public default String author() { return this.provider().author(); }
	
	/**
	 * This method is not side only as some meta requires it in both side. For example
	 * {@link MetaItem#unlocalizedName(ItemStack)}.
	 */
	public default String unlocalizedName() { return this.name(); }
	
	@SideOnly( Side.CLIENT )
	public default ResourceLocation texture() { return ResourceManager.TEXTURE_GREEN; }
	
	@SideOnly( Side.CLIENT )
	public default void render() { }
	
	/**
	 * @return Scale of the model. Will also be applied to all dimension settings.
	 */
	public default double scale() { return 1D; }
	
	public default EnumMeta enumMeta() { return EnumMeta.OTHER; }
	
	/**
	 * @return A human friendly identifier that shows its type, source and name. Mainly for debug.
	 */
	public default String identifier() {
		return "(" + this.enumMeta() + ")" + this.provider().name() + ":" + this.name();
	}
	
	public default < V extends MetaBase > void regisTo( V tar, Map< String, V > dest )
	{
		final MetaBase meta = dest.put( tar.name(), tar );
		if( meta != null )
			this.log().warn( this.format(
				"fmum.duplicatemetaregistration",
				meta.toString(),
				this.toString()
			) );
	}
	
	public static MetaBase get( String name ) { return regis.get( name ); }
}
