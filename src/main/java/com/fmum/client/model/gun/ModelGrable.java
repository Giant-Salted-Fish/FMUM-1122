package com.fmum.client.model.gun;

import com.fmum.client.model.Animator;
import com.fmum.common.module.ModuleInfo;
import com.fmum.common.util.ArmTendency;
import com.fmum.common.util.CoordSystem;

public interface ModelGrable
{
	public void updateLeftHandTarPos(
		Animator ani,
		CoordSystem location,
		ModuleInfo info,
		ArmTendency dest
	);
	
	public void updateRightHandTarPos(
		Animator ani,
		CoordSystem location,
		ModuleInfo info,
		ArmTendency dest
	);
}
