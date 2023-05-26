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
import static com.fmum.common.gun.ControllerDispatcher.ATTR_NO_MAG;

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
		
		this.addFlagAttr( ATTR_BOLT_CATCH_BEFORE_ACTION, boltCatchBeforeAction );
		this.addFlagAttr( ATTR_BOLT_CATCH_AFTER_ACTION, boltCatchAfterAction );
	}
	
	public GunControllerRanker( @Nullable IMag< ? > mag )
	{
		this.flagRankers.put( ATTR_IGNORE_MAG, flag -> flag ? 32 : 0 );
		
		final boolean hasMag = mag != null;
		this.flagRankers.put( ATTR_NO_MAG, flag -> flag ^ hasMag ? 64 : 0 );
		this.magCategory = hasMag ? mag.category() : ModuleCategory.END;
	}
	
	public GunControllerRanker addFlagAttr( String attr, boolean value )
	{
		this.flagRankers.put( attr, flag -> flag == value ? 64 : 0 );
		return this;
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
