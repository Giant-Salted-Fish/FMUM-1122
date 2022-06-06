package com.fmum.common.util;

/**
 * Builder for BlockBench animation
 *  
 * @author Giant_Salted_Fish
 */
public abstract class BBAnimationBuilder extends AnimationCatmullRom.Builder
{
	public BBAnimationBuilder( double length ) { super( length ); }
	
	public static class BBPosAnimationBuilder extends BBAnimationBuilder
	{
		public BBPosAnimationBuilder( double length ) { super( length ); }
		
		@Override
		public AnimationCatmullRom quickBuild()
		{
			// TODO: validate if it is necessary
			this.flip( true, false, false );
			this.scale( 1D / 16D );
			return super.quickBuild();
		}
	}
	
	public static class BBRotAnimationBuilder extends BBAnimationBuilder
	{
		public BBRotAnimationBuilder( double length ) { super( length ); }
		
		@Override
		public AnimationCatmullRom quickBuild()
		{
			this.flip( true, false, false );
			return super.quickBuild();
		}
	}
}
