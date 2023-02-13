package com.mcwb.common.gun;

import com.mcwb.client.gun.IGunPartRenderer;
import com.mcwb.client.input.InputHandler;
import com.mcwb.client.player.PlayerPatchClient;
import com.mcwb.common.load.BuildableLoader;
import com.mcwb.common.meta.IMeta;
import com.mcwb.common.modify.IModifiable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class GunType< C extends IGunPart, M extends IGunPartRenderer< ? super C > >
	extends GunPartType< C, M >
{
	public static final BuildableLoader< IMeta >
		LOADER = new BuildableLoader<>( "gun", GunJson.class );
	
	@Override
	protected IMeta loader() { return LOADER; }
	
	protected class Gun extends GunPart implements IGun
	{
		/**
		 * @see GunPart#ContextedGunPart()
		 */
		protected Gun() { }
		
		/**
		 * @see GunPart#GunPart(NBTTagCompound)
		 */
		protected Gun( NBTTagCompound nbt ) { super( nbt ); }
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean updateViewBobbing( boolean original ) { return false; }
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean hideCrosshair()
		{
			final boolean modifying = PlayerPatchClient.instance.operating() == OP_MODIFY;
			final boolean freeView = InputHandler.FREE_VIEW.down || InputHandler.CO_FREE_VIEW.down;
			return !( modifying && freeView );
		}
	}
	
	public static class GunJson extends GunType< IGun, IGunPartRenderer< ? super IGun > >
	{
		@Override
		public IModifiable newContexted( NBTTagCompound nbt ) { return this.new Gun( nbt ); }
		
		@Override
		public IModifiable deserializeContexted( NBTTagCompound nbt )
		{
			final Gun gun = this.new Gun();
			gun.deserializeNBT( nbt );
			return gun;
		}
	}
}
