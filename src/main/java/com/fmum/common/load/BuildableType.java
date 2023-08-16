package com.fmum.common.load;

import java.util.Optional;

public abstract class BuildableType
{
	protected String name;
	
	protected IContentPack loaded_from;
	
	protected BuildableType() { }
	
	protected void buildServerSide( IContentBuildContext ctx )
	{
		this.name = Optional.ofNullable( this.name )
			.orElseGet( ctx::fallbackName );
		this.loaded_from = ctx.contentPack();
	}
	
	@SideOnly( Side.CLIENT )
	protected void buildClientSide( IContentBuildContext ctx ) {
		this.buildServerSide( ctx );
	}
	
	public String toString()
	{
		return String.format(
			"%s::<%s.%s>", this.typeHint(),
			this.loaded_from.toString(), this.name );
	}
	
	protected abstract String typeHint();
}
