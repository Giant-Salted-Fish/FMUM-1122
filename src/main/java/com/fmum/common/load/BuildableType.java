package com.fmum.common.load;

import com.fmum.common.pack.IContentPack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

public abstract class BuildableType
{
	protected String name;
	
	protected IContentPack from_pack;
	
	protected BuildableType() { }
	
	public void buildServerSide( IContentBuildContext ctx )
	{
		this.name = Optional.ofNullable( this.name )
			.orElseGet( ctx::fallbackName );
		this.from_pack = ctx.contentPack();
	}
	
	@SideOnly( Side.CLIENT )
	public void buildClientSide( IContentBuildContext ctx ) {
		this.buildServerSide( ctx );
	}
	
	public String name() {
		return this.name;
	}
	
	public String toString()
	{
		return String.format(
			"%s::<%s.%s>", this._typeHint(),
			this.from_pack.toString(), this.name
		);
	}
	
	protected abstract String _typeHint();
}
