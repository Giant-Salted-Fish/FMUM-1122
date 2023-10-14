package gsf.fmum.common.paintjob;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@FunctionalInterface
public interface IPaintjob
{
	@SideOnly( Side.CLIENT )
	ResourceLocation texture();
}
