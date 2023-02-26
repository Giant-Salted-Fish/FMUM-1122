package com.mcwb.common.player;

import javax.annotation.Nullable;

import com.mcwb.common.MCWB;
import com.mcwb.common.item.IItem;
import com.mcwb.common.item.IItem.IUseContext;
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
	protected ItemStack mainStack = ItemStack.EMPTY;
	protected IItemType mainType = IItemType.VANILLA;
	protected IItem mainItem = IItem.EMPTY;
	protected int invSlot = -1; // TODO: better initialization?
	protected IUseContext mainUseContext = IItem.USE_CONTEXT;
	
	/// *** Stuffs for off-hand item *** ///
	protected ItemStack offStack = ItemStack.EMPTY;
	protected IItemType offType = IItemType.VANILLA;
	protected IItem offItem = IItem.EMPTY;
	protected IUseContext offUseContext = IItem.USE_CONTEXT;
	
	public PlayerPatch( EntityPlayer player ) { this.player = player; }
	
	public void tick()
	{
		final InventoryPlayer inv = this.player.inventory;
		
		/// *** Main hand stuff *** ///
		{
			final ItemStack stack = inv.getCurrentItem();
			final IItemType type = IItemTypeHost.getTypeOrDefault( stack );
			final IItem item = type.getContexted( stack );
			
			/// Check and fire callback for each condition
			if( inv.currentItem != this.invSlot || type != this.mainType )
			{
				this.executing = this.executing.onInHandItemChange( item );
				this.mainUseContext = item.onTakeOut(
					this.mainItem, this.player, EnumHand.MAIN_HAND
				);
			}
			else if( stack != this.mainStack )
			{
				// Actually still can be changing to another item but we literally has no way to \
				// distinguish those cases with the NBT update.
				this.executing = this.executing.onInHandStackChange( item );
				this.mainUseContext = item.onInHandStackChanged(
					this.mainItem, this.player, EnumHand.MAIN_HAND
				);
			}
			
			item.tickInHand( this.player, EnumHand.MAIN_HAND );
			this.mainStack = stack;
			this.mainType = type;
			this.mainItem = item; // Should always be updated. See {@link IItem}
			this.invSlot = inv.currentItem;
		}
		
		/// *** Off-hand stuff *** ///
		{
			final ItemStack stack = inv.offHandInventory.get( 0 );
			final IItemType type = IItemTypeHost.getTypeOrDefault( stack );
			final IItem item = type.getContexted( stack );
			
			if( type != this.offType )
				this.offUseContext = item.onTakeOut( this.offItem, this.player, EnumHand.OFF_HAND );
			else if( stack != this.offStack )
			{
				this.offUseContext = item.onInHandStackChanged(
					this.offItem, this.player, EnumHand.OFF_HAND
				);
			}
			
			this.offItem.tickInHand( this.player, EnumHand.OFF_HAND );
			this.offStack = stack;
			this.offType = type;
			this.offItem = item; // Always. See main hand part.
		}
		
		// Tick operation executing
		this.executing = this.executing.tick();
	}
	
	public final IUseContext getUseContext( EnumHand hand ) {
		return hand == EnumHand.MAIN_HAND ? this.mainUseContext : this.offUseContext;
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
		final IItem mitem = this.offItem;
		
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
