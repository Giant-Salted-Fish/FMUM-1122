package com.mcwb.common.tab;

import com.mcwb.common.MCWB;
import com.mcwb.common.item.IItemType;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.load.TexturedMeta;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.meta.IMetaHost;
import com.mcwb.common.pack.IContentProvider;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CreativeTab extends TexturedMeta implements ICreativeTab
{
	public static final BuildableLoader< IMeta >
		LOADER = new BuildableLoader<>( "creative_tab", CreativeTab.class );
	
	protected transient CreativeTabs tab;
	
	@SideOnly( Side.CLIENT )
	protected String iconItem; {
		if( MCWB.MOD.isClient() ) this.iconItem = "" + Items.FISH.getRegistryName();
	}
	
	@SideOnly( Side.CLIENT )
	protected short iconItemDam;
	
	@SideOnly( Side.CLIENT )
	protected boolean noScrollBar;
	
	@SideOnly( Side.CLIENT )
	protected boolean noTitle;
	
	@Override
	public CreativeTab build( String name, IContentProvider provider )
	{
		super.build( name, provider );
		
		ICreativeTab.REGISTRY.regis( this );
		
		this.tab = this.createTab();
		if( MCWB.MOD.isClient() )
		{
			if( this.noScrollBar )
				this.tab.setNoScrollbar();
			if( this.noTitle )
				this.tab.setNoTitle();
		}
		return this;
	}
	
	@Override
	public CreativeTabs creativeTab() { return this.tab; }
	
	protected CreativeTabs createTab() { return this.new VanillaCreativeTab( this.name ); }
	
	@Override
	@SideOnly( Side.CLIENT )
	protected void checkTextureSetup()
	{
		// Use a default background image if does not have
		if( this.texture == null )
			this.texture = CreativeTabs.BUILDING_BLOCKS.getBackgroundImage();
	}
	
	@Override
	protected IMeta loader() { return LOADER; }
	
	protected class VanillaCreativeTab extends CreativeTabs implements IMetaHost
	{
		public VanillaCreativeTab( String label ) { super( label ); }
		
		@Override
		public ICreativeTab meta() { return CreativeTab.this; }
		
		@Override
		@SideOnly( Side.CLIENT )
		public ItemStack createIcon()
		{
			final String icon = CreativeTab.this.iconItem;
			
			// Check if required item is defined in MCWB
			final IItemType meta = IItemType.REGISTRY.get( icon );
			final Item item = meta != null ? meta.item() : Item.getByNameOrId( icon );
			if( item != null )
				return new ItemStack( item, 1, CreativeTab.this.iconItemDam );
			
			CreativeTab.this.error(
				"mcwb.can_not_find_tab_icon_item",
				this.getTabLabel(),
				CreativeTab.this.iconItemDam
			);
			return new ItemStack( Items.FISH );
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public ResourceLocation getBackgroundImage() { return CreativeTab.this.texture; }
	}
}
