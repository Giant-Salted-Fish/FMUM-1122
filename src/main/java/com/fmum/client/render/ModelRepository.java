package com.fmum.client.render;

import com.fmum.common.util.ObjRepository;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
@FunctionalInterface
public interface ModelRepository extends ObjRepository< Renderable > { }
