package com.fmum.client.model.gun;

import com.fmum.common.util.Animation;
import com.fmum.common.util.AnimationCatmullRom;
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
	
	@Override
	public void applyGunTransform(CoordSystem sys, double smoothedProgress)
	{
		this.tracks[GUN_POS].getSmoothedPos(this.vec, smoothedProgress);
		sys.trans(this.vec);
		
		this.tracks[GUN_ROT].getSmoothedPos(this.vec, smoothedProgress);
//		this.vec.x = -this.vec.x; TODO
		sys.rot(this.vec);
		sys.submitRot();
	}
	
	public static final AnimationTracksGun TEST = new AnimationTracksGun();
	static
	{
		TEST.tracks[GUN_POS]
			= new AnimationCatmullRom.Builder(2.52D)
				.append(0D, 0D, 0D, 0.0D)
				.append(-0.25D, -0.5D, 0.25D, 0.12D)
				.append(-0.26D, 0.53D, 0.34D, 0.28D)
				.append(-1D, 1.25D, 1.34D, 0.4D)
				.append(-0.25D, 1.5D, 1.75D, 0.52D)
				.append(0.25D, 1D, 1.75D, 0.64D)
				.append(0.28D, 0.97D, 1.63D, 0.76D)
				.append(0.25D, 1D, 1.75D, 0.88D)
				.append(0.25D, 1D, 1.5D, 1.12D)
				.append(0.25D, 1D, 1.53625D, 1.32D)
				.append(0.25D, 1D, 1.75D, 1.48D)
				.append(0.25D, 1D, 1.5D, 1.6D)
			.scale(1D / 16D)
			.quickBuild();
		TEST.tracks[GUN_ROT]
			= new AnimationCatmullRom.Builder(2.52D)
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
	}
}
