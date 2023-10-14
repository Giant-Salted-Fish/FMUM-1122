package gsf.fmum.common.tab;

import gsf.fmum.common.Registry;
import gsf.fmum.common.item.IItemType;
import net.minecraft.creativetab.CreativeTabs;

public interface ICreativeTab
{
	Registry< ICreativeTab > REGISTRY = new Registry<>( ICreativeTab::name );
	
	String name();
	
	CreativeTabs vanillaCreativeTab();
	
	default void appendItem( IItemType item ) {
		item.vanillaItem().setCreativeTab( this.vanillaCreativeTab() );
	}
}
