package com.mcwb.common.gun;

import javax.annotation.Nullable;

import com.mcwb.client.gun.IGunPartRenderer;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.modify.IContextedModifiable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class GunMeta< C extends IContextedGun, M extends IGunPartRenderer< ? super C > >
	extends GunPartMeta< C, M >
{
	public static final BuildableLoader< IMeta >
		LOADER = new BuildableLoader<>( "gun", GunJson.class );
	
	protected IMeta loader() { return LOADER; }
	
	protected abstract class ContextedGun extends ContextedGunPart implements IContextedGun
	{
		/**
		 * @see ContextedGunPart#ContextedGunPart()
		 */
		protected ContextedGun() { }
		
		/**
		 * @see ContextedGunPart#ContextedGunPart(NBTTagCompound)
		 */
		protected ContextedGun( NBTTagCompound nbt ) { super( nbt ); }
		
		@Override
		public boolean hasCapability( Capability< ? > capability, @Nullable EnumFacing facing )
		{
			return capability == IContextedGun.CAPABILITY
				|| capability == IContextedGunPart.CAPABILITY
				|| capability == IContextedModifiable.CAPABILITY;
		}
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean updateViewBobbing( boolean original ) { return false; }
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean hideCrosshair() { return true; }
	}
	
	public static class GunJson extends GunMeta<
		IContextedGun,
		IGunPartRenderer< ? super IContextedGun >
	> {
		@Override
		protected Capability< ? extends IContextedGun > capability() {
			return IContextedGun.CAPABILITY;
		}
		
		@Override
		protected ContextedGun newCtxedCap( NBTTagCompound nbt )
		{
			return this.new ContextedGun( nbt )
			{
				@Override
				protected IContextedGun self() { return this; }
			};
		}
		
		@Override
		protected ContextedGun newRawCtxedCap()
		{
			return this.new ContextedGun()
			{
				@Override
				protected IContextedGun self() { return this; }
			};
		}
	}
}
