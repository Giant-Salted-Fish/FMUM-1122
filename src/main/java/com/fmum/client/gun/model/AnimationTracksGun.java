package com.fmum.client.gun.model;

import com.fmum.common.util.Animation;
import com.fmum.common.util.BBAnimationBuilder.BBPosAnimationBuilder;
import com.fmum.common.util.BBAnimationBuilder.BBRotAnimationBuilder;
import com.fmum.common.util.CoordSystem;
import com.fmum.common.util.Vec3;

public class AnimationTracksGun implements AnimationGun
{
	public static final byte
		CAMERA = 0,
		GUN_POS = 1,
		GUN_ROT = 2;
	
	protected static final Animation[] DEF_TRACKS = {
		AnimationGun.NONE,
		AnimationGun.NONE,
		AnimationGun.NONE
	};
	
	public Animation[] tracks = DEF_TRACKS;
	
	protected final Vec3 vec = new Vec3();
	
	public AnimationTracksGun(int numTracks)
	{
		this.tracks = new Animation[numTracks];
		while(--numTracks >= 0)
			this.tracks[numTracks] = AnimationGun.NONE;
	}
	
	@Override
	public void applyGunTransform(CoordSystem sys, double smoothedProgress)
	{
		this.tracks[GUN_POS].getSmoothedPos(this.vec, smoothedProgress);
		sys.trans(this.vec);
		sys.trans(-1.8D / 16D, 5D / 16D, -2.5D / 16D);
		
		this.tracks[GUN_ROT].getSmoothedPos(this.vec, smoothedProgress);
		sys.rot(this.vec);
		sys.submitRot();
		
		sys.trans(1.8D / 16D, -5D / 16D, 2.5D / 16D);
	}
	
	public static final AnimationTracksGun VIEW_WEAPON = new AnimationTracksGun(3);
	static
	{
		VIEW_WEAPON.tracks[GUN_ROT]
			= new BBRotAnimationBuilder(2.52D)
				.append(0D, 0D, 0D, 0.0D)
				.append(0D, 0D, 5D, 0.12D)
				.append(-11.78673D, -3.50172D, 22.842D, 0.28D)
				.append(-38.33D, -6.65D, 31.55D, 0.4D)
				.append(-48.87974D, -7.79023D, 34.27749D, 0.52D)
				.append(-56.68335D, -4.35035D, 33.55114D, 0.64D)
				.append(-44.54943D, -4.93618D, 33.22328D, 0.76D)
				.append(-47.52065D, -5.42D, 35.95377D, 0.88D)
				.append(-44.83268D, -7.26117D, 34.2521D, 1.12D)
				.append(-42.28446D, -7.59773D, 33.8792D, 1.32D)
				.append(-54.96074D, -9.36987D, 36.89416D, 1.48D)
				.append(-52.46074D, -9.36987D, 36.89416D, 1.6D)
				.append(-52.68452D, -7.38444D, 38.42986D, 1.72D)
				.append(-50.10981D, -8.22019D, 37.86908D, 1.84D)
				.append(-50.19547D, -8.70075D, 38.45123D, 1.96D)
			.quickBuild();
		VIEW_WEAPON.tracks[GUN_POS]
			= new BBPosAnimationBuilder(2.52D)
				.append(0D, 0D, 0D, 0.0D)
//				.append(-0.25D, -0.5D, 0.25D, 0.12D)
//				.append(-0.26D, 0.53D, 0.34D, 0.28D)
//				.append(-1D, 1.25D, 1.34D, 0.4D)
//				.append(-0.25D, 1.5D, 1.75D, 0.52D)
//				.append(0.25D, 1D, 1.75D, 0.64D)
//				.append(0.28D, 0.97D, 1.63D, 0.76D)
//				.append(0.25D, 1D, 1.75D, 0.88D)
//				.append(0.25D, 1D, 1.5D, 1.12D)
//				.append(0.25D, 1D, 1.53625D, 1.32D)
//				.append(0.25D, 1D, 1.75D, 1.48D)
//				.append(0.25D, 1D, 1.5D, 1.6D)
			.quickBuild();
	}
	
