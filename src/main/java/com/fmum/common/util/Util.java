package com.fmum.common.util;

public abstract class Util
{
	private Util() { }
	
	public static double clamp(double value, double min, double max) {
		return value < min ? min : value > max ? max : value;
	}
	
	public static String spliceClassPath(String... pathFragments)
	{
		String classPath = pathFragments[pathFragments.length - 1];
		for(
			int i = pathFragments.length - 1;
			--i >= 0;
			classPath = pathFragments[i] + "." + classPath
		);
		return(
			classPath.endsWith(".class")
			? classPath.substring(0, classPath.length() - ".class".length())
			: classPath
		);
	}
	
	public static String splice(String[] split, int head) {
		return splice(split, head, split.length);
	}
	
	public static String splice(String[] split, int head, int tail)
	{
		String s = "";
		while(--tail >= head) s = split[tail] + " " + s;
		return s;
	}
}
