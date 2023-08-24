package com.fmum.common.item;

import com.fmum.common.Registry;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

public interface ItemType
{
	Registry< ItemType > REGISTRY = new Registry<>( ItemType::name );
	
	ItemType VANILLA = new ItemType()
	{
		@Override
		public String name() { return "vanilla"; }
		
		@Override
		public net.minecraft.item.Item vanillaItem() { return Items.AIR; }
		
		@Override
		public Item getItem( ItemStack stack ) { return Item.VANILLA; }
	};
	
	default void onItemRegister( RegistryEvent.Register< net.minecraft.item.Item > evt ) {
		evt.getRegistry().register( this.vanillaItem() );
	}
	
	// FIXME: model register need to be override if item has sub-types
	@SideOnly( Side.CLIENT )
	default void onModelRegister( ModelRegistryEvent evt )
	{
		final net.minecraft.item.Item item = this.vanillaItem();
		final ResourceLocation res_loc = item.getRegistryName();
		final ModelResourceLocation model_res =
			new ModelResourceLocation( res_loc, "inventory" );
		ModelLoader.setCustomModelResourceLocation( item, 0, model_res );
	}
	
	String name();
	
	net.minecraft.item.Item vanillaItem();
	
	Item getItem( ItemStack stack );
	
	/**
	 * Given item must be an instance of {@link FMUMVanillaItem}.
	 *
	 * @see #getFromOrDefault(net.minecraft.item.Item)
	 */
	static ItemType getFrom( net.minecraft.item.Item vanilla_item ) {
		return ( ( FMUMVanillaItem ) vanilla_item ).type();
	}
	
	/**
	 * @see #getFrom(net.minecraft.item.Item)
	 * @return {@link #VANILLA} if given item is not an instance of {@link FMUMVanillaItem}.
	 */
	static ItemType getFromOrDefault( net.minecraft.item.Item vanilla_item )
	{
		final boolean is_fmum_item = vanilla_item instanceof FMUMVanillaItem;
		return is_fmum_item ? getFrom( vanilla_item ) : VANILLA;
	}
	
	static Optional< net.minecraft.item.Item > findItem( String identifier )
	{
		final Optional< ItemType > type = REGISTRY.lookup( identifier );
		final net.minecraft.item.Item item = type.map( ItemType::vanillaItem )
												 .orElseGet( () -> net.minecraft.item.Item.getByNameOrId( identifier ) );
		return Optional.ofNullable( item );
	}
}
