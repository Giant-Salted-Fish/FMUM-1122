package com.fmum.common.gun;

import java.util.Map;

import com.fmum.client.item.RenderableItem;
import com.fmum.common.item.TypeItemCustomizable;
import com.fmum.common.pack.TypeParser;
import com.fmum.common.util.Vec3;

/**
 * A default implementation of {@link MetaGunPart}
 * 
 * @author Giant_Salted_Fish
 */
// FIXME: replace RenderableItem
public abstract class TypeGunPart< T extends RenderableItem > extends TypeItemCustomizable< T >
	implements MetaGunPart
{
	public static final TypeParser< TypeGunPart< ? > >
		parser = new TypeParser<>( TypeItemCustomizable.parser );
	static
	{
		parser.addKeyword(
			"Offsets",
			( s, t ) -> {
				t.offsets = new double[ s.length - 1 ];
				for( int i = s.length; --i > 0; t.offsets[ i - 1 ] = Double.parseDouble( s[ i ] ) );
			}
		);
		
		parser.addKeyword(
			"AimCenter",
			( s, t ) -> {
				t.aimCenter = Vec3.locate( 0D );
				switch( s.length )
				{
				case 4: t.aimCenter.z = Double.parseDouble( s[ 3 ] );
				case 3: t.aimCenter.y = Double.parseDouble( s[ 2 ] );
				default: t.aimCenter.x = Double.parseDouble( s[ 1 ] );
				}
			}
		);
		parser.addKeyword(
			"LeftHandPriority",
			( s, t ) -> t.leftHandPriority = Integer.parseInt( s[ 1 ] )
		);
		parser.addKeyword(
			"RightHandPriority",
			( s, t ) -> t.rightHandPriority = Integer.parseInt( s[ 1 ] )
		);
	}
	
	protected static final Vec3 DEF_AIM_CENTER = Vec3.locate( 0D );
	
	public double[] offsets = DEF_OFFSETS;
	
	/**
	 * Point in space where player will aim down with this gun part
	 */
	public Vec3 aimCenter = DEF_AIM_CENTER;
	
	public int
		leftHandPriority = Integer.MIN_VALUE,
		rightHandPriority = Integer.MIN_VALUE;
	
	public TypeGunPart( String name ) { super( name ); }
	
	@Override
	public void regisPostInitHandler( Map< String, Runnable > tasks )
	{
		super.regisPostInitHandler( tasks );
		MetaGunPart.super.regisPostInitHandler( tasks );
		
		// Do not forget to apply model scale
		tasks.put(
			"RESCALE_GUN",
			() -> {
				// Default offset is 0D hence multiply it with any scale will not change it
				for( int i = this.offsets.length; i-- > 0; this.offsets[ i ] *= this.scale );
				
				this.aimCenter.scale( this.scale );
			}
		);
	}
	
	@Override
	public void regisPostLoadHandler( Map< String, Runnable > tasks )
	{
		super.regisPostLoadHandler( tasks );
		MetaGunPart.super.regisPostLoadHandler( tasks );
	}
	
	@Override
	public int numOffsets() { return this.offsets.length; }
	
	@Override
	public double offset( int idx ) { return this.offsets[ idx ]; }
}
