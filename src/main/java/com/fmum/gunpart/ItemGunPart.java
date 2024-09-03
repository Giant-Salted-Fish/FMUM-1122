package com.fmum.gunpart;

import com.fmum.FMUM;
import com.fmum.item.FMUMVanillaItem;
import com.fmum.item.IItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Random;

public class ItemGunPart extends FMUMVanillaItem
{
	public static final ItemGunPart INSTANCE = new ItemGunPart();
	static {
		INSTANCE.setRegistryName( FMUM.MODID, "gun_part" );
	}
	
	protected static final String CAPABILITY_TAG = "~";
	
	
	protected ItemGunPart() {
		this.setMaxStackSize( 1 );
	}
	
	@Nonnull
	@Override
	public String getTranslationKey() {
		return "item.gun_part";
	}
	
	@Nonnull
	@Override
	public String getTranslationKey( @Nonnull ItemStack stack )
	{
		// Because we can not make sure this stack is not created with \
		// {new ItemStack(...)}, #getItemFrom(...) here could return \
		// IItem#VANILLA as a fallback delegate.
		final IItem item = this.getItemFrom( stack );
		return item != null ? "item." + item.getType().getName() : this.getTranslationKey();
	}
	
	// TODO: Handle paintjobs with sub method override.
	
	@Override
	@SuppressWarnings( "DataFlowIssue" )
	public IItem getItemFrom( ItemStack stack ) {
		return stack.getCapability( IItem.CAPABILITY, null );
	}
	
	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(
		@Nonnull ItemStack stack,
		@Nullable NBTTagCompound cap_nbt
	) {
		// 4 case to handle: \
		// has-stack_nbt & has-cap_nbt: {ItemStack#ItemStack(NBTTagCompound)} \
		// has-stack_nbt & no--cap_nbt: \
		// no--stack_nbt & has-cap_nbt: {ItemStack#copy()} \
		// no--stack_nbt & no--cap_nbt: {new ItemStack(...)}, {PacketBuffer#readItemStack()} \
		//
		// Case 1/3 -> CapabilityProvider#deserializeNBT(...) will be called.
		// Case 4 -> See #getNBTShareTag(...) and #readNBTShareTag(...).
		if ( stack.getTagCompound() == null )
		{
			final NBTTagCompound nbt = new NBTTagCompound();
			final int stack_id = new Random().nextInt();  // TODO: Buffer rand obj.
			nbt.setInteger( GunPartType.STACK_ID_TAG, stack_id );
			stack.setTagCompound( nbt );
		}
		
		return new GunPartCapProvider( stack );
	}
	
	@Override
	@SuppressWarnings( "DataFlowIssue" )
	public NBTTagCompound getNBTShareTag( @Nonnull ItemStack stack )
	{
		// Copy to avoid changing the original NBT of the stack.
		final NBTTagCompound stack_nbt = Objects.requireNonNull( stack.getTagCompound() );
		final NBTTagCompound copied_nbt = stack_nbt.copy();
		
		final GunPartCapProvider provider = stack.getCapability( GunPartCapProvider.CAPABILITY, null );
		final NBTTagCompound cap_nbt = provider.serializeNBT();
		copied_nbt.setTag( CAPABILITY_TAG, cap_nbt );
		return copied_nbt;
	}
	
	@Override
	@SuppressWarnings( "DataFlowIssue" )
	public void readNBTShareTag( @Nonnull ItemStack stack, @Nullable NBTTagCompound nbt )
	{
		final NBTTagCompound cap_nbt = nbt.getCompoundTag( CAPABILITY_TAG );
		nbt.removeTag( CAPABILITY_TAG );
		super.readNBTShareTag( stack, nbt );
		
		final GunPartCapProvider provider = stack.getCapability( GunPartCapProvider.CAPABILITY, null );
		provider.deserializeNBT( cap_nbt );
	}
	
	// Avoid breaking blocks when holding this item in survive mode.
	@Override
	public boolean onBlockStartBreak(
		@Nonnull ItemStack itemstack,
		@Nonnull BlockPos pos,
		@Nonnull EntityPlayer player
	) {
		return true;
	}
	
	// Avoid breaking blocks when holding this item in creative mode.
	@Override
	public boolean canDestroyBlockInCreative(
		@Nonnull World world,
		@Nonnull BlockPos pos,
		@Nonnull ItemStack stack,
		@Nonnull EntityPlayer player
	) {
		return false;
	}
}
