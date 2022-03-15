package com.fmum.common.pack;

import java.io.File;

public abstract class ZipContentPack extends LocalContentProvider
{
	public ZipContentPack(File zip)
	{
		super(zip);
	}
}
