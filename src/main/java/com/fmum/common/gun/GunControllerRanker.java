package com.fmum.common.gun;

import com.fmum.common.mag.IMag;
import com.fmum.common.module.ModuleCategory;
import com.fmum.common.player.OperationController;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.function.Function;

import static com.fmum.common.gun.ControllerDispatcher.ATTR_BOLT_CATCH_AFTER_ACTION;
import static com.fmum.common.gun.ControllerDispatcher.ATTR_BOLT_CATCH_BEFORE_ACTION;
import static com.fmum.common.gun.ControllerDispatcher.ATTR_IGNORE_MAG;
import static com.fmum.common.gun.ControllerDispatcher.ATTR_MAG_CATEGORY;
import static com.fmum.common.gun.ControllerDispatcher.ATTR_REQUIRE_NO_MAG;

/**
 * Used by {@link ControllerDispatcher} to select proper {@link OperationController} under certain
 * conditions.
 * 
 * @author Giant_Salted_Fish
 */
public class GunControllerRanker
{
	protected final HashMap< String, Function< Boolean, Integer > > flagRankers = new HashMap<>();
	
	protected final ModuleCategory magCategory;
	
	public GunControllerRanker(
		@Nullable IMag< ? > mag,
		boolean boltCatchBeforeAction,
		boolean boltCatchAfterAction
	) {
		this( mag );
		
		this.flagRankers.put(
			ATTR_BOLT_CATCH_BEFORE_ACTION,
			flag1 -> flag1 == boltCatchBeforeAction ? 64 : 0
		);
		this.flagRankers.put(
			ATTR_BOLT_CATCH_AFTER_ACTION,
			flag -> flag == boltCatchAfterAction ? 64 : 0
		);
	}
	
	public GunControllerRanker( @Nullable IMag< ? > mag )
	{
		final boolean noMag = mag == null;
		this.flagRankers.put( ATTR_REQUIRE_NO_MAG, flag -> flag == noMag ? 64 : 0 );
		this.flagRankers.put( ATTR_IGNORE_MAG, flag -> flag && noMag ? 32 : 0 );
		this.magCategory = noMag ? ModuleCategory.END : mag.category();
	}
	
	public int getRank( String attr, boolean value ) {
		return this.flagRankers.getOrDefault( attr, flag -> 0 ).apply( value );
	}
	
	public int getRank( String attr, ModuleCategory category )
	{
		final boolean attrMagCategory = attr.equals( ATTR_MAG_CATEGORY );
		return attrMagCategory ? this.magCategory.getMatchingLayerCount( category ) : 0;
	}
}
