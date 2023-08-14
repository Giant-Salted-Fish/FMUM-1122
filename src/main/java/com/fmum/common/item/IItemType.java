package com.fmum.common.item;

import com.fmum.common.Registry;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

public interface IItemType
{
	Registry< IItemType > REGISTRY = new Registry<>( IItemType::name );
	
	IItemType VANILLA = new IItemType()
	{
		@Override
		public String name() { return "vanilla"; }
		
		@Override
		public Item vanillaItem() { return Items.AIR; }
		
		@Override
		public IItem getItem( ItemStack stack ) { return IItem.VANILLA; }
	};
	
	default void onItemRegister( RegistryEvent.Register< Item > evt ) {
		evt.getRegistry().register( this.vanillaItem() );
	}
	
	// FIXME: model register need to be override if item has sub-types
	@SideOnly( Side.CLIENT )
	default void onModelRegister( ModelRegistryEvent evt )
	{
		final Item item = this.vanillaItem();
		final ResourceLocation res_loc = item.getRegistryName();
		final ModelResourceLocation model_res = new ModelResourceLocation( res_loc, "inventory" );
		ModelLoader.setCustomModelResourceLocation( item, 0, model_res );
	}
	
	String name();
	
	Item vanillaItem();
	
	IItem getItem( ItemStack stack );
	
	/**
	 * Given item must be an instance of {@link IFMUMVanillaItem}.
	 *
	 * @see #getFromOrDefault(Item)
	 */
	static IItemType getFrom( Item vanilla_item ) {
		return ( ( IFMUMVanillaItem ) vanilla_item ).type();
	}
	
	/**
	 * @see #getFrom(Item)
	 * @return {@link #VANILLA} if given item is not an instance of {@link IFMUMVanillaItem}.
	 */
	static IItemType getFromOrDefault( Item vanilla_item )
	{
		final boolean is_fmum_item = vanilla_item instanceof IFMUMVanillaItem;
		return is_fmum_item ? getFrom( vanilla_item ) : VANILLA;
	}
	
	static Optional< Item > findItem( String identifier )
	{
		final Optional< IItemType > type = REGISTRY.find( identifier );
		return Optional.ofNullable(
			type.isPresent() ? type.get().vanillaItem() : Item.getByNameOrId( identifier ) );
	}
}
