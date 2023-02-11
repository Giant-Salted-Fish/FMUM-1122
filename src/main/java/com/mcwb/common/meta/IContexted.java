package com.mcwb.common.meta;

import javax.annotation.Nullable;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public interface IContexted
{
	@CapabilityInject( IContexted.class )
	public static final Capability< IContexted > CAPABILITY = null;
	
	public static class ContextedWrapper implements ICapabilityProvider
	{
		protected final IContexted contexted;
		
		public ContextedWrapper( IContexted contexted ) { this.contexted = contexted; }
		
		@Override
		public boolean hasCapability( Capability< ? > capability, @Nullable EnumFacing facing ) {
			return capability == CAPABILITY;
		}
		
		@Override
		public < T > T getCapability( Capability< T > capability, @Nullable EnumFacing facing ) {
			return CAPABILITY.cast( this.contexted );
		}
	}
}
