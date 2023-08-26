package com.fmum.common.paintjob;

import com.fmum.common.load.TexturedType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.Set;

public class Paintjob extends TexturedType implements IPaintjob
{
	protected Set< PaintjobMaterial > cost = Collections.emptySet();
	
	@Override
	@SideOnly( Side.CLIENT )
	public ResourceLocation texture() {
		return this.texture;
	}
	
	@Override
	protected String _typeHint() {
		return "PAINTJOB";
	}
	
	// TODO: paintjob material
	public static class PaintjobMaterial
	{
		public String item;
		public short meta;
		public short amount = 1;
	}
}
