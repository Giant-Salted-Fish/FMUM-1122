package com.fmum.common.gun;

import com.fmum.common.load.IContentProvider;
import com.google.gson.annotations.SerializedName;

public interface IFireController
{
	void build( IContentProvider provider );
	
	class SafeMode implements IFireController
	{
		@Override
		public void build( IContentProvider provider ) { }
	}
	
	class FullAuto implements IFireController
	{
		@SerializedName( value = "roundsPerMin", alternate = "rpm" )
		protected float roundsPerMin = 600F;
		protected transient int timeBetweenRounds;
		
		@Override
		public void build( IContentProvider provider )
		{
			final int ticksPerSec = 20;
			final int timePerMin = ( ticksPerSec * 60 ) << 16; // 16-bits shift.
			this.timeBetweenRounds = ( int ) ( timePerMin / this.roundsPerMin );
		}
	}
	
//	class SemiAuto implements IFireController
//	{
//		
//	}
//	
//	class VarRateAuto implements IFireController
//	{
//		
//	}
}
