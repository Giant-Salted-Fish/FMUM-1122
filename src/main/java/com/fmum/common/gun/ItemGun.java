package com.fmum.common.gun;

import com.fmum.client.FMUMClient;
import com.fmum.client.KeyManager.Key;
import com.fmum.client.OperationProgressive;
import com.fmum.client.gun.model.AnimationTracksGun;
import com.fmum.common.module.TagModular;
import com.fmum.common.network.PacketInitModuleTag;
import com.fmum.common.type.ItemHoldable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class ItemGun extends ItemHoldable implements ItemAmmoContainer
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
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean disableViewBobbing() { return true; }
	
	@Override
	@SideOnly(Side.CLIENT)
	public void tick(ItemStack stack)
	{
		if(!TagModular.validateTag(stack))
		{
			FMUMClient.netHandler.sendToServer(new PacketInitModuleTag());
			return;
		}
		
		// TODO: ton of tick task
		
		
		this.type.model.itemTick(stack, this.type);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderFP(ItemStack stack) { this.type.model.renderFP(stack, this.type); }
	
	@Override
	@SideOnly(Side.CLIENT)
	public void render() { this.type.model.render(); }
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean disableCrosshair() { return true; }
	
	@Override
	@SideOnly(Side.CLIENT)
	public void keyNotify(Key key)
	{
		ItemAmmoContainer.super.keyNotify(key);
		
		switch(key)
		{
		case VIEW_WEAPON:
		case CO_VIEW_WEAPON:
			FMUMClient.tryLaunchOp(new OperationProgressive() { { this.progressor = 1D / 5.52D / 20D; } });
			((ItemGun)FMUMClient.prevItem).type.model.getAnimatorFP().launchAnimation(AnimationTracksGun.RELOAD);
		default:;
		}
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
