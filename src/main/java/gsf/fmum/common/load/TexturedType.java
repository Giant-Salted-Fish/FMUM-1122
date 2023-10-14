package gsf.fmum.common.load;

import gsf.fmum.client.FMUMClient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

public abstract class TexturedType extends BuildableType
{
	@SideOnly( Side.CLIENT )
	protected ResourceLocation texture;
	
	protected TexturedType() { }
	
	@SideOnly( Side.CLIENT )
	public void buildClientSide( IContentBuildContext ctx )
	{
		super.buildClientSide( ctx );
		
		this.texture = Optional.ofNullable( this.texture )
			.orElseGet( () -> this._fallbackTexture( ctx ) );
	}
	
	@SideOnly( Side.CLIENT )
	protected ResourceLocation _fallbackTexture( IContentBuildContext ctx ) {
		return FMUMClient.TEXTURE_GREEN;
	}
}
