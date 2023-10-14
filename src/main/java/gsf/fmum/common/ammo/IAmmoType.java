package gsf.fmum.common.ammo;

import gsf.fmum.common.IDRegistry;
import gsf.fmum.common.item.IItemType;
import gsf.fmum.util.Category;

public interface IAmmoType extends IItemType
{
	IDRegistry< IAmmoType > REGISTRY = new IDRegistry<>( IAmmoType::name );
	
	Category category();
	
	/**
	 * This is called right before firing this round. Can be used to create misfire rounds randomly.
	 */
//	IAmmoType onShoot();  // TODO: Maybe pass in some "IShooter" in as reference.
	
	boolean canShoot();
}
