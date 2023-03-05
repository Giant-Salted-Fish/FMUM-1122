package com.mcwb.common.module;

import com.google.common.base.Supplier;

@FunctionalInterface
public interface IModuleEventSubscriber< T >
{
	public void onReceiveEvent( T evt );
	
	public default int priority() { return 0; }
	
	public static class ModuleInstallEvent
	{
		public IModular< ? > base;
		
		public IModular< ? > module;
		
		public int slot;
		
		public Supplier< IPreviewPredicate > action;
		
		public ModuleInstallEvent(
			IModular< ? > base,
			int slot, IModular< ? > module,
			Supplier< IPreviewPredicate > action
		) {
			this.base = base;
			this.slot = slot;
			this.module = module;
			this.action = action;
		}
	}
	
	public static class ModuleRemoveEvent
	{
		public IModular< ? > base;
		
		public int slot;
		public int idx;
		
		// TODO: onBeingRemoved
		public Supplier< IModular< ? > > action;
		
		public ModuleRemoveEvent(
			IModular< ? > base,
			int slot, int idx,
			Supplier< IModular< ? > > action
		) {
			this.base = base;
			this.slot = slot;
			this.idx = idx;
			this.action = action;
		}
	}
}
