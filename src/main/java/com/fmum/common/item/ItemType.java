package com.fmum.common.item;

import com.fmum.common.pack.IContentBuildContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

public class ItemType extends Item implements IItemType
{
	protected String name;
	
//	@SerializedName( value = "creative_tab", alternate = "item_group" )
//	protected String creative_tab = FMUM.DEFAULT_CREATIVE_TAB.name();
	
	public ItemType buildServerSide( IContentBuildContext ctx )
	{
		IItemType.REGISTRY.regis( this );
		this.name = Optional.ofNullable( this.name ).orElseGet( ctx::fallbackName );
		return this;
	}
	
	@SideOnly( Side.CLIENT )
	public ItemType buildClientSide( IContentBuildContext ctx )
	{
		this.buildServerSide( ctx );
		
		return this;
	}
	
	@Override
	public String name() {
		return this.name;
	}
	
	@Override
	public Item vanillaItem()
	{
		return null;
	}
	
	@Override
	public IItem getItem( ItemStack stack )
	{
		return null;
	}
}
