package com.fmum.common.util;

@FunctionalInterface
public interface InstanceRepository<T> { public T fetch(String identifier); }