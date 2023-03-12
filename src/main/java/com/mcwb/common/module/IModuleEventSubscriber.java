package com.mcwb.common.module;

import com.google.common.base.Supplier;

@FunctionalInterface
public interface IModuleEventSubscriber< T >
{
	public void onReceiveEvent( T evt );
	
	public default int priority() { return 0; }
	
	public static class ModuleInstallEvent
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
	
	public static class ModuleRemoveEvent
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
