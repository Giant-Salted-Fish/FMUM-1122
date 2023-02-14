package com.mcwb.common.player;

import javax.annotation.Nullable;

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

public class PlayerPatch implements ICapabilityProvider
{
	@CapabilityInject( PlayerPatch.class )
	public static final Capability< PlayerPatch > CAPABILITY = null;
	
	/**
	 * Host player of this patch
	 */
	protected final EntityPlayer player;
	
	/**
	 * Current operation that is executing
	 */
	protected IOperation operating = IOperation.NONE;
	
	/// Stuffs for main hand item ///
	protected ItemStack mainStack = ItemStack.EMPTY;
	protected IItemType mainType = IItemType.VANILLA;
	protected IItem mainItem = IItem.EMPTY;
	protected int invSlot = -1; // TODO: better initialize?
	
	/// Stuffs for off-hand item ///
	protected ItemStack offStack = ItemStack.EMPTY;
	protected IItemType offType = IItemType.VANILLA;
	protected IItem offItem = IItem.EMPTY;
	
	public PlayerPatch( EntityPlayer player ) { this.player = player; }
	
	public void tick()
	{
		final InventoryPlayer inv = this.player.inventory;
		
		/// Main hand stuff ///
		{
			final ItemStack stack = inv.getCurrentItem();
			final IItemType type = IItemTypeHost.getType( stack );
			final IItem item = type.getContexted( stack );
			
			// Check selected slot switch
			if( inv.currentItem != this.invSlot )
			{
				// Fire events for inventory slot change
//				this.prevMainItem.onPutAway( item, this.player );
				this.operating = this.operating
					.onInvSlotChange( item, inv.currentItem, this.invSlot );
				item.onTakeOut( this.mainItem, this.player, EnumHand.MAIN_HAND );
			}
			else if( type != this.mainType )
			{
				// Swap hand will be handled separately hence can only be something like set in \
				// inventory. In this case, put away is not necessary.
				// TODO: swap hand is only handled in client side so check if it is valid on server side
//				this.prevMainItem.onPutAway( item, this.player, EnumHand.MAIN_HAND );
				
				this.operating = this.operating.onHoldingTypeChange( item );
				item.onTakeOut( this.mainItem, this.player, EnumHand.MAIN_HAND );
			}
			else if( stack != this.mainStack )
			{
				// Actually still can be switching to another item but we literally has no way to \
				// distinguish those cases with simple NBT update.
				this.operating = this.operating.onHoldingStackChange( item );
			}
			
			// For convenience, update all previous variables here. Notice that context is special \
			// as it could change even if all three conditional branch all miss.
			this.mainStack = stack;
			this.mainType = type;
			this.mainItem = item; // Special
			this.invSlot = inv.currentItem;
		}
		
		/// Off-hand stuff ///
		{
			final ItemStack stack = inv.offHandInventory.get( 0 );
			final IItemType type = IItemTypeHost.getType( stack );
			final IItem item = type.getContexted( stack );
			
			if( type != this.offType )
				item.onTakeOut( this.offItem, this.player, EnumHand.OFF_HAND );
//			else if( offStack != this.prevOffStack ) // TODO: maybe call stack change for off-hand item
			
			this.offStack = stack;
			this.offItem = item;
			this.offType = type;
		}
		
		// Tick current operating
		this.operating = this.operating.tick();
	}
	
	public void trySwapHand()
	{
		if( !this.player.isSpectator() && this.mainItem.onSwapHand( this.player ) )
			this.doSwapHand();
	}
	
	public IOperation operating() { return this.operating; }
	
	public IOperation tryLaunch( IOperation op ) {
		return this.operating = this.operating.onOtherTryLaunch( op );
	}
	
	public IOperation ternimateOperating() { return this.operating = this.operating.terminate(); }
	
	public IOperation toggleOperating() { return this.operating = this.operating.toggle(); }
	
	@Override
	public boolean hasCapability( Capability< ? > capability, @Nullable EnumFacing facing ) {
		return capability == CAPABILITY;
	}
	
	@Override
	public < T > T getCapability( Capability< T > capability, @Nullable EnumFacing facing ) {
		return CAPABILITY.cast( this );
	}
	
	protected void doSwapHand()
	{
		final InventoryPlayer inv = this.player.inventory;
		inv.setInventorySlotContents( this.invSlot, this.offStack );
		inv.offHandInventory.set( 0, this.mainStack );
		
		final ItemStack mstack = this.offStack;
		final IItemType mtype = this.offType;
		final IItem mitem = this.offItem;
		
		this.offStack = this.mainStack;
		this.offType = this.mainType;
		this.offItem = this.mainItem;
		
		this.mainStack = mstack;
		this.mainType = mtype;
		this.mainItem = mitem;
		
		this.operating = this.operating.onSwapHand( mitem );
	}
	
	public static PlayerPatch get( EntityPlayer player ) {
		return player.getCapability( CAPABILITY, null );
	}
}
