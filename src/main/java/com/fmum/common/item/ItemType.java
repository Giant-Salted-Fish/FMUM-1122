package com.fmum.common.item;

import com.fmum.client.FMUMClient;
import com.fmum.common.FMUM;
import com.fmum.common.load.IContentBuildContext;
import com.fmum.common.load.TexturedType;
import com.fmum.common.pack.IContentPack;
import com.fmum.common.pack.IContentPackFactory.IPostLoadContext;
import com.fmum.common.tab.ICreativeTab;
import com.google.gson.annotations.SerializedName;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class ItemType extends TexturedType implements IItemType
{
	@SerializedName( value = "creative_tab", alternate = "item_group" )
	protected String creative_tab = FMUM.MODID;
	
	protected transient Item item;
	
	@Override
	public void buildServerSide( IContentBuildContext ctx )
	{
		super.buildServerSide( ctx );
		
		IItemType.REGISTRY.regis( this );
		this.item = this._createItem( ctx );
		
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
		return this.item;
	}
	
	@Override
	public IItem getItem( ItemStack stack )
	{
		final EnumFacing facing = null;
		return stack.getCapability( IItem.CAPABILITY, facing );
	}
	
	public String toString()
	{
		return String.format(
			"%s::<%s.%s>", this._typeHint(),
			this.from_pack.toString(), this.name
		);
	}
	
	protected abstract Item _createItem( IContentBuildContext ctx );
	
	protected abstract String _typeHint();
	
	protected void _setupCreativeTab( IPostLoadContext ctx )
	{
		ICreativeTab.REGISTRY.lookup( this.creative_tab ).orElseGet( () -> {
			FMUM.MOD.logError(
				"fmum.fail_to_find_tab",
				this.toString(), this.creative_tab
			);
			return ctx.defaultCreativeTab();
		} ).appendItem( this );
		
		// No longer needed, release it.
		this.creative_tab = null;
	}
	
	@SideOnly( Side.CLIENT )
	protected ResourceLocation _fallbackTexture( IContentBuildContext ctx ) {
		return FMUMClient.TEXTURE_GREEN;
	}
}
