package com.mcwb.common.player;

import javax.annotation.Nullable;

import com.mcwb.common.MCWB;
import com.mcwb.common.item.IInUseItem;
import com.mcwb.common.item.IItem;
import com.mcwb.common.item.IItemType;
import com.mcwb.common.item.IItemTypeHost;
import com.mcwb.common.operation.IOperation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

/**
 * Additional patch that added to the player to do extra logic required by {@link MCWB}
 * 
 * @author Giant_Salted_Fish
 */
public class PlayerPatch implements ICapabilityProvider
{
	@CapabilityInject( PlayerPatch.class )
	private static final Capability< PlayerPatch > CAPABILITY = null;
	
	/**
	 * Host player of this patch
	 */
	protected final EntityPlayer player;
	
	/**
	 * Operation that is currently executing. {@link IOperation#NONE} if is idle.
	 */
	protected IOperation executing = IOperation.NONE;
	
	/// *** Stuffs for main hand item *** ///
	protected int invSlot = -1; // TODO: better initialization?
	protected ItemStack mainStack = ItemStack.EMPTY;
	protected IItemType mainType = IItemType.VANILLA;
	protected IInUseItem mainItem = IInUseItem.EMPTY;
	
	/// *** Stuffs for off-hand item *** ///
	protected ItemStack offStack = ItemStack.EMPTY;
	protected IItemType offType = IItemType.VANILLA;
	protected IInUseItem offItem = IInUseItem.EMPTY;
	
	public PlayerPatch( EntityPlayer player ) { this.player = player; }
	
	public void tick()
	{
		final InventoryPlayer inv = this.player.inventory;
		
		/// *** Main hand stuff *** ///
		{
			final EnumHand hand = EnumHand.MAIN_HAND;
			final ItemStack stack = inv.getCurrentItem();
			final IItemType type = IItemTypeHost.getTypeOrDefault( stack );
			
			/// Check and fire callback for each condition
			if( inv.currentItem != this.invSlot || type != this.mainType )
			{
				final IItem item = type.getContexted( stack );
				this.mainItem = item.onTakeOut( this.mainItem, this.player, hand );
				this.executing = this.executing.onInHandItemChange( this.mainItem );
				
				this.invSlot = inv.currentItem;
				this.mainStack = stack;
				this.mainType = type;
			}
			else if( stack != this.mainStack )
			{
				// Actually still can be changing to another item but we literally has no way to \
				// distinguish those cases with the NBT update.
				final IItem item = type.getContexted( stack );
				this.mainItem = item.onInHandStackChanged( this.mainItem, this.player, hand );
				this.executing = this.executing.onInHandStackChange( this.mainItem );
				
				this.mainStack = stack;
			}
			
			this.mainItem.tickInHand( this.player, hand );
		}
		
		/// *** Off-hand stuff *** ///
		{
			final EnumHand hand = EnumHand.OFF_HAND;
			final ItemStack stack = inv.offHandInventory.get( 0 );
			final IItemType type = IItemTypeHost.getTypeOrDefault( stack );
			
			if( type != this.offType )
			{
				final IItem item = type.getContexted( stack );
				this.offItem = item.onTakeOut( this.offItem, this.player, hand );
				
				this.offStack = stack;
				this.offType = type;
			}
			else if( stack != this.offStack )
			{
				final IItem item = type.getContexted( stack );
				this.offItem = item.onInHandStackChanged( this.offItem, this.player, hand );
				
				this.offStack = stack;
			}
			
			this.offItem.tickInHand( this.player, EnumHand.OFF_HAND );
		}
		
		// Tick operation executing
		this.executing = this.executing.tick();
	}
	
	public final IOperation executing() { return this.executing; }
	
	public final IOperation tryLaunch( IOperation op ) {
		return this.executing = this.executing.onOtherTryLaunch( op );
	}
	
	public final IOperation ternimateExecuting() {
		return this.executing = this.executing.terminate();
	}
	
	public final IOperation toggleExecuting() { return this.executing = this.executing.toggle(); }
	
	public void trySwapHand()
	{
		if( this.player.isSpectator() || this.mainItem.onSwapHand( this.player ) ) return;
		
		final InventoryPlayer inv = this.player.inventory;
		inv.setInventorySlotContents( this.invSlot, this.offStack );
		inv.offHandInventory.set( 0, this.mainStack );
		
		final ItemStack mstack = this.offStack;
		final IItemType mtype = this.offType;
		final IInUseItem mitem = this.offItem;
		
		this.offStack = this.mainStack;
		this.offType = this.mainType;
		this.offItem = this.mainItem;
		
		this.mainStack = mstack;
		this.mainType = mtype;
		this.mainItem = mitem;
		
		this.executing = this.executing.onSwapHand( mitem );
	}
	
	@Override
	public final boolean hasCapability( Capability<?> capability, @Nullable EnumFacing facing ) {
		return capability == CAPABILITY;
	}
	
	@Override
	public final < T > T getCapability( Capability< T > capability, @Nullable EnumFacing facing ) {
		return CAPABILITY.cast( this );
	}
	
	public static PlayerPatch get( EntityPlayer player ) {
		return player.getCapability( CAPABILITY, null );
	}
}
