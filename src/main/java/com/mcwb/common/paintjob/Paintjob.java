package com.mcwb.common.paintjob;

import java.util.Collections;
import java.util.Set;

import com.google.gson.JsonDeserializer;
import com.google.gson.annotations.SerializedName;
import com.mcwb.common.MCWB;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.load.TexturedMeta;
import com.mcwb.common.meta.IMeta;

import net.minecraft.util.ResourceLocation;

public class Paintjob extends TexturedMeta implements IPaintjob
{
	public static final JsonDeserializer< IPaintjob >
		ADAPTER = ( json, typeOfT, context ) -> MCWB.GSON.fromJson( json, Paintjob.class );
	
	public static final BuildableLoader< IMeta >
		LOADER = new BuildableLoader<>( "paintjob", ExternalPaintjob.class );
	
	@SerializedName( value = "materials", alternate = "cost" )
	protected Set< PaintjobMaterial > materials = Collections.emptySet();
	
	@Override
	public ResourceLocation texture() { return this.texture; }
	
	@Override
	protected IMeta loader() { return LOADER; }
	
	// TODO: material
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
