package com.fmum.common.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fmum.client.item.RenderableItem;
import com.fmum.common.FMUM;
import com.fmum.common.gun.RailSlot;
import com.fmum.common.meta.MetaBase;
import com.fmum.common.module.MetaModular;
import com.fmum.common.module.ModuleSlot;
import com.fmum.common.module.PreInstalledModules;
import com.fmum.common.module.PreInstalledOnRail;
import com.fmum.common.pack.TypeParser;
import com.fmum.common.paintjob.MetaPaintable;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Base for common {@link FMUM} items. They are modular and support paintjob.
 * 
 * @author Giant_Salted_Fish
 */
// FIXME: replace RenderableItem
public abstract class TypeItemCustomizable< T extends RenderableItem > extends TypeItem< T >
	implements MetaModular, MetaPaintable
{
	public static final TypeParser< TypeItemCustomizable< ? > >
		parser = new TypeParser<>( TypeItem.parser );
	static
	{
		parser.addKeyword( "Slots", ( s, t ) -> t.slots = RailSlot.parse( s, 1 ) );
		parser.addKeywords(
			( s, t ) -> {
				PreInstalledOnRail modules = new PreInstalledOnRail( t.name );
				modules.parse( s, 1 );
				t.defModules = modules;
			},
			"PreInstalled",
			"DefModules"
		);
		
		// TODO: hit box
		
	}
	
	public ModuleSlot[] slots = DEF_SLOTS;
	
	public PreInstalledModules defModules = DEF_MODULES;
	
	// TODO: maybe a better initial capacity?
	public ArrayList< MetaBase > paintjobs = new ArrayList<>( 1 );
	
	public TypeItemCustomizable( String name )
	{
		super( name );
		
		// Add itself as a primary paintjob
		this.paintjobs.add( this );
	}
	
	@Override
	public void regisPostInitHandler( Map< String, Runnable > tasks )
	{
		super.regisPostInitHandler( tasks );
		MetaModular.super.regisPostInitHandler( tasks );
		MetaPaintable.super.regisPostInitHandler( tasks );
	}
	
	@Override
	public void regisPostLoadHandler( Map< String, Runnable > tasks )
	{
		super.regisPostLoadHandler( tasks );
		MetaModular.super.regisPostLoadHandler( tasks );
		MetaPaintable.super.regisPostLoadHandler( tasks );
	}
	
	@Override
	public int numSlots() { return this.slots.length; }
	
	@Override
	public ModuleSlot slot( int idx ) { return this.slots[ idx ]; }
	
	@Override
	public PreInstalledModules defModules() { return this.defModules; }
	
	/**
	 * Override this method if you want to do a check before adding the paint job or reject any
	 * paintjob adding
	 */
	@Override
	public void injectPaintjob( MetaBase paintjob )
	{
		this.paintjobs.add( paintjob );
		this.paintjobs.sort( null );
	}
	
	@Override
	public int numTextures() { return this.paintjobs.size(); }
	
	@Override
	@SideOnly( Side.CLIENT )
	public ResourceLocation texture( int idx ) { return this.paintjobs.get( idx ).texture(); }
}
