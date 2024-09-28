package com.fmum.gun;

import com.fmum.FMUM;
import com.fmum.gunpart.GunPartType;
import com.fmum.item.IEquippedItem;
import com.fmum.item.IItem;
import com.fmum.item.IItemType;
import com.fmum.load.IContentBuildContext;
import com.fmum.load.IContentLoader;
import com.fmum.load.JsonData;
import com.fmum.mag.IMag;
import com.fmum.module.IModifyPreview;
import com.fmum.module.IModule;
import com.fmum.module.IModuleType;
import com.fmum.paintjob.IPaintableType;
import gsf.util.lang.Error;
import gsf.util.lang.Result;
import gsf.util.lang.Success;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

public class GunType extends GunPartType
{
	public static final IContentLoader< GunType > LOADER = IContentLoader.of(
		GunType::new,
		IItemType.REGISTRY, IModuleType.REGISTRY, IPaintableType.REGISTRY
	);
	
	
	protected GunOpConfig op_load_mag;
	
	@SideOnly( Side.CLIENT )
	protected GunOpConfig op_inspect;
	
	
	@Override
	public void reload( JsonData data, IContentBuildContext ctx )
	{
		super.reload( data, ctx );
		
		this.op_load_mag = data.get( "op_load_mag", GunOpConfig.class ).orElse( GunOpConfig.DEFAULT );
		FMUM.SIDE.runIfClient( () -> {
			this.op_inspect = data.get( "op_inspect", GunOpConfig.class ).orElse( GunOpConfig.DEFAULT );
		} );
	}
	
	@Override
	protected GunPart _createRawModule()
	{
		final Optional< Short > opt = IModuleType.REGISTRY.lookupID( this );
		return new Gun( opt.orElseThrow( IllegalStateException::new ) );
	}
	
	@Override
	public IModule takeAndDeserialize( NBTTagCompound nbt ) {
		return new Gun( nbt );
	}
	
	@Override
	protected IEquippedItem _newEquipped( EnumHand hand, IItem item, EntityPlayer player ) {
		return new EquippedGun();
	}
	
	
	protected class Gun extends GunPart implements IGun
	{
		protected Gun( short id ) {
			super( id );
		}
		
		protected Gun( NBTTagCompound nbt ) {
			super( nbt );
		}
		
		@Override
		public Optional< ? extends IMag > getMag()
		{
			final boolean has_mag = this.countModuleInSlot( 0 ) > 0;
			return has_mag ? Optional.of( ( IMag ) this.getInstalled( 0, 0 ) ) : Optional.empty();
		}
		
		@Override
		public Result< Runnable, String > checkMagForLoad( IMag mag )
		{
			final IModifyPreview< Integer > preview = this.tryInstall( 0, mag );
			final Optional< String > error = preview.getPreviewError();
			return (
				error.isPresent()
				? new Error<>( error.get() )
				: new Success<>( preview::apply )
			);
		}
	}
}
