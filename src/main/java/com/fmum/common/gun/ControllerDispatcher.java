package com.fmum.common.gun;

import com.fmum.common.load.IContentProvider;
import com.fmum.common.mag.IMag;
import com.fmum.common.module.ModuleCategory;
import com.google.common.collect.LinkedListMultimap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

public class ControllerDispatcher
{
	public static final JsonDeserializer< ControllerDispatcher >
		ADAPTER = ( json, typeOfT, context ) -> new ControllerDispatcher( json, context );
	
	public static final String
		ATTR_BOLT_CATCH_BEFORE_ACTION = "boltCatchBeforeAction",
		ATTR_BOLT_CATCH_AFTER_ACTION = "boltCatchAfterAction",
		ATTR_IGNORE_MAG = "ignoreMag",
		ATTR_NO_MAG = "noMag",
		ATTR_MAG_CATEGORY = "magCategory";
	
	protected final HashSet< GunOpController > controllers = new HashSet<>();
	
	protected final LinkedListMultimap< String, GunOpController >
		invertedMapTrue = LinkedListMultimap.create(),
		invertedMapFalse = LinkedListMultimap.create();
	
	protected final LinkedListMultimap< ModuleCategory, GunOpController >
		invertedMapMagCategory = LinkedListMultimap.create();
	
	public ControllerDispatcher( JsonElement json, JsonDeserializationContext context )
	{
		json.getAsJsonArray().forEach( e -> {
			final GunOpController controller = context.deserialize( e, GunOpController.class );
			final Condition condition = context.deserialize( e, Condition.class );
			
			this.controllers.add( controller );
			( condition.boltCatchBeforeAction ? this.invertedMapTrue : this.invertedMapFalse )
				.put( ATTR_BOLT_CATCH_BEFORE_ACTION, controller );
			( condition.boltCatchAfterAction ? this.invertedMapTrue : this.invertedMapFalse )
				.put( ATTR_BOLT_CATCH_AFTER_ACTION, controller );
			( condition.ignoreMag ? this.invertedMapTrue : this.invertedMapFalse )
				.put( ATTR_IGNORE_MAG, controller );
			( condition.noMag ? this.invertedMapTrue : this.invertedMapFalse )
				.put( ATTR_NO_MAG, controller );
			this.invertedMapMagCategory.put( condition.magCategory, controller );
		} );
	}
	
	public ControllerDispatcher( GunOpController defaultController ) {
		this.controllers.add( defaultController );
	}
	
	@SideOnly( Side.CLIENT )
	public void checkAssetsSetup( IContentProvider provider ) {
		this.controllers.forEach( c -> c.checkAssetsSetup( provider ) );
	}
	
	public GunOpController match(
		@Nullable IMag< ? > mag,
		boolean boltCatchBeforeAction,
		boolean boltCatchAfterAction
	) {
		return this.match( new GunControllerRanker(
			mag, boltCatchBeforeAction, boltCatchAfterAction
		) );
	}
	
	public GunOpController match( GunControllerRanker ranker )
	{
		final HashMap< GunOpController, Integer > ranks = new HashMap<>();
		this.controllers.forEach( controller -> ranks.put( controller, 0 ) );
		
		this.invertedMapTrue.asMap().forEach( ( attr, controllers ) -> {
			final int rank = ranker.getRank( attr, true );
			controllers.forEach(
				controller -> ranks.compute( controller, ( key, val ) -> val + rank )
			);
		} );
		this.invertedMapFalse.asMap().forEach( ( attr, controllers ) -> {
			final int rank = ranker.getRank( attr, false );
			controllers.forEach(
				controller -> ranks.compute( controller, ( key, val ) -> val + rank )
			);
		} );
		this.invertedMapMagCategory.asMap().forEach( ( magCategory, controllers ) -> {
			final int rank = ranker.getRank( ATTR_MAG_CATEGORY, magCategory );
			controllers.forEach(
				controller -> ranks.compute( controller, ( key, val ) -> val + rank )
			);
		} );
		
		// Return controller with the highest rank.
		final TreeMap< Integer, GunOpController > tree = new TreeMap<>();
		ranks.forEach( ( controller, rank ) -> tree.put( rank, controller ) );
		return tree.lastEntry().getValue();
	}
	
	public static class Condition
	{
		@SerializedName( value = ATTR_BOLT_CATCH_BEFORE_ACTION, alternate = "boltOpenBeforeAction" )
		protected boolean boltCatchBeforeAction;
		@SerializedName( value = ATTR_BOLT_CATCH_AFTER_ACTION, alternate = "boltOpenAfterAction" )
		protected boolean boltCatchAfterAction;
		
		boolean ignoreMag;
		protected boolean noMag;
		protected ModuleCategory magCategory = ModuleCategory.END;
	}
}
