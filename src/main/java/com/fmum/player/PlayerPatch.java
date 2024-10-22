package com.fmum.player;

import com.fmum.FMUM;
import com.fmum.item.IItem;
import com.fmum.item.IItemType;
import com.fmum.item.IMainEquipped;
import com.fmum.item.IOffEquipped;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
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
import java.util.function.BiFunction;

/**
 * Patches the vanilla player with some additional logic.
 *
 * @see PlayerPatchClient
 */
@EventBusSubscriber( modid = FMUM.MODID )
public class PlayerPatch
{
	private static final IMainEquipped VANILLA_MAIN_EQUIPPED = new IMainEquipped() {
		@Override
		@SideOnly( Side.CLIENT )
		public boolean renderInHand( IItem item ) {
			return false;
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean renderSpecificInHand( IItem item ) {
			return false;
		}
	};
	
	private static final IOffEquipped VANILLA_OFF_EQUIPPED = new IOffEquipped() {
		@Override
		@SideOnly( Side.CLIENT )
		public boolean renderInHand( IItem item ) {
			return true;  // TODO: Config setting?
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean renderSpecificInHand( IItem item ) {
			return true;
		}
	};
	
	private static final IItem VANILLA_ITEM = new IItem() {
		@Override
		public IItemType getType() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public IMainEquipped onTakeOutMainHand( EntityPlayer player ) {
			return VANILLA_MAIN_EQUIPPED;
		}
		
		@Override
		public IOffEquipped onTakeOutOffHand( EntityPlayer player ) {
			return VANILLA_OFF_EQUIPPED;
		}
	};
	
	
	int inv_slot = -1;
	IItem main_item = VANILLA_ITEM;
	IMainEquipped main_equipped = VANILLA_MAIN_EQUIPPED;
	
	IItem off_item = VANILLA_ITEM;
	IOffEquipped off_equipped = VANILLA_OFF_EQUIPPED;
	
	
	PlayerPatch() { }
	
	void _tick( EntityPlayer player )
	{
		// >>> Update Main Hand <<<
		{
			final EnumHand hand = EnumHand.MAIN_HAND;
			final InventoryPlayer inv = player.inventory;
			final int inv_slot = inv.currentItem;
			final ItemStack stack = inv.getStackInSlot( inv_slot );
			final Optional< IItem > opt_item = IItem.ofOrEmpty( stack );
			if ( this.getItemIn( hand ).equals( opt_item ) && inv_slot == this.inv_slot )
			{
				final IItem item = opt_item.orElse( VANILLA_ITEM );
				// Necessary, because we are using #equals here.
				this.main_item = item;
				this.main_equipped = this.main_equipped.tickInHand( item, player );
			}
			else
			{
				final Optional< IMainEquipped > opt_eq;
				opt_eq = this.main_equipped.tickPutAway( this.main_item, player );
				if ( opt_eq.isPresent() ) {
					this.main_equipped = opt_eq.get();
				}
				else
				{
					this.inv_slot = inv_slot;
					final IItem item = opt_item.orElse( VANILLA_ITEM );
					this.main_item = item;
					final IMainEquipped equipped = item.onTakeOutMainHand( player );
					this.main_equipped = equipped.tickInHand( item, player );
				}
			}
		}
		
		// >>> Update Off-hand <<<
		{
			final EnumHand hand = EnumHand.OFF_HAND;
			final ItemStack stack = player.getHeldItemOffhand();
			final Optional< IItem > opt_item = IItem.ofOrEmpty( stack );
			if ( this.getItemIn( hand ).equals( opt_item ) )
			{
				final IItem item = opt_item.orElse( VANILLA_ITEM );
				this.off_item = item;
				this.off_equipped = this.off_equipped.tickInHand( item, player );
			}
			else
			{
				final Optional< IOffEquipped > opt_eq;
				opt_eq = this.off_equipped.tickPutAway( this.main_item, player );
				if ( opt_eq.isPresent() ) {
					this.off_equipped = opt_eq.get();
				}
				else
				{
					final IItem item = opt_item.orElse( VANILLA_ITEM );
					this.off_item = item;
					final IOffEquipped equipped = item.onTakeOutOffHand( player );
					this.off_equipped = equipped.tickInHand( item, player );
				}
			}
		}
	}
	
	public final Optional< IItem > getItemIn( EnumHand hand )
	{
		final IItem item = hand == EnumHand.MAIN_HAND ? this.main_item : this.off_item;
		return item != VANILLA_ITEM ? Optional.of( item ) : Optional.empty();
	}
	
	public final < T extends IMainEquipped > Optional< T > mapMainEquipped(
		BiFunction< ? super IMainEquipped, ? super IItem, T > mapper
	) {
		if ( this.main_equipped == VANILLA_MAIN_EQUIPPED ) {
			return Optional.empty();
		}
		
		final T mapped = mapper.apply( this.main_equipped, this.main_item );
		this.main_equipped = mapped;
		return Optional.of( mapped );
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
