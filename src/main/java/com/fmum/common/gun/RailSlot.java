package com.fmum.common.gun;

import java.util.LinkedList;
import java.util.TreeSet;

import com.fmum.common.FMUM;
import com.fmum.common.module.FixedSlot;
import com.fmum.common.module.ModuleSlot;
import com.fmum.common.util.CoordSystem;

/**
 * An implementation for {@link ModuleSlot} that used for guns in {@link FMUM}. Support to adjust
 * position in x-direction and specify the rotation along x-axis.
 * 
 * @see ModuleSlot
 * @author Giant_Salted_Fish
 */
public class RailSlot extends FixedSlot
{
	/**
	 * Orientation of this slot. Its the rotation along x-axis.
	 */
	public double rotX = 0D;
	
	/**
	 * How far it goes for each adjustment step
	 */
	public double stepLen = 0D;
	
	/**
	 * Max steps that the attachments can go on this slot
	 */
	public short maxStep = 0;
	
	@Override
	public RailSlot rescale( double s )
	{
		super.rescale( s );
		this.stepLen *= s;
		return this;
	}
	
	@Override
	public int maxPosStep() { return this.maxStep; }
	
	@Override
	public double posStep() { return this.stepLen; }
	
	@Override
	public void apply( CoordSystem sys )
	{
		sys.trans( this );
		
		if( this.rotX != 0D )
		{
			sys.rot( this.rotX, CoordSystem.X );
			sys.submitRot();
		}
	}
	
	public static ModuleSlot[] parse( String[] split, int cursor )
	{
		final LinkedList< ModuleSlot > ret = new LinkedList< ModuleSlot >();
		
		RailSlot slot = new RailSlot();
		for( int i = cursor; i < split.length; ++i )
			switch( split[ i ] )
			{
			case "|":
				ret.add( slot );
				slot = new RailSlot();
				cursor = i + 1;
				break;
				
			case "M[":
				int after = parseList(
					split,
					"]",
					i,
					slot.moduleWhitelist = new TreeSet<>()
				);
				cursor += after - i + 1;
				i = after;
				break;
				
			case "M<":
				after = parseList(
					split,
					">",
					i,
					slot.moduleBlacklist = new TreeSet<>()
				);
				cursor += after - i + 1;
				i = after;
				break;
				
			case "C[":
				after = parseList(
					split,
					"]",
					i,
					slot.categoryWhitelist = new TreeSet<>()
				);
				cursor += after - i + 1;
				i = after;
				break;
				
			case "C<":
				after = parseList(
					split,
					">",
					i,
					slot.categoryBlacklist = new TreeSet<>()
				);
				cursor += after - i + 1;
				i = after;
				break;
				
			default:
				switch(i - cursor)
				{
				case 0:
					slot.x = Double.parseDouble( split[ i ] ) / 16D;
					break;
				case 1:
					slot.y = Double.parseDouble( split[ i ] ) / 16D;
					break;
				case 2:
					slot.z = Double.parseDouble( split[ i ] ) / 16D;
					break;
				case 3:
					slot.rotX = Double.parseDouble( split[ i ] );
					break;
				case 4:
					slot.stepLen = Double.parseDouble( split[ i ] ) / 16D;
					break;
				case 5:
					slot.maxStep = Short.parseShort( split[ i ] );
					break;
				case 6:
					slot.maxCanInstall = Byte.parseByte( split[ i ] );
					break;
				default:
					throw new RuntimeException( "Too many arguments for one attachable slot" );
				}
			}
		
		if( slot != null ) ret.add( slot );
		return ret.toArray( new ModuleSlot[ ret.size() ] );
	}
}
