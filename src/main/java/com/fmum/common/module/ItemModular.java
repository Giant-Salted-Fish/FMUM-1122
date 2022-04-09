package com.fmum.common.module;

import com.fmum.client.FMUMClient;
import com.fmum.client.KeyManager.Key;
import com.fmum.client.module.OpModification;
import com.fmum.common.network.PacketModuleTagInit;
import com.fmum.common.paintjob.ItemPaintable;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ItemModular extends ItemPaintable
{
	@Override
	public TypeModular getType();
	
	@Override
	@SideOnly(Side.CLIENT)
	default void tick(ItemStack stack)
	{
		if(TagModular.validateTag(stack))
			ItemPaintable.super.tick(stack);
		else FMUMClient.netHandler.sendToServer(new PacketModuleTagInit());
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	default void keyNotify(Key key)
	{
		ItemPaintable.super.keyNotify(key);
		
		switch(key)
		{
		case SELECT_UP:
			OpModification.INSTANCE.upKeyDown = true;
			break;
		case SELECT_DOWN:
			OpModification.INSTANCE.downKeyDown = true;
			break;
		case SELECT_LEFT:
			OpModification.INSTANCE.leftKeyDown = true;
			break;
		case SELECT_RIGHT:
			OpModification.INSTANCE.rightKeyDown = true;
			break;
		case SELECT_CONFIRM:
			OpModification.INSTANCE.confirmKeyDown = true;
			break;
		case SELECT_CANCEL:
			OpModification.INSTANCE.cancelKeyDown = true;
			break;
		case SELECT_TOGGLE:
			OpModification.INSTANCE.toggleKeyDown = true;
			break;
		case TOGGLE_MODIFY:
		case CO_TOGGLE_MODIFY:
			final OpModification modify = OpModification.INSTANCE;
			if(FMUMClient.operating == modify)
				modify.progressor = -modify.progressor;
			else FMUMClient.tryLaunchOp(modify);
			break;
		default:;
		}
	}
}
