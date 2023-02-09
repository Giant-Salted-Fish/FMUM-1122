package com.mcwb.common.item;

import com.mcwb.common.meta.IMeta;
import com.mcwb.common.meta.Registry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IItemType extends IMeta
{
	public static final Registry< IItemType > REGISTRY = new Registry<>();
	
	public static final String
		TRANSLATION_PREFIX = "item.",
		TRANSLATION_SUFFIX = ".name";
	
	/**
	 * A default item that simply "do nothing". Delegate for vanilla items.
	 */
	public static final IItemType VANILLA = new IItemType()
	{
		@Override
		public String name() { return "vanilla"; }
		
		@Override
		public Item item() { return null; }
		
		@Override
		public IItem getContexted( ICapabilityProvider provider ) {
			return provider == ItemStack.EMPTY ? IItem.EMPTY : IItem.VANILLA;
		}
	};
	
	/**
	 * @return Corresponding vanilla item in {@link Minecraft}
	 */
	public Item item();
	
	public IItem getContexted( ICapabilityProvider provider );
	
	public default void onItemRegister( RegistryEvent.Register< Item > evt ) {
		evt.getRegistry().register( this.item() );
	}
	
	// FIXME: model register need to be override if item has sub types
	@SideOnly( Side.CLIENT )
	public default void onModelRegister( ModelRegistryEvent evt )
	{
		final Item item = this.item();
		ModelLoader.setCustomModelResourceLocation(
			item, 0, new ModelResourceLocation( item.getRegistryName(), "inventory" )
		);
	}
}
