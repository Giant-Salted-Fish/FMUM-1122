package com.fmum.common.type;

import com.fmum.common.util.Messager;

@FunctionalInterface
public interface ParserFunc<T>
{
	default public void parse(String[] split, T type, Messager sourceName) {
		this.parse(split, type);
	}
	
	public void parse(String[] split, T type);
}