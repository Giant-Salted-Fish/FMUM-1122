package com.fmum.common.util;

public abstract class Util
{
	private Util() { }
	
	public static double clamp(double value, double min, double max) {
		return value < min ? min : value > max ? max : value;
	}
	
	/**
	 * Get intersection of the given line and plane
	 * 
	 * @param a1 X-position of the line 
	 * @param b1 Y-position of the line
	 * @param c1 Z-position of the line
	 * @param A1 X-direction of the line
	 * @param B1 Y-direction of the line
	 * @param C1 Z-direction of the line
	 * @param a2 X-position of the plane
	 * @param b2 Y-position of the plane
	 * @param c2 Z-position of the plane
	 * @param A2 X-normal of the plane
	 * @param B2 Y-normal of the plane
	 * @param C2 Z-normal of the plane
	 * @param dst Intersection will be saved into this vector
	 */
	public static void getLinePlaneIntersection(
		double a1, double b1, double c1,
		double A1, double B1, double C1, 
		double a2, double b2, double c2,
		double A2, double B2, double C2,
		Vec3 dst
	) {
		a2 = ((a2 - a1) * A2 + (b2 - b1) * B2 + (c2 - c1) * C2) / (A1 * A2 + B1 * B2 + C1 * C2);
		dst.set(
			a1 + A1 * a2,
			b1 + B1 * a2,
			c1 + C1 * a2
		);
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
