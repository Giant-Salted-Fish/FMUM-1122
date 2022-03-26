package com.fmum.client;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.lwjgl.opengl.GLContext;

import com.fmum.client.model.Model;
import com.fmum.client.model.ModelDebugBox;
import com.fmum.common.CommonProxy;
import com.fmum.common.FMUM;
import com.fmum.common.FMUMClassLoader;
import com.fmum.common.pack.FMUMCreativeTab;
import com.fmum.common.util.InstanceRepository;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.resource.VanillaResourceType;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.MetadataCollection;
import net.minecraftforge.fml.common.discovery.ContainerType;
import net.minecraftforge.fml.common.discovery.ModCandidate;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class ClientProxy extends CommonProxy
{
	public static final String RECOMMENDED_TEXTURE_FOLDER = "skins" + File.separator;
	
	public static File keyBindsFile = null;
	
	/**
	 * Override this to skip server only localization map loading
	 */
	@Override
	public void syncConfig(Configuration config, File minecraftDir)
	{
		this.parseConfig(config);
		
		// Save configuration file if has changed
		if(config.hasChanged())
			config.save();
	}
	
	/**
	 * Localization map is only used in server side. Skip loading.
	 */
	@Override
	public void loadLocalizationMap() { }
	
	@Override
	public String format(String translateKey, Object... parameters) {
		return I18n.format(translateKey, parameters);
	}
	
	@Override
	public String addLocalizeKey(String key, String formator) { return null; }
	
	@Override
	public void checkOpenGL()
	{
		if(!GLContext.getCapabilities().OpenGL30)
			throw new RuntimeException(I18n.format("fmum.openglversiontoolow"));
	}
	
	@Override
	public void loadContentPack()
	{
		super.loadContentPack();
		
		// Force resource reload to load resources in content packs
		// TODO: maybe check if is only mod based content pack loaded
		FMLClientHandler.instance().refreshResources(
			VanillaResourceType.MODELS,
			VanillaResourceType.TEXTURES,
			VanillaResourceType.SOUNDS,
			VanillaResourceType.LANGUAGES
		);
	}
	
	@Override
	public void setupCreativeTabs()
	{
		for(FMUMCreativeTab tab : FMUMCreativeTab.tabs.values())
			tab.setupIconStack();
	}
	
	@Override
	public void loadKeyBinds()
	{
		// Create options file if not exist
		if(!keyBindsFile.exists())
		{
			try { keyBindsFile.createNewFile(); }
			catch(IOException e) {
				FMUM.log.error(I18n.format("fmum.errorcreatingkeybindsfile"), e);
			}
			KeyManager.saveTo(keyBindsFile);
		}
		
		// Otherwise, read key bind settings from the file
		// NOTICE: an empty key bind file may fail to trigger lazy load of keys
		else KeyManager.readFrom(keyBindsFile);
	}
	
	@Override
	public void registerLocalResource(File source)
	{
		super.registerLocalResource(source);
		
		// Register it as a resource pack to load textures and sounds client side
		HashMap<String, Object> descriptor = new HashMap<>();
		descriptor.put("modid", FMUM.MODID);
		descriptor.put("name", FMUM.MOD_NAME + ":" + source.getName());
		descriptor.put("version", "1");
		FMLModContainer container = new FMLModContainer(
			"com.fmum.common.FMUM",
			new ModCandidate(
				source,
				source,
				source.isDirectory() ? ContainerType.DIR : ContainerType.JAR
			),
			descriptor
		);
		container.bindMetadata(MetadataCollection.from(null, ""));
		FMLClientHandler.instance().addModAsResource(container);
	}
	
	private static final HashMap<String, InstanceRepository<? extends Model>>
		modelRepositories = new HashMap<>();
	@SuppressWarnings("unchecked")
	public Model loadModel(String modelPath)
	{
		final int i = modelPath.indexOf(':');
		try
		{
			if(i < 0)
				return (Model)FMUMClassLoader.INSTANCE.loadClass(
					modelPath
				).getConstructor().newInstance();
			
			final String repositoryName = modelPath.substring(0, i);
			InstanceRepository<? extends Model> repository = modelRepositories.get(repositoryName);
			if(repository == null)
				modelRepositories.put(
					repositoryName,
					repository = (InstanceRepository<? extends Model>)FMUMClassLoader
						.INSTANCE.loadClass(repositoryName).getConstructor().newInstance()
				);
			Model model = repository.fetch(modelPath.substring(i + 1));
			if(model != null) return model;
			
			FMUM.log.error(
				this.format(
					"fmum.modelnotfoundinrepositroy",
					modelPath.substring(i + 1),
					repositoryName
				)
			);
		}
		catch(Exception e) { FMUM.log.error(this.format("fmum.errorloadingmodel", modelPath), e); }
		return ModelDebugBox.INSTANCE;
	}
	
	@Override
	protected void parseConfig(Configuration config)
	{
		super.parseConfig(config);
		final String CLIENT_SETTING = "Client";
		
		// Parse client side only settings
		keyBindsFile = new File(
			mcDir,
			config.getString(
				"keyBindsFile",
				CLIENT_SETTING,
				"config/fmumoptions.txt",
				"File name where FMUM will save key binds to"
			)
		);
	}
}