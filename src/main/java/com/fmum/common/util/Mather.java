package com.fmum.common.util;

public abstract class Mather
{
	private Mather() { }
	
	public static double clamp(double value, double min, double max) {
		return value < min ? min : value > max ? max : value;
	}
}
