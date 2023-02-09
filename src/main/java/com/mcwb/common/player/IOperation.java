package com.mcwb.common.player;

import com.mcwb.common.item.IItem;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IOperation
{
	/**
	 * Default operation instance that simply do nothing
	 */
	public static final IOperation NONE = new IOperation() {
		@Override
		public String toString() { return "Operation::None"; }
	};
	
	public default float progress() { return 1F; }
	
	@SideOnly( Side.CLIENT )
	public default float getProgress( float smoother ) { return 1F; }
	
	/// Methods for operation life cycle ///
	/**
	 * Prepare the execution
	 */
	public default IOperation launch( IOperation oldOp ) { return this; }
	
	/**
	 * For progressive operation that can reverse its progress. For example you can enter sprint
	 * stance and keep it then quit it after a while.
	 */
	public default IOperation toggle() { return this; }
	
	/**
	 * Called when the outer requires to terminate this operation. Be aware that the executing
	 * operation may refuse to terminate.
	 * 
	 * @return {@link #NONE} if execution is aborted
	 */
	public default IOperation terminate() { return NONE; }
	
	/**
	 * Tick this operation
	 * 
	 * @return {@link #NONE} if this operation has complete. {@code this} otherwise.
	 */
	public default IOperation tick() { return this; }
	
	/**
	 * Callback when another operation is requesting to launch during the execution of this
	 * operation. You can call {@link IOperation#launch(IOperation)} on the new operation if this
	 * operation should give up its execution and let new operation to run.
	 * 
	 * @param op New operation that requests to launch
	 * @return {@link IOperation} that should be executed after this call
	 */
	public default IOperation onOtherTryLaunch( IOperation op ) { return op.launch( this ); }
	
	/**
	 * Callback when player changes the selected inventory slot
	 * @return {@link #NONE} if this operation should terminate on item switch
	 */
	public default IOperation onInvSlotChange( IItem newItem, int newSlot, int oldSlot ) {
		return this.terminate();
	}
	
	/**
	 * <p> Called when the {holding stack} != {last tick holding stack}. </p>
	 * 
	 * <p> This can be triggered in two ways: </p>
	 * <ol>
	 *     <li> Item stack in current selected slot being replaced with another stack(drop current
	 *     item, swap hand, click to replace stack in inventory gui, etc). </li>
	 *     
	 *     <li> NBT tag of the stack being updated </li>
	 * </ol>
	 * 
	 * @warning
	 *     This method is usually being forgotten to handle with which could cause weird termination
	 *     and duplicate launch problems
	 * @return {@link #NONE} if this operation should terminate on item change
	 */
	public default IOperation onHoldingStackChange( IItem newItem ) {
		return this.terminate();
	}
}
