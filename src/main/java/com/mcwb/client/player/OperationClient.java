package com.mcwb.client.player;

import java.util.function.Consumer;

import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.IOperationController;
import com.mcwb.common.operation.Operation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public abstract class OperationClient< T extends IEquippedItem< ? > > extends Operation< T >
{
	protected final Consumer< T > ternimateCallback;
	
	public OperationClient(
		T equipped,
		IOperationController controller,
		Consumer< T > ternimateCallback
	) {
		super( equipped, controller );
		
		this.ternimateCallback = ternimateCallback;
	}
	
	@Override
	public IOperation terminate( EntityPlayer player )
	{
		this.ternimateCallback.accept( this.equipped );
		return NONE;
	}
}
