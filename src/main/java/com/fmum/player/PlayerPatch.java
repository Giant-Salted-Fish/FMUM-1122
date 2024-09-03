package com.fmum.player;

import com.fmum.FMUM;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import com.fmum.item.IItemType;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Patches the vanilla player with some additional logic.
 *
 * @see PlayerPatchClient
 */
@EventBusSubscriber( modid = FMUM.MODID )
public class PlayerPatch
{
	private static final IEquippedItem VANILLA_EQUIPPED = new IEquippedItem() {
		@Override
		@SideOnly( Side.CLIENT )
		public boolean renderInHand( EnumHand hand, IItem item ) {
			return false;
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean renderSpecificInHand( EnumHand hand, IItem item ) {
			return false;
		}
	};
	
	private static final IItem VANILLA_ITEM = new IItem() {
		@Override
		public IItemType getType() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public ItemStack getBoundStack() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public IEquippedItem onTakeOut( EnumHand hand, EntityPlayer player ) {
			return VANILLA_EQUIPPED;
		}
	};
	
	
	IItem main_item = VANILLA_ITEM;
	IEquippedItem main_equipped = VANILLA_EQUIPPED;
	
	IItem off_item = VANILLA_ITEM;
	IEquippedItem off_equipped = VANILLA_EQUIPPED;
	
	
	PlayerPatch() { }
	
	void _tick( EntityPlayer player )
	{
		// >>> Update Main Hand <<<
		{
			final EnumHand hand = EnumHand.MAIN_HAND;
			final ItemStack stack = player.getHeldItemMainhand();
			final IItem held_item = IItem.ofOrEmpty( stack ).orElse( VANILLA_ITEM );
			this.main_equipped = (
				this.main_item.equals( held_item )
				? this.main_equipped.tickInHand( hand, held_item, player )
				: held_item.onTakeOut( hand, player )
			);
			
			// Not necessary, but this helps to release the old item if the instance has changed.
			this.main_item = held_item;
		}
		
		// >>> Update Off-hand <<<
		{
			final EnumHand hand = EnumHand.OFF_HAND;
			final ItemStack stack = player.getHeldItemOffhand();
			final IItem held_item = IItem.ofOrEmpty( stack ).orElse( VANILLA_ITEM );
			this.off_equipped = (
				this.off_item.equals( held_item )
				? this.off_equipped.tickInHand( hand, held_item, player )
				: held_item.onTakeOut( hand, player )
			);
			
			this.off_item = held_item;
		}
	}
	
	public final Optional< IItem > getItemIn( EnumHand hand )
	{
		final IItem item = hand == EnumHand.MAIN_HAND ? this.main_item : this.off_item;
		return item != VANILLA_ITEM ? Optional.of( item ) : Optional.empty();
	}
	
	
	/**
	 * Retrieve patched instance for given player. You can directly use
	 * {@link PlayerPatchClient#get()} if you are trying to retrieve instance for
	 * {@link net.minecraft.client.Minecraft#player}.
	 */
	@SuppressWarnings( "DataFlowIssue" )
	public static PlayerPatch of( EntityPlayer player ) {
		return player.getCapability( CAPABILITY, null );
	}
	
	
	// >>> For Capability Attach and Tick <<<
	@CapabilityInject( PlayerPatch.class )
	private static final Capability< PlayerPatch > CAPABILITY = null;
	
	private static final ResourceLocation KEY = new ResourceLocation( FMUM.MODID, "patch" );
	
	@SubscribeEvent
	@SideOnly( Side.SERVER )
	static void _onEntityCapAttachS( AttachCapabilitiesEvent< Entity > evt )
	{
		// We do not need to take care of EntityPlayerSp on dedicated server.
		final Entity entity = evt.getObject();
		if ( entity instanceof EntityPlayer ) {
			evt.addCapability( KEY, new PlayerPatch().new CapabilityProvider() );
		}
	}
	
	@SubscribeEvent
	@SideOnly( Side.CLIENT )
	static void _onEntityCapAttachC( AttachCapabilitiesEvent< Entity > evt )
	{
		final Entity entity = evt.getObject();
		if ( entity instanceof EntityPlayer )
		{
			final boolean is_sp = entity instanceof EntityPlayerSP;
			final PlayerPatch patch = is_sp ? new PlayerPatchClient() : new PlayerPatch();
			evt.addCapability( KEY, patch.new CapabilityProvider() );
		}
	}
	
	@SubscribeEvent
	static void _onPlayerTick( PlayerTickEvent evt )
	{
		if ( evt.phase == Phase.END )
		{
			final EntityPlayer player = evt.player;
			of( player )._tick( player );
		}
	}
	
	private final class CapabilityProvider implements ICapabilityProvider
	{
		@Override
		@SuppressWarnings( "ConstantValue" )
		public boolean hasCapability( @Nonnull Capability< ? > capability, @Nullable EnumFacing facing ) {
			return capability == CAPABILITY;
		}
		
		@Override @Nullable
		@SuppressWarnings( "ConstantValue" )
		public < T > T getCapability( @Nonnull Capability< T > capability, @Nullable EnumFacing facing ) {
			return capability == CAPABILITY ? CAPABILITY.cast( PlayerPatch.this ) : null;
		}
	}
}
