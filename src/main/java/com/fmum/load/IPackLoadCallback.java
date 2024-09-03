package com.fmum.load;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IPackLoadCallback
{
	void onPreLoad( IPreLoadContext ctx );
	
	void onLoad( ILoadContext ctx );
	
	void onPostLoad( IPostLoadContext ctx );
	
	@SideOnly( Side.CLIENT )
	void onMeshLoad( IMeshLoadContext ctx );
}
