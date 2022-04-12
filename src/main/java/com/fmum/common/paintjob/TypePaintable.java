package com.fmum.common.paintjob;

import java.util.ArrayList;
import java.util.HashMap;

import com.fmum.client.ResourceManager;
import com.fmum.common.FMUM;
import com.fmum.common.type.ItemVariant;
import com.fmum.common.type.TypeInfo;
import com.fmum.common.type.TypeTextParser.LocalTypeFileParser;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;

public abstract class TypePaintable extends TypeInfo
{
	public static final HashMap<String, TypePaintable> paintables = new HashMap<>();
	
	public static final LocalTypeFileParser<TypePaintable>
		parser = new LocalTypeFileParser<>(TypeInfo.parser);
	static
	{
		parser.addKeyword(
			"Paintjob",
			(s, t) -> {
				ItemVariant paintjob = new ItemVariant(s[1]).notifyProvider(t.provider);
				paintjobParser.parse(s, paintjob);
				t.paintjobs.add(paintjob);
			}
		);
	}
	
	// FIXME: sort the list to avoid dismatch of index and paintjobs on server side and client side
	public ArrayList<ItemVariant> paintjobs = new ArrayList<>();
	
	protected TypePaintable(String name)
	{
		super(name);
		
		// Add itself as the default paint job
		this.paintjobs.add(this);
	}
	
	@Override
	public void postParse()
	{
		super.postParse();
		
		paintables.put(this.name, this);
	}
	
	@Override
	public void onModelRegister(ModelRegistryEvent evt)
	{
		for(int i = this.paintjobs.size(); i-- > 0; )
		{
			final ResourceLocation resLoc = new ResourceLocation(
				FMUM.MODID,
				this.paintjobs.get(i).name // TODO
			);
			ModelLoader.registerItemVariants(this.item, resLoc);
			ModelLoader.setCustomModelResourceLocation(
				this.item,
				i,
				new ModelResourceLocation(resLoc, MODEL_RES_INV)
			);
		}
	}
	
	/**
	 * Override this method if you want to do a check before adding the paint job or prevent any
	 * paint job adding
	 * 
	 * @param paintjob Paint job to add
	 */
	public void registerExternalPaintjob(ItemVariant paintjob) { this.paintjobs.add(paintjob); }
	
	public ResourceLocation getTexture(int dam) {
		return ResourceManager.getTexture(this.paintjobs.get(dam).texture);
	}
	
	@Override
	public ResourceLocation getTexture(ItemStack stack) {
		return this.getTexture(stack.getItemDamage());
	}
}
