package com.fmum.common.module;

import java.util.function.Supplier;

@FunctionalInterface
public interface IModuleEventSubscriber< T >
{
	void onReceiveEvent( T evt );
	
	default int priority() { return 0; }
	
	class ModuleInstallEvent
	{
		public IModule< ? > base;
		
		public IModule< ? > module;
		
		public int slot;
		
		public Supplier< IPreviewPredicate > action;
		
		public ModuleInstallEvent(
			IModule< ? > base,
			int slot, IModule< ? > module,
			Supplier< IPreviewPredicate > action
		) {
			this.base = base;
			this.slot = slot;
			this.module = module;
			this.action = action;
		}
	}
	
	class ModuleRemoveEvent
	{
		public IModule< ? > base;
		
		public int slot;
		public int idx;
		
		// TODO: onBeingRemoved
		public Supplier< IModule< ? > > action;
		
		public ModuleRemoveEvent(
			IModule< ? > base,
			int slot, int idx,
			Supplier< IModule< ? > > action
		) {
			this.base = base;
			this.slot = slot;
			this.idx = idx;
			this.action = action;
		}
	}
}
