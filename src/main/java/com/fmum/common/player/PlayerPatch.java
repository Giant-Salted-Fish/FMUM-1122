package com.fmum.common.player;

import com.fmum.common.item.EquippedItem;
import com.fmum.common.item.Item;
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
	protected Item main_item = Item.VANILLA;
	protected EquippedItem< ? > main_equipped = EquippedItem.VANILLA;
	
	protected Item off_item = Item.VANILLA;
	protected EquippedItem< ? > off_equipped = EquippedItem.VANILLA;
	
	protected Operation operation = Operation.NONE;
	
	public void tick( EntityPlayer player )
	{
		final InventoryPlayer inv = player.inventory;
		
		// Main hand update.
		{
			final EnumHand hand = EnumHand.MAIN_HAND;
			final ItemStack stack = inv.getCurrentItem();
			final Item item = Item.getFromOrDefault( stack );
			
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
			final Item item = Item.getFromOrDefault( stack );
			
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
	
	public final Operation operation() { return this.operation; }
	
	
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
