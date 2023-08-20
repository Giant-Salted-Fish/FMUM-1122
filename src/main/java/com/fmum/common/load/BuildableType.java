package com.fmum.common.load;

import com.fmum.common.pack.ContentPack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

public abstract class BuildableType
{
	protected String name;
	
	protected ContentPack loaded_from;
	
	protected BuildableType() { }
	
	public void buildServerSide( ContentBuildContext ctx )
	{
		this.name = Optional.ofNullable( this.name )
			.orElseGet( ctx::fallbackName );
		this.loaded_from = ctx.contentPack();
	}
	
	@SideOnly( Side.CLIENT )
	public void buildClientSide( ContentBuildContext ctx ) {
		this.buildServerSide( ctx );
	}
	
	public String name() {
		return this.name;
	}
	
	public String toString()
	{
		return String.format(
			"%s::<%s.%s>", this._typeHint(),
			this.loaded_from.toString(), this.name );
	}
	
	protected abstract String _typeHint();
}
