package com.mcwb.util;
//package com.mcwb.util;
//
//public class SecondOrderDynamics
//{
//	protected float tar = 0F;
//	protected float prevTar = 0F;
//	
//	protected float pos = 0F;
//	protected float vel = 0F;
//	protected float acc = 0F;
//	
//	protected float k1;
//	protected float k2;
//	protected float k3;
//	
//	/**
//	 * TODO
//	 * 
//	 * @param naturalFrequency How fast the system will converge
//	 * @param dampingCoefficient
//	 * @param initialResponse
//	 */
//	public SecondOrderDynamics(
//		float naturalFrequency,
//		float dampingCoefficient,
//		float initialResponse
//	) {
//		this.k1 = dampingCoefficient / ( Util.PI * naturalFrequency );
//		
//		final float divisor = 2F * Util.PI * naturalFrequency;
//		this.k2 = 1F / ( divisor * divisor );
//		
//		this.k3 = initialResponse * dampingCoefficient / ( 2F * Util.PI * naturalFrequency );
//	}
//	
//	public void update( float deltaTime )
//	{
//		
//	}
//	
//	public float setTar( float tar )
//	{
//		final float old = this.tar;
//		this.tar = tar;
//		return old;
//	}
//}
