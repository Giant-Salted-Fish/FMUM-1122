package com.fmum.gunpart;

import com.fmum.module.IModifyContext;
import com.fmum.module.IModifyPreview;
import com.fmum.module.IModule;
import com.fmum.render.IAnimator;
import com.fmum.render.IRenderCallback;
import com.mojang.realmsclient.util.Pair;
import gsf.util.animation.IPoseSetup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;
import java.util.Optional;

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
		int base_slot_idx,
		IAnimator animator,
		Collection< IRenderCallback > render_queue
	);
	
	@SideOnly( Side.CLIENT )
	IPoseSetup IGunPart$getRenderSetup( IGunPart gun_part, int slot_idx );
}
