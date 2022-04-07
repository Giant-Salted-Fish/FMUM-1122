package com.fmum.common.type;

import com.fmum.client.KeyManager.Key;
import com.fmum.client.model.ModelDebugBox;

import net.minecraft.item.ItemStack;
import net.minecraft.util.MouseHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Base item type for all items accepted by FMUM
 * 
 * @author Giant_Salted_Fish
 */
public interface ItemInfo
{
	public static final ItemInfo NONE = new ItemInfo()
	{
		@Override
		public TypeInfo getType() { return null; }
		
		@Override
		@SideOnly(Side.CLIENT)
		public void tick(ItemStack stack)
		{
			// Tick default model to apply primary camera control
			ModelDebugBox.INSTANCE.itemTick(stack, null);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void renderTick(ItemStack stack, MouseHelper mouse) {
			ModelDebugBox.INSTANCE.itemRenderTick(ItemStack.EMPTY, null, mouse);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void renderFP(ItemStack stack) { }
		
		@Override
		@SideOnly(Side.CLIENT)
		public void render() { }
	};
	
	/**
	 * @return Type that this item proxy for
	 */
	public TypeInfo getType();
	
	/**
	 * @return {@code true} if view bobbing should be disabled when holding this item
	 */
	@SideOnly(Side.CLIENT)
	default public boolean disableViewBobbing() { return false; }
	
	@SideOnly(Side.CLIENT)
	default public boolean disableCrosshair() { return false; }
	
	/**
	 * Called when player switched to this item
	 */
	@SideOnly(Side.CLIENT)
	default public void onTakeOut(ItemStack stack) { }
	
	/**
	 * Called when player is holding this item. In default it ticks the model and return
	 * {@code false}.
	 */
	@SideOnly(Side.CLIENT)
	default public void tick(ItemStack stack)
	{
		final TypeInfo type = this.getType();
		type.model.itemTick(stack, type);
	}
	
	@SideOnly(Side.CLIENT)
	default public void renderTick(ItemStack stack, MouseHelper mouse)
	{
		final TypeInfo type = this.getType();
		type.model.itemRenderTick(stack, type, mouse);
	}
	
	@SideOnly(Side.CLIENT)
	default public void renderFP(ItemStack stack)
	{
		final TypeInfo type = this.getType();
		type.model.renderFP(stack, type);
	}
	
	@SideOnly(Side.CLIENT)
	default public void render() { this.getType().model.render(); }
	
	/**
	 * Called client side when a key is pressed
	 * 
	 * @param key Key been pressed
	 */
	@SideOnly(Side.CLIENT)
	default public void keyNotify(Key key) { }
}
