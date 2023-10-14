package gsf.fmum.common.gun;

import gsf.fmum.client.render.IAnimator;
import gsf.fmum.common.item.IEquippedItem;
import gsf.fmum.common.item.IItem;
import gsf.fmum.common.module.ModuleWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GunPartWrapper< T extends IGunPart >
	extends ModuleWrapper< T > implements IGunPart, ICapabilityProvider
{
	public static final String
		STACK_ID_TAG = "i",
		ROOT_TAG = "_";
	
	protected final ItemStack stack;
	
	protected GunPartWrapper( T wrapped, ItemStack stack )
	{
		super( wrapped );
		
		this.stack = stack;
	}
	
	@Override
	@SuppressWarnings( "ConstantValue" )
	public boolean hasCapability(
		@Nonnull Capability< ? > capability,
		@Nullable EnumFacing facing
	) { return capability == IItem.CAPABILITY; }
	
	@Nullable
	@Override
	@SuppressWarnings( "ConstantValue" )
	public < C > C getCapability(
		@Nonnull Capability< C > capability,
		@Nullable EnumFacing facing
	) {
		final Capability< IItem > cap = IItem.CAPABILITY;
		return capability == cap ? cap.cast( this ) : null;
	}
	
	@Override
	public final int stackId()
	{
		final NBTTagCompound stack_tag = this.stack.getTagCompound();
		assert stack_tag != null;
		return stack_tag.getInteger( STACK_ID_TAG );
	}
	
	@Override
	public final ItemStack itemStack() {
		return this.stack;
	}
	
	@Override
	public final IEquippedItem< ? > onTakeOut(
		EntityPlayer player,
		EnumHand hand
	) { return this.wrapped.onTakeOut( player, hand ); }
	
	@Override
	@SideOnly( Side.CLIENT )
	public final void _prepareRenderInHand(
		IAnimator animator,
		IInHandRenderContext ctx
	) { this.wrapped._prepareRenderInHand( animator, ctx ); }
	
	@Override
	public final void _syncNBTTag()
	{
		final NBTTagCompound nbt = this.wrapped.serializeNBT();
		final NBTTagCompound stack_tag = this.stack.getTagCompound();
		assert stack_tag != null;
		stack_tag.setTag( ROOT_TAG, nbt );
	}
}
