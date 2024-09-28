package com.fmum.gun;

import com.fmum.gunpart.GunPartType;
import com.fmum.item.IItemType;
import com.fmum.load.IContentLoader;
import com.fmum.module.IModuleType;
import com.fmum.paintjob.IPaintableType;

public class GunType extends GunPartType
{
	public static final IContentLoader< GunType > LOADER = IContentLoader.of(
		GunType::new,
		IItemType.REGISTRY, IModuleType.REGISTRY, IPaintableType.REGISTRY
	);
}
