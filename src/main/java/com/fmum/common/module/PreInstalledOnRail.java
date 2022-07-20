package com.fmum.common.module;

import java.util.LinkedList;

import javax.annotation.Nullable;

import com.fmum.common.AutowireLogger;
import com.fmum.common.gun.RailSlot;

import net.minecraft.nbt.NBTTagList;

/**
 * An implementation of {@link PreInstalledModules} that installed on {@link RailSlot}
 * 
 * @author Giant_Salted_Fish
 */
public class PreInstalledOnRail implements PreInstalledModules, AutowireLogger
{
	public String module;
	
	public LinkedList< LinkedList< PreInstalledModules > > slots = new LinkedList<>();
	
	public short dam;
	
	public short step;
	public byte offset;
	
	public PreInstalledOnRail( String module ) { this.module = module; }
	
	/**
	 * Parse default modules from given split
	 * 
	 * @param split String array to parse default attachments from
	 * @param cursor Should be set to the index right after '['
	 * @return Cursor position after parsing
	 */
	public int parse( String[] split, int cursor )
	{
		PreInstalledOnRail curModule = null;
		LinkedList< PreInstalledModules > curSlot = new LinkedList<>();
		for( int i = cursor; i < split.length; ++i )
			switch( split[ i ] )
			{
			// Switch to next slot
			case "|":
				// Save previous one if yet not saved
				if( curModule != null )
				{
					curSlot.add( curModule );
					curModule = null;
				}
				
				this.slots.add( curSlot );
				curSlot = new LinkedList<>();
				cursor = i + 1;
				break;
				
			// Next module in same slot
			case ">":
			case "+":
				cursor = i + 1;
				break;
				
			// Parse default modules for current reading module
			case "[":
				int after = curModule.parse( split, i + 1 );
				cursor += after - i + 1;
				i = after;
				break;
				
			// End symbol
			case "]":
				cursor = i;
				i = split.length;
				break;
				
			default:
				switch( i - cursor )
				{
				case 0:
					if( curModule != null )
						curSlot.add( curModule );
					
					curModule = new PreInstalledOnRail( split[ i ] );
					break;
				case 1:
					curModule.step = Short.parseShort( split[ i ] );
					break;
				case 2:
					curModule.offset = Byte.parseByte( split[ i ] );
					break;
				case 3:
					curModule.dam = Short.parseShort( split[ i ] );
					
					cursor = i + 1;
					break;
				default:
					throw new RuntimeException( "Too many arguments for <DefaultModule> key word" );
				}
			}
		
		// Add current default modules and slots if have not added before return
		if( curModule != null ) curSlot.add( curModule );
		if( curSlot.size() > 0 ) this.slots.add( curSlot );
		return cursor;
	}
	
	@Override
	public void writeToTag( NBTTagList tag )
	{
		// Go through each slot
		for( int i = this.slots.size(); i-- > 0; )
		{
			// Skip if no default module in this slot
			if( this.slots.get( i ).size() == 0 ) continue;
			
			// Write each default module to this slot tag
			NBTTagList slotTag = ( NBTTagList ) tag.get( 1 + i );
			for( PreInstalledModules dm : this.slots.get( i ) )
			{
				NBTTagList moduleTag = dm.genTag();
				if( moduleTag != null )
					slotTag.appendTag( moduleTag );
			}
		}
	}
	
	@Nullable
	@Override
	public NBTTagList genTag()
	{
		MetaModular module = MetaModular.get( this.module );
		if( module == null )
		{
			this.log().error( "Default module <" + this.module + "> does not exist" );
			return null;
		}
		
		NBTTagList tag = module.genTag();
		module.$dam( tag, this.dam );
		module.$step( tag, this.step );
		module.$offset( tag, this.offset );
		this.writeToTag( tag );
		return null;
	}
}
