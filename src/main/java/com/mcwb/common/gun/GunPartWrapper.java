package com.mcwb.common.gun;

import java.util.Collection;

import com.mcwb.client.module.IDeferredRenderer;
import com.mcwb.client.render.IAnimator;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.module.ModuleWrapper;
import com.mcwb.util.ArmTracker;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GunPartWrapper<
	I extends IGunPart< ? extends I >,
	T extends IGunPart< ? extends I >
> extends ModuleWrapper< I, T > implements IGunPart< I >
{
	protected final ItemStack stack;
	
	protected GunPartWrapper( T primary, ItemStack stack )
	{
		super( primary );
		
		this.stack = stack;
	}
	
	@Override
	public final int stackId() { return this.stack.getTagCompound().getInteger( "i" ); }
	
	@Override
	public final ItemStack toStack() { return this.stack; }
	
	@Override
	public final IEquippedItem< ? > onTakeOut( EntityPlayer player, EnumHand hand ) {
		return this.primary.onTakeOut( player, hand );
	}
	
	@Override
	public IEquippedItem< ? > onStackUpdate(
		IEquippedItem< ? > prevEquipped,
		EntityPlayer player,
		EnumHand hand
	) { return this.primary.onStackUpdate( prevEquipped, player, hand ); }
	
	@Override
	public int leftHandPriority() { return this.primary.leftHandPriority(); }
	
	@Override
	public int rightHandPriority() { return this.primary.rightHandPriority(); }
	
	@Override
	@SideOnly( Side.CLIENT )
	public void prepareInHandRenderSP(
		IAnimator animator,
		Collection< IDeferredRenderer > renderQueue0,
		Collection< IDeferredRenderer > renderQueue1
	) { throw new RuntimeException(); }
	
	@Override
	@SideOnly( Side.CLIENT )
	public void setupLeftArmToRender( IAnimator animator, ArmTracker leftArm ) {
		throw new RuntimeException();
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void setupRightArmToRender( IAnimator animator, ArmTracker rightArm ) {
		throw new RuntimeException();
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public final ResourceLocation texture() { throw new RuntimeException(); }
	
	@Override
	protected final void syncNBTData() {
		this.stack.getTagCompound().setTag( "_", this.primary.serializeNBT() );
	}
}
