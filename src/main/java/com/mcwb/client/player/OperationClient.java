package com.mcwb.client.player;

import com.mcwb.common.IAutowirePacketHandler;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.IOperationController;
import com.mcwb.common.operation.Operation;
import com.mcwb.util.Animation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public abstract class OperationClient< T extends IEquippedItem< ? > > extends Operation< T >
	implements IAutowirePacketHandler
{
	protected final Animation animation;
	
	public OperationClient(
		T equipped,
		IOperationController controller,
		Animation animtion
	) {
		super( equipped, controller );
		
		this.animation = animtion;
	}
	
	@Override
	public IOperation launch( EntityPlayer player )
	{
		this.equipped.animator().playAnimation( this.animation );
		return this;
	}
	
	@Override
	public IOperation terminate( EntityPlayer player )
	{
//		this.sendToServer( message );
		this.equipped.animator().playAnimation( Animation.NONE );
		return NONE;
	}
}
