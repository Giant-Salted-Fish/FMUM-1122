package com.fmum.gunpart;

import com.fmum.item.IItem;
import com.fmum.module.IModifyContext;
import com.fmum.module.IModifyPreview;
import com.fmum.module.IModule;
import com.fmum.render.IPreparedRenderer;
import com.mojang.realmsclient.util.Pair;
import gsf.util.animation.IAnimator;
import gsf.util.render.IPose;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Gun part does not necessarily need to be an item. It can also be an installed
 * module which does not have a corresponding {@link ItemStack}.
 */
public interface IGunPart extends IModule
{
	@Override
	Optional< ? extends IGunPart > getBase();
	
	@Override
	IGunPart getInstalled( int slot_idx, int module_idx );
	
	int getOffsetCount();
	
	int getOffset();
	
	int getStep();
	
	int getStepCount( int slot_idx );
	
	IModifyPreview< Pair< Integer, Integer > > trySetOffsetAndStep( int offset, int step );
	
	
	@SideOnly( Side.CLIENT )
	IModule IGunPart$createSelectionProxy( IModifyContext ctx );
	
	@SideOnly( Side.CLIENT )
	void IGunPart$prepareRender(
		IPose base_pose,
		IAnimator animator,
		Consumer< IPreparedRenderer > registry
	);
	
	
	static IGunPart from( IItem item )
	{
		final Optional< IModule > opt = item.lookupCapability( IModule.CAPABILITY );
		return ( IGunPart ) opt.orElseThrow( IllegalArgumentException::new );
	}
}
