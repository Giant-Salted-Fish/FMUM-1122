package gsf.fmum.client.input;

import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Set;

@SideOnly( Side.CLIENT )
public interface IKeyBind
{
	String identifier();
	
	String category();
	
	IKeyConflictContext conflictContext();
	
	int defaultKeyCode();
	
	Set< Integer > defaultCombinations();
	
	ActivateResult activate();
	
	void deactivate();
	
	enum ActivateResult
	{
		ACTIVATED,
		PASS
	}
}
