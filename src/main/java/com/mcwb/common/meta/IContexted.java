package com.mcwb.common.meta;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public interface IContexted
{
	@CapabilityInject( IContexted.class )
	public static final Capability< IContexted > CAPABILITY = null;
	
	// TODO: check if this is needed
//	public static class ContextedWrapper implements ICapabilityProvider
//	{
//		protected final IContexted contexted;
//		
//		public ContextedWrapper( IContexted contexted ) { this.contexted = contexted; }
//		
//		@Override
//		public boolean hasCapability( Capability< ? > capability, @Nullable EnumFacing facing ) {
//			return capability == CAPABILITY;
//		}
//		
//		@Override
//		public < T > T getCapability( Capability< T > capability, @Nullable EnumFacing facing ) {
//			return CAPABILITY.cast( this.contexted );
//		}
//	}
}
