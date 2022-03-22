package com.fmum.common;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

public final class FMUMClassLoader extends URLClassLoader
{
	public static final FMUMClassLoader INSTANCE = new FMUMClassLoader();
	
	private final HashMap<String, byte[]> source = new HashMap<>();
	
	private FMUMClassLoader() {
		super(new URL[0], net.minecraft.server.MinecraftServer.class.getClassLoader());
	}
	
	@Override
	public void addURL(URL url) { super.addURL(url); }
	
	public byte[] putSource(String key, byte[] value) {
		return this.source.put(key, value);
	}
	
	public void clear() { source.clear(); }
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException
	{
		byte[] bytes = this.source.get(name);
		return(
			bytes != null
			? this.defineClass(name, bytes, 0, bytes.length)
			: super.findClass(name)
		);
	}
}
