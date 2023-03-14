package com.mcwb.common.meta;

import com.mcwb.common.MCWB;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/**
 * Root interface for all elements managed by {@link MCWB}
 * 
 * @author Giant_Salted_Fish
 * TODO: validate if this needed(like IRenderer)
 */
public interface IMeta //extends Comparable< IMeta >
{
	/**
	 * For contexted meta
	 */
	@CapabilityInject( Object.class )
	public static final Capability< Object > CONTEXTED = null;
	
	/**
	 * @return Name of the meta
	 */
	public String name();
	
	/**
	 * To retrieve additional data from this meta. Reserved for soft extension.
	 * 
	 * @param key Key of the meta to retrieve
	 * @return Corresponding meta data. {@code null} if does not present.
	 */
//	@Nullable
//	public default Object getMeta( Object key ) { return null; }
	
	/**
	 * In default the meta will be compared via their name
	 */
//	@Override
//	public default int compareTo( IMeta m ) { return this.name().compareTo( m.name() ); }
}
