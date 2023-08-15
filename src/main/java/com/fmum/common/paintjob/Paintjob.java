package com.fmum.common.paintjob;

import com.google.gson.annotations.SerializedName;

public class Paintjob implements IPaintjob
{
	protected String name;
	
	@Override
	public String toString() {
		return "PAINTJOB::" + this.name;
	}
	
	// TODO: paintjob material
	public static class PaintjobMaterial
	{
		@SerializedName( value = "item", alternate = { "required", "material" } )
		public String item;
		public short meta;
		public short amount = 1;
	}
}
