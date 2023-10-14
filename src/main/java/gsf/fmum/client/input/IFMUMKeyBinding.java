package gsf.fmum.client.input;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Set;

@FunctionalInterface
@SideOnly( Side.CLIENT )
public interface IFMUMKeyBinding {
	void setKeyCodeAndCombinations( int key_code, Set< Integer > combinations );
}