	public static final AnimationTracksGun RELOAD = new AnimationTracksGun(3);
	static
	{
		RELOAD.tracks[GUN_ROT]
			= new BBRotAnimationBuilder(5.52D)
				.append(0D, 0D, 0D, 0.0D)
				.append(7.5D, 0D, 0D, 0.12D)
				.append(-10.23995D, -4.4837D, 3.33966D, 0.24D)
				.append(-21.00293D, -4.17867D, 11.25987D, 0.36D)
				.append(-23.73021D, -8.76038D, 13.27524D, 0.48D)
				.append(-28.73021D, -8.76038D, 13.27524D, 0.6D)
				.append(-28.58686D, -6.51224D, 12.16091D, 0.72D)
				.append(-24.02783D, -4.49284D, 16.7564D, 0.84D)
				.append(-18.68225D, -6.37868D, 13.00991D, 0.96D)
				.append(-21.02303D, -5.12283D, 8.82378D, 1.12D)
				.append(-18.71D, -4.22D, 11.16D, 1.28D)
				.append(-16.21367D, -4.22197D, 11.16363D, 1.48D)
				.append(-18.66751D, -1.85178D, 10.35786D, 1.72D)
				.append(-21.29789D, -6.54817D, 12.0725D, 1.84D)
				.append(-23.32298D, -9.97468D, 10.71172D, 2.08D)
				.append(-20.70635D, -7.59618D, 9.91715D, 2.2D)
				.append(-23.20635D, -7.59618D, 9.91715D, 2.32D)
				.append(-28.13543D, -7.80185D, 9.52962D, 2.4D)
				.append(-18.39278D, -9.80179D, 11.11807D, 2.56D)
				.append(-13.33652D, -7.29182D, 10.73983D, 2.76D)
				.append(-15.5084D, -9.58557D, 11.39856D, 2.92D)
				.append(-18.33652D, -7.29182D, 10.73983D, 3.12D)
				.append(-15.83652D, -7.29182D, 10.73983D, 3.36D)
				.append(-5.78184D, -4.83156D, 10.26718D, 3.48D)
				.append(-8.23877D, -2.42551D, 9.58684D, 3.68D)
				.append(-11.78298D, -2.24929D, 9.53701D, 3.8D)
				.append(-6.64554D, -2.30286D, 9.55215D, 3.88D)
				.append(-10.73877D, -2.42551D, 9.58684D, 3.96D)
				.append(-13.12351D, -2.99568D, 7.14998D, 4.24D)
				.append(-3.95143D, -3.65619D, 2.71016D, 4.4D)
				.append(7.89878D, -2.6218D, 1.57827D, 4.6D)
				.append(0D, 0D, 0D, 4.96D)
				.append(0D, 0D, 0D, 5.12D)
			.quickBuild();
		RELOAD.tracks[GUN_POS]
			= new BBPosAnimationBuilder(5.52D)
				.append(0D, 0D, 0D, 0.0D)
				.append(-1D, -0.25D, 0D, 0.12D)
				.append(-1.25D, 0D, 0D, 0.24D)
				.append(-1.25D, 0D, 0D, 0.36D)
				.append(-1.25D, 0.25D, 0.25D, 0.48D)
				.append(-1.25D, 0.5D, 0.5D, 0.6D)
				.append(-0.5D, 0.5D, 0.5D, 0.72D)
				.append(0.25D, 0.5D, 0.5D, 0.84D)
				.append(-0.08D, 0.5D, 0.25D, 0.96D)
				.append(-0.25D, 0.5D, 0.5D, 1.12D)
				.append(-0.25D, 0.5D, 0.25D, 1.28D)
				.append(-0.25D, 0.5D, 0.25D, 1.48D)
				.append(-0.25D, 0.5D, 0.25D, 1.72D)
				.append(-0.25D, 0.25D, 0.25D, 1.84D)
				.append(-0.25D, 0.75D, 0.25D, 2.08D)
				.append(-0.25D, 0.75D, 0D, 2.2D)
				.append(-0.25D, 0.84D, 0.07D, 2.32D)
				.append(-0.25D, 0.86D, 0.09D, 2.4D)
				.append(-0.25D, 0.75D, 0D, 2.56D)
				.append(-0.25D, 0.5D, -0.25D, 2.76D)
				.append(-0.5D, 0.25D, -0.25D, 3.12D)
				.append(-0.25D, 0.75D, 0D, 3.36D)
				.append(-0.25D, 0.5D, -0.25D, 3.48D)
				.append(-0.25D, 0.5D, 0D, 3.68D)
				.append(-0.23D, 1.22313D, -0.01125D, 3.96D)
				.append(-0.25D, 0.82813D, 0D, 4.24D)
				.append(-0.5D, 0.55D, 0D, 4.4D)
				.append(-0.12D, 0.12D, 0D, 4.6D)
				.append(0.5D, 0.03D, 0D, 4.76D)
				.append(0D, -0.125D, 0D, 4.96D)
				.append(0D, 0D, 0D, 5.12D)
			.quickBuild();
	}
}
