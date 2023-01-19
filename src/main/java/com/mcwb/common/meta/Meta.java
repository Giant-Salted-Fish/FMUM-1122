package com.mcwb.common.meta;

/**
 * Simple implementation for {@link IMeta}
 * 
 * @author Giant_Salted_Fish
 */
public abstract class Meta implements IMeta
{
	protected String name;
	
	@Override
	public String name() { return this.name; }
	
	@Override
	public int hashCode() { return this.name.hashCode(); }
	
	@Override
	public boolean equals( Object obj ) {
		return obj instanceof IMeta && this.name.equals( ( ( IMeta ) obj ).name() );
	}
	
	@Override
	public String toString() { return this.name; }
}
