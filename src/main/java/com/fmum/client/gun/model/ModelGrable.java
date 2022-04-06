package com.fmum.client.gun.model;

import com.fmum.client.model.Animator;
import com.fmum.common.module.InfoModule;
import com.fmum.common.util.ArmTendency;
import com.fmum.common.util.CoordSystem;

public interface ModelGrable
{
	public void updateLeftHandTarPos(
		Animator ani,
		CoordSystem location,
		InfoModule info,
		ArmTendency dest
	);
	
	public void updateRightHandTarPos(
		Animator ani,
		CoordSystem location,
		InfoModule info,
		ArmTendency dest
	);
}
