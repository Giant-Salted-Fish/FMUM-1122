package com.fmum.common.item;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.fmum.client.input.MetaKeyBind;
import com.fmum.common.meta.MetaBase;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Corresponding to {@link Item} in Minecraft. Works more like a proxy that receive and handle the
 * actual events that passed to the bounden {@link Item}.
 * 
 * @author Giant_Salted_Fish
 */
public interface MetaItem extends MetaBase
{
	public static final HashMap< String, MetaItem > regis = new HashMap<>();
	
	public static final String
		TRANSLATION_PREFIX = "item.",
		TRANSLATION_SUFFIX = ".name";
	
	/**
	 * A default item that "do nothing"
	 */
	public static final MetaItem NONE = new MetaItem()
	{
		@Override
		public String name() { return "none"; }
		
		@Override
		public Item item() { return null; }
	};
	
	@Override
	public default void regisPostInitHandler( Set< Runnable > tasks )
	{
		MetaBase.super.regisPostInitHandler( tasks );
		
		tasks.add( () -> this.regisTo( this, regis ) );
	}
	
	@Override
	public default void regisPostLoadHandler( Set< Runnable > tasks ) {
		MetaBase.super.regisPostLoadHandler( tasks );
	}
	
	/**
	 * @return Corresponding {@link Item} to this type
	 */
	public Item item();
	
	public default void onItemRegister( RegistryEvent.Register< Item > evt ) {
		evt.getRegistry().register( this.item() );
	}
	
	public default void onModelRegister( ModelRegistryEvent evt )
	{
		ModelLoader.setCustomModelResourceLocation(
			this.item(),
			0,
			new ModelResourceLocation(
				this.item().getRegistryName(), // modid + name
				"inventory"
			)
		);
	}
	
	public default boolean nbtBroken( ItemStack stack ) { return false; }
	
	public default String description( ItemStack stack ) { return this.description(); }
	
	/**
	 * In default avoid to break the block when holding this item in survive mode
	 * 
	 * @see Item#onBlockStartBreak(ItemStack, BlockPos, EntityPlayer)
	 */
	public default boolean onBlockStartBreak( ItemStack itemstack, BlockPos pos, EntityPlayer player ) {
		return true;
	}
	
	/**
	 * In default avoid to break the block when holding this item in creative mode
	 * 
	 * @see Item#canDestroyBlockInCreative(World, BlockPos, ItemStack, EntityPlayer)
	 */
	public default boolean canDestroyBlockInCreative(
		World world,
		BlockPos pos,
		ItemStack stack,
		EntityPlayer player
	) { return false; }
	
	@SideOnly( Side.CLIENT )
	public default void addInformation(
		ItemStack stack,
		World worldIn,
		List< String > tooltip,
		ITooltipFlag flagIn
	) { }
	
	/**
	 * Determine whether view bobbing setting should be disabled while holding this item. It is not
	 * recommended to in {@link #onTakeOut(ItemStack)} and {@link #onPutAway(ItemStack)} since
	 * player may open settings menu to toggle view bobbing setting during the game play.
	 * 
	 * @return {@code false} if view bobbing should be disabled while holding this item
	 */
	@SideOnly( Side.CLIENT )
	public default boolean allowViewBobbing() { return true; }
	
	@SideOnly( Side.CLIENT )
	public default boolean disableCrosshair() { return false; }
	
	/**
	 * @param stack Item stack
	 * @param dWheel Mouse wheel change
	 * @return {@code true} if this input should be suppressed(prevent changing slot)
	 */
	@SideOnly( Side.CLIENT )
	public default boolean onMouseWheelInput( ItemStack stack, int dWheel ) { return false; }
	
	@SideOnly( Side.CLIENT )
	public default void onTakeOut( ItemStack stack ) { }
	
	@SideOnly( Side.CLIENT )
	public default void onPutAway( ItemStack stack ) { }
	
	@SideOnly( Side.CLIENT )
	public default void onInvTick( ItemStack stack, IInventory inv, int slot ) { }
	
	@SideOnly( Side.CLIENT )
	public default void onHandTick( ItemStack stack ) { }
	
	@SideOnly( Side.CLIENT )
	public default void onKeyInput( ItemStack stack, MetaKeyBind key ) { }
	
	@SideOnly( Side.CLIENT )
	public default String translationKey( ItemStack stack ) { return this.translationKey(); }
	
	@Override
	@SideOnly( Side.CLIENT )
	public default String translationKey() {
		return TRANSLATION_PREFIX + this.name() + TRANSLATION_SUFFIX;
	}
	
	@SideOnly( Side.CLIENT )
	public default void onRenderTick( ItemStack stack, MouseHelper mouse ) { }
	
	@SideOnly( Side.CLIENT )
	public default void renderFP( ItemStack stack ) { }
	
	public static MetaItem get( String name ) { return regis.get( name ); }
}
