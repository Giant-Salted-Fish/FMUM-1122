package com.fmum.common.module;

import com.fmum.common.type.ItemPaintable;

public interface ItemModular extends ItemPaintable
{
	@Override
	public TypeModular getType();
}
