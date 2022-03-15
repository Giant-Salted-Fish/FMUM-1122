package com.fmum.client;

import java.io.File;
import java.util.HashMap;

import com.fmum.common.CommonProxy;
import com.fmum.common.FMUM;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.discovery.ContainerType;
import net.minecraftforge.fml.common.discovery.ModCandidate;

public final class ClientProxy extends CommonProxy
{
	@Override
	public void registerLocalResource(File source)
	{
		super.registerLocalResource(source);
		
		// Register it as a resource pack to load textures and sounds client side
		HashMap<String, Object> descriptor = new HashMap<>();
		descriptor.put("modid", FMUM.MODID);
		descriptor.put("name", FMUM.MOD_NAME + ":" + source.getName());
		descriptor.put("version", "1");
		FMLClientHandler.instance().addModAsResource(
			new FMLModContainer(
				"com.fmum.common.FMUM",
				new ModCandidate(
					source,
					source,
					source.isDirectory() ? ContainerType.DIR : ContainerType.JAR
				),
				descriptor
			)
		);
	}
}
