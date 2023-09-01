package com.fmum.common.item;

import com.fmum.client.FMUMClient;
import com.fmum.common.load.IContentBuildContext;
import com.fmum.common.pack.IContentPack;
import com.fmum.common.pack.IContentPackFactory.IPostLoadContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

public abstract class ItemType extends Item implements IItemType
{
	protected String name;
	
	protected IContentPack from_pack;
	
	@SideOnly( Side.CLIENT )
	protected ResourceLocation texture;
	
//	@SerializedName( value = "creative_tab", alternate = "item_group" )
//	protected String creative_tab = FMUM.DEFAULT_CREATIVE_TAB.name();
	
	public void buildServerSide( IContentBuildContext ctx )
	{
		IItemType.REGISTRY.regis( this );
		
		this.name = Optional.ofNullable( this.name )
			.orElseGet( ctx::fallbackName );
		this.from_pack = ctx.contentPack();
		
		// Item creative tab may not be loaded yet, so we defer it to post load.
		ctx.regisPostLoadCallback( this::_setupCreativeTab );
	}
	
	@SideOnly( Side.CLIENT )
	public void buildClientSide( IContentBuildContext ctx )
	{
		this.buildServerSide( ctx );
		
		this.texture = Optional.ofNullable( this.texture )
			.orElseGet( () -> this._fallbackTexture( ctx ) );
	}
	
	@Override
	public String name() {
		return this.name;
	}
	
	@Override
	public Item vanillaItem() {
		return this;
	}
	
	@Override
	public IItem getItem( ItemStack stack )
	{
		return null;
	}
	
	public String toString()
	{
		return String.format(
			"%s::<%s.%s>", this._typeHint(),
			this.from_pack.toString(), this.name
		);
	}
	
	protected abstract String _typeHint();
	
	protected abstract void _setupCreativeTab( IPostLoadContext ctx );
	
	@SideOnly( Side.CLIENT )
	protected ResourceLocation _fallbackTexture( IContentBuildContext ctx ) {
		return FMUMClient.TEXTURE_GREEN;
	}
}
