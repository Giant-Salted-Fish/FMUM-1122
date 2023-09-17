package com.fmum.common.module;

import java.util.function.Consumer;

public interface IModuleModifySession< T >
{
	void setOffsetAndStep( int offset, int step );
	
	void setPaintjob( int paintjob );
	
	void install( int slot_idx, IModule< ? > module, Consumer< Integer > _out_install_idx );
	
	void remove( int slot_idx, int install_idx, Consumer< T > _out_removed_module );
	
	boolean isValidState();
	
	boolean canPreview();
	
	String cause();
	
	void commit();
}
