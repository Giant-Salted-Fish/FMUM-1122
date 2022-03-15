package com.fmum.common.type;

@FunctionalInterface
public interface ParserFunc<T>
{
	default public void parse(String[] split, T type, String sourceName) {
		this.parse(split, type);
	}
	
	public void parse(String[] split, T type);
}