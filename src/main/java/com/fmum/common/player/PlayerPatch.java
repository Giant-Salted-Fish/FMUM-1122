package com.fmum.common.player;

import com.fmum.common.item.IEquippedItem;
import com.fmum.common.item.IItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerPatch implements ICapabilityProvider
{
	protected IItem main_item = IItem.VANILLA;
	protected IEquippedItem< ? > main_equipped = IEquippedItem.VANILLA;
	
	protected IItem off_item = IItem.VANILLA;
	protected IEquippedItem< ? > off_equipped = IEquippedItem.VANILLA;
	
	protected IOperation operation = IOperation.NONE;
	
	public void tick( EntityPlayer player )
	{
		final InventoryPlayer inv = player.inventory;
		
		// Main hand update.
		{
			final EnumHand hand = EnumHand.MAIN_HAND;
			final ItemStack stack = inv.getCurrentItem();
			final IItem item = IItem.getFromOrDefault( stack );
			
			final boolean is_equipped_changed =
				item.stackId() != this.main_item.stackId();
			if ( is_equipped_changed )
			{
				this.main_item = item;
				this.main_equipped = item.onTakeOut( player, hand );
				this.operation = this.operation.onEquippedChanged(
					this.main_equipped, player );
			}
			
			this.main_equipped.tickInHand( item, player, hand );
		}
		
		// Off-hand update.
		{
			final EnumHand hand = EnumHand.OFF_HAND;
			final ItemStack stack = inv.offHandInventory.get( 0 );
			final IItem item = IItem.getFromOrDefault( stack );
			
			final boolean is_equipped_changed =
				item.stackId() != this.off_item.stackId();
			if ( is_equipped_changed )
			{
				this.off_item = item;
				this.off_equipped = item.onTakeOut( player, hand );
			}
			
			this.off_equipped.tickInHand( item, player, hand );
		}
		
		this.operation = this.operation.tick( player );
	}
	
	public final IOperation operation() { return this.operation; }
	
	
	@CapabilityInject( PlayerPatch.class )
	private static final Capability< PlayerPatch > CAPABILITY = null;
	
	@Override
	@SuppressWarnings( "ConstantValue" )
	public boolean hasCapability(
		@Nonnull Capability< ? > capability,
		@Nullable EnumFacing facing
	) { return capability == CAPABILITY; }
	
	@Nullable
	@Override
	@SuppressWarnings( "ConstantValue" )
	public < T > T getCapability(
		@Nonnull Capability< T > capability,
		@Nullable EnumFacing facing
	) { return capability == CAPABILITY ? CAPABILITY.cast( this ) : null; }
	
	@SuppressWarnings( "DataFlowIssue" )
	public static PlayerPatch getFrom( EntityPlayer player ) {
		return player.getCapability( CAPABILITY, null );
	}
}
