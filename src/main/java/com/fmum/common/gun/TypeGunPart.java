package com.fmum.common.gun;

import java.util.Set;

import com.fmum.common.item.TypeItemCustomizable;
import com.fmum.common.module.ModuleSlot;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.LocalAttrParser;
import com.fmum.common.util.Vec3;

import net.minecraft.nbt.NBTTagList;

/**
 * <p> A default implementation of {@link MetaGunPart}. </p>
 * 
 * <p> It needs one {@code int} to store install step and offset. </p>
 * <pre>
 *  4      8      4          16
 * 0000 00000000 0000 0000000000000000
 *  |      |      |          |
 * offset step   flag    undefined
 * </pre>
 * 
 * @author Giant_Salted_Fish
 */
public abstract class TypeGunPart extends TypeItemCustomizable implements MetaGunPart
{
	public static final LocalAttrParser< TypeGunPart >
		parser = new LocalAttrParser<>( TypeItemCustomizable.parser );
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
				t.aimCenter = Vec3.get( 0D );
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
	
	protected static final Vec3 DEF_AIM_CENTER = Vec3.get( 0D );
	
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
	public void regisPostInitHandler( Set< Runnable > tasks )
	{
		super.regisPostInitHandler( tasks );
		MetaGunPart.super.regisPostInitHandler( tasks );
		
		// Do not forget to apply model scale
		tasks.add( () -> {
			// Default offset is 0D hence multiply it with any scale will not change it
			for( int i = this.offsets.length; i-- > 0; this.offsets[ i ] *= this.modelScale );
			
			this.aimCenter.scale( this.modelScale );
		} );
	}
	
	@Override
	public void regisPostLoadHandler( Set< Runnable > tasks )
	{
		super.regisPostLoadHandler( tasks );
		MetaGunPart.super.regisPostLoadHandler( tasks );
	}
	
	@Override
	public int dataArraySize() { return super.dataArraySize() + 1; }
	
	@Override
	public void applyTransform( ModuleSlot slot, NBTTagList tag, CoordSystem dst )
	{
		dst.trans(
			slot.posStepX() * (
				( ( NBTTagList ) tag.get( 0 ) ).getIntArrayAt( 0 )[
					super.dataArraySize()
				] >>> 16 & 0xFF
			),
			CoordSystem.NORM_X
		);
	}
	
	@Override
	public int numOffsets() { return this.offsets.length; }
	
	@Override
	public double offset( int index ) { return this.offsets[ index ]; }
}
