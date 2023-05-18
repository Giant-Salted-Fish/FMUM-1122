//package com.fmum.common.gun;
//
//import com.fmum.common.load.BuildableLoader;
//import com.fmum.common.load.IBuildable;
//import com.fmum.common.load.IContentProvider;
//import com.google.gson.annotations.SerializedName;
//
//public interface IFireController
//{
//	boolean canAction( int actionCount );
//	
//	class SafeMode implements IFireController, IBuildable< IFireController >
//	{
//		public static final BuildableLoader< IFireController >
//			LOADER = new BuildableLoader<>( "safe_mode", SafeMode.class );
//		
//		@Override
//		public IFireController build( String name, IContentProvider provider ) { return this; }
//	}
//	
//	class FullAuto implements IFireController, IBuildable< IFireController >
//	{
//		public static final BuildableLoader< IFireController >
//			LOADER = new BuildableLoader<>( "full_auto", FullAuto.class );
//		
//		@SerializedName( value = "roundsPerMin", alternate = "rpm" )
//		protected float roundsPerMin = 600F;
//		protected transient int timeBetweenRounds;
//		
//		@Override
//		public IFireController build( String name, IContentProvider provider )
//		{
//			final int ticksPerSec = 20;
//			final int timePerMin = ( ticksPerSec * 60 ) << 16; // 16-bits shift.
//			this.timeBetweenRounds = ( int ) ( timePerMin / this.roundsPerMin );
//			return this;
//		}
//	}
//	
////	class SemiAuto implements IFireController
////	{
////		
////	}
////	
////	class VarRateAuto implements IFireController
////	{
////		
////	}
//}
