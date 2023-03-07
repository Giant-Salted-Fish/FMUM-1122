package com.mcwb.devtool;

import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.mcwb.util.Vec3f;

public class BBAnimationExport
{
	public static void main( String[] args )
	{
		final GsonBuilder builder = new GsonBuilder();
		builder.setLenient();
		builder.setPrettyPrinting();
		final JsonDeserializer< Vec3f > vecAdapter = ( json, typeOfT, context ) -> {
			final JsonArray arr = json.getAsJsonArray();
			final Vec3f vec = new Vec3f();
			vec.z = arr.get( 2 ).getAsFloat();
			vec.y = arr.get( 1 ).getAsFloat();
			vec.x = arr.get( 0 ).getAsFloat();
			return vec;
		};
		builder.registerTypeAdapter( Vec3f.class, vecAdapter );
		
		final Gson gson = builder.create();
		
		final String path = args.length > 0 ? args[ 0 ] : "z-dev/model.animation.json";
		try( FileReader reader = new FileReader( new File( path ) ) )
		{
			final BBAnimationExport ani = gson.fromJson( reader, BBAnimationExport.class );
			final boolean a = false;
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	public String format_version;
	public Map< String, BBAnimation > animations = Collections.emptyMap();
	
	@Override
	public String toString() { return this.animations.keySet().toString(); }
	
	public static class BBAnimation
	{
		public boolean loop = false;
		public float animation_length;
		public Map< String, Bone > bones = Collections.emptyMap();
		
		@Override
		public String toString() {
			return "len:" + this.animation_length + "|" + this.bones.keySet();
		}
	}
	
	public static class Bone
	{
		public String parent;
		public Map< Float, Vec3f > rotation = Collections.emptyMap();
		public Map< Float, Vec3f > position = Collections.emptyMap();
		public Map< Float, Float > alpha = Collections.emptyMap();
		
		@Override
		public String toString() {
			return "rot:" + this.rotation.size() + " pos:" + this.position.size();
		}
	}
}
