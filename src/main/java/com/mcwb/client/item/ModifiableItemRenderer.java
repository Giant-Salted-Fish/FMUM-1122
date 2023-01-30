package com.mcwb.client.item;

import com.mcwb.client.player.ModifyOp;
import com.mcwb.common.item.IContextedItem;
import com.mcwb.util.Vec3f;

import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public abstract class ModifiableItemRenderer< T extends IContextedItem > extends ItemRenderer< T >
{
	protected static final Vec3f MODIFY_POS = new Vec3f( 0F, 0F, 20F / 16F );
	
	protected Vec3f modifyPos = MODIFY_POS;
	
	protected abstract ModifyOp< ? > modifyOp();
	
	@Override
	protected abstract ModifiableItemAnimatorState animator( EnumHand hand );
}
