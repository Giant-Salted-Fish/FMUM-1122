package gsf.fmum.client.input;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@FunctionalInterface
@SideOnly( Side.CLIENT )
public interface IInput
{
	boolean asBool();
	
	default float asFloat() {
		return this.asBool() ? 1.0F : 0.0F;
	}
	
//	default Vec2f asVec2() {
//		return this.asFlag() ? new Vec2f( 1.0F, 1.0F ) : Vec2f.ORIGIN;
//}
}
