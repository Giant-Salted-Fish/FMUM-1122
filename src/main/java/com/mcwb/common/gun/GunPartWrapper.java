package com.mcwb.common.gun;

import java.util.Collection;

import com.mcwb.client.module.IDeferredPriorityRenderer;
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
	M extends IGunPart< ? extends M >,
	T extends IGunPart< ? extends M >
> extends ModuleWrapper< M, T > implements IGunPart< M >
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
	public int leftHandPriority() { return this.primary.leftHandPriority(); }
	
	@Override
	public int rightHandPriority() { return this.primary.rightHandPriority(); }
	
	@Override
	@SideOnly( Side.CLIENT )
	public void prepareInHandRenderSP(
		IAnimator animator,
		Collection< IDeferredRenderer > renderQueue0,
		Collection< IDeferredPriorityRenderer > renderQueue1
	) { throw new RuntimeException(); }
	
	@Override
	@SideOnly( Side.CLIENT )
	public void setupLeftArmToRender( ArmTracker leftArm, IAnimator animator ) {
		throw new RuntimeException();
	}
	
	@Override
	@SideOnly( Side.CLIENT )
	public void setupRightArmToRender( ArmTracker rightArm, IAnimator animator ) {
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
