package com.fmum.common.module;

import java.util.LinkedList;

import com.fmum.common.FMUM;

import net.minecraft.nbt.NBTTagList;

public final class DefaultModules
{
	public String type;
	
	public LinkedList<LinkedList<DefaultModules>> slots = new LinkedList<>();
	
	public short dam;
	
	public short step;
	public byte offset;
	
	DefaultModules(String type) { this.type = type; }
	
	/**
	 * Parse default modules from given split
	 * 
	 * @param split String array to parse default attachments from
	 * @param cursor Should be set to the index right after '['
	 * @return Cursor position after parsing
	 */
	public int parse(String[] split, int cursor)
	{
		DefaultModules curModule = null;
		LinkedList<DefaultModules> curSlot = new LinkedList<>();
		for(int i = cursor; i < split.length; ++i)
			switch(split[i])
			{
			// Switch to next slot
			case "|":
				// Save previous one if yet not saved
				if(curModule != null)
				{
					curSlot.add(curModule);
					curModule = null;
				}
				
				this.slots.add(curSlot);
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
				int after = curModule.parse(split, i + 1);
				cursor += after - i + 1;
				i = after;
				break;
			
			// End symbol
			case "]":
				cursor = i;
				i = split.length;
				break;
			
			default:
				switch(i - cursor)
				{
				case 0:
					if(curModule != null)
						curSlot.add(curModule);
					
					curModule = new DefaultModules(split[i]);
					break;
				case 1:
					curModule.step = Short.parseShort(split[i]);
					break;
				case 2:
					curModule.offset = Byte.parseByte(split[i]);
					break;
				case 3:
					curModule.dam = Short.parseShort(split[i]);
					
					cursor = i + 1;
					break;
				default:
					throw new RuntimeException(FMUM.proxy.format("fmum.toomanykeywordargs"));
				}
			}
		
		// Add current default modules and slots if have not added before return
		if(curModule != null) curSlot.add(curModule);
		if(curSlot.size() > 0) this.slots.add(curSlot);
		return cursor;
	}
	
	public void writeToTag(NBTTagList tag)
	{
		// Go through each slot
		for(int i = this.slots.size(); --i >= 0; )
		{
			// Only add module tag if has default modules on this slot
			if(this.slots.get(i).size() == 0) continue;
			
			// Write each default module to this slot tag
			NBTTagList slotTag = (NBTTagList)tag.get(i + 1);
			for(DefaultModules dm : this.slots.get(i))
			{
				NBTTagList moduleTag = dm.genTag();
				if(moduleTag != null)
					slotTag.appendTag(moduleTag);
			}
		}
	}
	
	private NBTTagList genTag()
	{
		TypeModular module = TypeModular.modules.get(this.type);
		if(module == null)
		{
			FMUM.log.error(FMUM.proxy.format("fmum.defaultmodulenotfound", this.type));
			return null;
		}
		
		NBTTagList tag = module.genTag(this.dam, this.step, this.offset);
		this.writeToTag(tag);
		return tag;
	}
}
