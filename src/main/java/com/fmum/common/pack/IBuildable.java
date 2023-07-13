package com.fmum.common.pack;

import javafx.scene.layout.TilePaneBuilder;

@FunctionalInterface
public interface IBuildable< T >
{
	T build();
}
