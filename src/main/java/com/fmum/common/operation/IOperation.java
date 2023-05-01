package com.fmum.common.operation;

import com.fmum.common.item.IEquippedItem;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * <p> Represents something that the player is doing with the item in main hand. </p>
 * 
 * <p> In default your implementation should guarantee that {@link #terminate()} is always called
 * when the operation is left for execution. </p>
 * 
 * <p> Notice that we supply {@link EntityPlayer} for every life cycle method of operation. This is
 * because we know the outer caller should have the reference to the corresponding player and this
 * helps to eliminate the memory cost to have the identical reference to the same instance. On the
 * other side, this also helps to prevent others from calling these methods as they do not have the
 * corresponding context. </p>
 * 
 * @author Giant_Salted_Fish
 */
public interface IOperation
{
	/**
	 * Placeholder operation instance. Represents that no operation is currently running.
	 */
	static final IOperation NONE = new IOperation()
	{
		@Override
		public IOperation onStackUpdate( IEquippedItem< ? > newEquipped,  EntityPlayer player ) {
			return this;
		}
		
		@Override
		public String toString() { return "Operation::NONE"; }
	};
	
	default float progress() { return 1F; }
	
	@SideOnly( Side.CLIENT )
	default float getProgress( float smoother ) { return 1F; }
	
	/**
	 * Prepare for the execution.
	 */
	default IOperation launch( EntityPlayer player ) { return this; }
	
	/**
	 * For progressive operation that can reverse its progress. For example you can enter sprint
	 * stance and keep it then quit it after a while.
	 */
	default IOperation toggle( EntityPlayer player ) { return this; }
	
	/**
	 * <p> Called when the outer requires to terminate this operation. Be aware that the executing
	 * operation may refuse to terminate. </p>
	 * 
	 * @return {@link #NONE} if execution is aborted.
	 */
	default IOperation terminate( EntityPlayer player ) { return NONE; }
	
	/**
	 * Tick this operation.
	 * 
	 * @return {@link #NONE} if this operation has complete. {@code this} otherwise.
	 */
	default IOperation tick( EntityPlayer player ) { return this; }
	
	/**
	 * Called when another operation is requesting to launch during the execution of this operation.
	 * You can call {@link IOperation#launch(EntityPlayer)} on the new operation if this operation
	 * should give up its execution and let new operation to run.
	 * 
	 * @param op New operation that requests to launch.
	 * @return {@link IOperation} that should be executed after this call.
	 */
	default IOperation onOtherTryLaunch( IOperation op, EntityPlayer player ) {
		return op.launch( player );
	}
	
	/**
	 * Called when the item in player's main hand has changed.
	 * 
	 * @return {@link #NONE} if this operation should terminate on item switch.
	 */
	default IOperation onItemChange( IEquippedItem< ? > newEquipped, EntityPlayer player ) {
		return this.terminate( player );
	}
	
	/**
	 * Called when the corresponding stack of the current item has been updated.
	 * 
	 * @return {@link #NONE} if this operation should terminate on stack update.
	 */
	default IOperation onStackUpdate( IEquippedItem< ? > newEquipped, EntityPlayer player ) {
		throw new RuntimeException( this.toString() );
	}
}
