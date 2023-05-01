package com.fmum.common.paintjob;

import java.util.Collections;
import java.util.Set;

import com.fmum.common.FMUM;
import com.fmum.common.load.TexturedMeta;
import com.fmum.common.meta.IMeta;
import com.google.gson.JsonDeserializer;
import com.google.gson.annotations.SerializedName;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Simply implementation of {@link IPaintjob}
 * 
 * @author Giant_Salted_Fish
 */
public class Paintjob extends TexturedMeta implements IPaintjob
{
	public static final JsonDeserializer< IPaintjob >
		ADAPTER = ( json, typeOfT, context ) -> FMUM.GSON.fromJson( json, Paintjob.class );
	
	@SerializedName( value = "materials", alternate = "cost" )
	protected Set< PaintjobMaterial > materials = Collections.emptySet();
	
	@Override
	@SideOnly( Side.CLIENT )
	public ResourceLocation texture() { return this.texture; }
	
	@Override
	protected IMeta descriptor() { return () -> "PAINTJOB"; }
	
	// TODO: paintjob material
	public static class PaintjobMaterial
	{
		@SerializedName( value = "item", alternate = { "required", "material" } )
		public String item;
		
		@SerializedName( value = "damage", alternate = "meta" )
		public short damage;
		
		@SerializedName( value = "amount", alternate = { "count", "size" } )
		public short amount = 1;
	}
}
