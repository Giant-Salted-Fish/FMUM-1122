package com.fmum.common.gun;

import com.fmum.common.item.IItem;
import com.fmum.common.module.IModule;
import com.fmum.common.paintjob.IPaintable;

public interface IGunPart< T extends IGunPart< ? extends T > >
	extends IItem, IModule< T >, IPaintable
{

}
