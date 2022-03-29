package com.fmum.client.model;

public class Animation implements Comparable<Animation>
{
	public static final Animation INSTANCE = new Animation();
	
	public void launch() { }
	
	/**
	 * Tick current animation
	 * 
	 * @return {@code true} if this animation has complete
	 */
	public boolean tick() { return false; }
	
	public int getKeyTick() { return 0; }
	
	@Override
	public int compareTo(Animation a)
	{
		int i = this.getKeyTick();
		int j = a.getKeyTick();
		return i > j ? 1 : i < j ? -1 : 0;
	}
}
