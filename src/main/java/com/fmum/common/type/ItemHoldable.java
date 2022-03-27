package com.fmum.common.type;

import java.util.List;

import javax.annotation.Nullable;

import com.fmum.common.FMUM;
import com.fmum.common.pack.FMUMContentProvider;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Base item template for items that can not be used to break block 
 * 
 * @author Giant_Salted_Fish
 */
public abstract class ItemHoldable extends Item implements ItemPaintable
{
	@Override
	public final boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player) {
		return true;
	}
	
	@Override
	public final boolean canDestroyBlockInCreative(
		World world,
		BlockPos pos,
		ItemStack stack,
		EntityPlayer player
	) { return false; }
	
	@Override
	public final String getTranslationKey(ItemStack stack) {
		return ItemPaintable.super.getTranslationKey(stack);
	}
	
//	@Override
//	public void onUpdate(
//		ItemStack stack,
//		World worldIn,
//		Entity entityIn,
//		int itemSlot,
//		boolean isSelected
//	) { }
	
	@Override
	@SideOnly(Side.CLIENT)
	public final boolean isFull3D() { return true; }
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(
		ItemStack stack,
		@Nullable World worldIn,
		List<String> tooltip,
		ITooltipFlag flagIn
	) {
		FMUMContentProvider pack = this.getType().provider;
		tooltip.add(
			"\u00a7o" + I18n.format(
				"tooltip.providerinfo",
				I18n.format(pack.getName()),
				I18n.format(pack.getAuthor())
			)
		);
		
		this.addAttriInfo(stack, tooltip);
		
		tooltip.add("");
		for(String s : this.getType().description)
			tooltip.add(I18n.format(s));
	}
	
	@Override
	public final String getCreatorModId(ItemStack itemStack) { return FMUM.MODID; }
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderFP(ItemStack stack)
	{
		final TypeInfo type = this.getType();
		type.model.renderFP(stack, type);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void render() { this.getType().model.render(); }
	
	@SideOnly(Side.CLIENT)
	protected void addAttriInfo(ItemStack stack, List<String> tooltip) { }
}
