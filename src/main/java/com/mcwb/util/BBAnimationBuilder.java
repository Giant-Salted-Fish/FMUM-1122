package com.mcwb.util;

/**
 * Builder for BlockBench animation
 *  
 * @author Giant_Salted_Fish
 */
public abstract class BBAnimationBuilder extends CatmullRomAnimation.Builder
{
	public BBAnimationBuilder( float length ) { super( length ); }
	
	public static class BBPosAnimationBuilder extends BBAnimationBuilder
	{
		public BBPosAnimationBuilder( float length ) { super( length ); }
		
		@Override
		public CatmullRomAnimation quickBuild()
		{
			// TODO: validate if it is necessary
			this.flip( true, false, false );
			this.scale( Util.PRIMARY_SCALE );
			return super.quickBuild();
		}
	}
	
	public static class BBRotAnimationBuilder extends BBAnimationBuilder
	{
		public BBRotAnimationBuilder( float length ) { super( length ); }
		
		@Override
		public CatmullRomAnimation quickBuild()
		{
			this.flip( true, false, false );
			return super.quickBuild();
		}
	}
}
