package com.fmum.common.gun;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public final class ItemGun extends ItemAmmoContainer
{
	public final TypeGun type;
	
	public ItemGun(TypeGun type) { this.type = type; }
	
	@Override
	public TypeGun getType() { return this.type; }
	
	@Override
	public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.BOW; }

	@Override
	public int getMaxItemUseDuration(ItemStack stack) { return 100; }
	
	@Override
	// Side only maybe
	public void onUpdate(
		ItemStack stack,
		World worldIn,
		Entity entityIn,
		int itemSlot,
		boolean isSelected
	) {
		if(!worldIn.isRemote || !isSelected) return;
	}
	
	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		return true;
	}
	
//	@Override
//	@SideOnly(Side.CLIENT)
//	public void addInformation(
//		ItemStack stack,
//		@Nullable World worldIn,
//		List<String> tooltip,
//		ITooltipFlag flagIn
//	) {
//		
//	}
	
//	@Override TODO
//	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
//	{
//		return !oldStack.equals(newStack); //!ItemStack.areItemStacksEqual(oldStack, newStack);
//	}

//	@Override
//	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
//	{
//		return new ActionResult<ItemStack>(EnumActionResult.PASS, playerIn.getHeldItem(handIn));
//	}
//	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase entityLiving)
//	{
//		return stack;
//	}
	
//	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker)
//	{
//		return false;
//	}

//	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft)
//	{
//	}
	
//	public boolean canItemEditBlocks()
//	{
//		return false;
//	}
	
//	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
//	{
//		return EnumActionResult.PASS;
//	}
	
//	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer playerIn, EntityLivingBase target, EnumHand hand)
//	{
//		return false;
//	}
	
//	@Override
//	public boolean onEntitySwing(EntityLivingBase entityLiving, ItemStack stack)
//	{
//		return true;
//	}
	
//	@Override
//	public EnumActionResult onItemUse(
//		EntityPlayer player,
//		World worldIn,
//		BlockPos pos,
//		EnumHand hand,
//		EnumFacing facing,
//		float hitX, float hitY, float hitZ
//	) { return EnumActionResult.PASS; }
}
