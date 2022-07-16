package com.fmum.common.item;

import java.util.List;

import com.fmum.common.FMUM;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Proxy the item and pass all essential method call to bounden meta
 * 
 * @author Giant_Salted_Fish
 */
public class ItemProxy< T extends MetaItem > extends Item implements MetaHostItem
{
	public final T meta;
	
	public ItemProxy( T meta ) { this.meta = meta; }
	
	@Override
	public T meta() { return this.meta; }
	
	@Override
	public boolean onBlockStartBreak( ItemStack itemstack, BlockPos pos, EntityPlayer player ) {
		return this.meta.onBlockStartBreak( itemstack, pos, player );
	}
	
	@Override
	public boolean canDestroyBlockInCreative(
		World world, BlockPos pos, ItemStack stack, EntityPlayer player
	) { return this.meta.canDestroyBlockInCreative( world, pos, stack, player ); }
	
	@Override
	public String getUnlocalizedName( ItemStack stack ) {
		return this.meta.unlocalizedName( stack );
	}
	
	// TODO: check if update method here is required
//	@Override
//	public void onUpdate(
//		ItemStack stack,
//		World worldIn,
//		Entity entityIn,
//		int itemSlot,
//		boolean isSelected
//	) {
//		super.onUpdate( stack, worldIn, entityIn, itemSlot, isSelected );
//	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public final boolean isFull3D() { return true; }
	
	@Override
	@SideOnly( Side.CLIENT )
	public void addInformation(
		ItemStack stack,
		World worldIn,
		List< String > tooltip,
		ITooltipFlag flagIn
	) { this.meta.addInformation( stack, worldIn, tooltip, flagIn ); }
	
	@Override
	public final String getCreatorModId( ItemStack itemStack ) { return FMUM.MODID; }
}
