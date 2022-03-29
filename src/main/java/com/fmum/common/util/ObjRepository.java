package com.fmum.common.util;

@FunctionalInterface
public interface ObjRepository<T> { public T fetch(String identifier); }