package com.fmum.client.module;

import com.fmum.client.KeyManager.Key;
import com.fmum.client.OperationProgressive;
import com.fmum.common.FMUM;
import com.fmum.common.module.InfoModule;
import com.fmum.common.module.ItemModular;
import com.fmum.common.module.TagModular;
import com.fmum.common.module.TypeModular;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class OpModification extends OperationProgressive
{
	public static final OpModification INSTANCE = new OpModification();
	
	public byte[] modifying = null;
	public int modifyingLen = 0;
	
	public ModifyMode mode = ModifyMode.ON_SLOT;
	
	public int streamIndex = 0;
	
	public int step = 0;
	public int offset = 0;
	public int dam = 0;
	
	public TypeModular baseType = null;
	
	public int previewIndex = 0;
	public ItemStack previewStack = ItemStack.EMPTY;
	
	public boolean
		upKeyDown = false,
		downKeyDown = false,
		leftKeyDown = false,
		rightKeyDown = false,
		confirmKeyDown = false,
		cancelKeyDown = false,
		toggleKeyDown = false;
	
	private final InfoModule info = new InfoModule();
	
	public void onConfigSync() { this.modifying = new byte[FMUM.maxLayers << 1]; }
	
	public boolean isPaintMode() { return this.mode == ModifyMode.PAINTJOB; }
	
	public boolean selected() {
		return this.modifyingLen == 0 || this.modifying[this.modifyingLen - 1] >= 0;
	}
	
	public boolean nonPrimarySelected() {
		return this.modifyingLen > 0 && this.modifying[this.modifyingLen - 1] >= 0;
	}
	
	@Override
	protected void launch(ItemStack stack)
	{
		this.prevProgress
			= this.progress
			= 0D;
		this.progressor = 0.1D;
		this.modifyingLen = 0;
		this.mode = ModifyMode.ON_SLOT;
		this.streamIndex = 0;
		this.step = 0;
		this.offset = 0;
		this.dam = stack.getItemDamage();
		this.baseType = null;
		this.clearPreviewSelected();
		this.clearInputStates();
	}
	
	@Override
	protected boolean tick(ItemStack stack)
	{
		if(!super.tick(stack))
			return this.prevProgress < 0D && this.progressor < 0D;
		
		// Buffer variables
		final byte[] modifying = this.modifying;
		final int modifyingLen = this.modifyingLen;
		
		// Get base module we are currently on
		this.info.setDefault(stack).moveTo(modifying, Math.max(0, modifyingLen - 2));
		NBTTagList baseTag = this.info.tag;
		this.baseType = this.info.type;
		
		if(this.nonPrimarySelected())
			this.info.moveTo(modifying[modifyingLen - 2], modifying[modifyingLen - 1]);
		
		if(Key.CO.down())
		{
			// TODO:Press confirm to install or update state, cancel to remove current module
			
			switch(this.mode)
			{
			case PAINTJOB:
				// TODO: handle paintjob
				break;
			
			default:
				if(modifyingLen == 0) break;
				
				if(
					(this.upKeyDown || this.downKeyDown)
					&& modifying[modifyingLen - 1] < 0
				) {
					int stepper = this.upKeyDown ? -1 : 1;
					InventoryPlayer inv = getPlayer().inventory;
					int size = inv.getSizeInventory();
					int next = this.previewIndex + stepper;
					
					for(
						next = next < -1 ? size - 1 : next;
						next >= 0 && next < size;
						next += stepper
					) {
						ItemStack s = inv.getStackInSlot(next);
						if(
							s.getItem() instanceof ItemModular
							&& this.baseType.slots[modifying[modifyingLen - 2]].isAllowed(
								((ItemModular)s.getItem()).getType()
							)
						) {
							this.previewIndex = next;
							this.previewStack = s;
							
							// Set indicator that we have find a module to attach
							size = -1;
							break;
						}
					}
					
					if(size > 0)
					{
						this.previewIndex = -1;
						this.previewStack = ItemStack.EMPTY;
					}
					
					this.offset = 0;
				}
				else if(
					(this.leftKeyDown || this.rightKeyDown)
					&& (modifying[modifyingLen - 1] >= 0 || this.previewStack != ItemStack.EMPTY)
				) {
					switch(this.mode)
					{
					case ON_SLOT:
						int bound = this.baseType.slots[modifying[modifyingLen - 2]].maxStep;
						this.step = (
							this.leftKeyDown
							? (this.step > 0 ? this.step : bound) - 1
							: ++this.step < bound ? this.step : 0
						);
						break;
					default: // ON_MODULE
						bound = this.info.type.offsets.length;
						this.offset = (
							this.leftKeyDown
							? (this.offset > 0 ? this.offset : bound) - 1
							: ++this.offset < bound ? this.offset : 0
						);
					}
				}
				else if(this.confirmKeyDown)
				{
					// TODO: Update position and offset
					if(modifying[modifyingLen - 1] >= 0)
					{
						
					}
					else if(this.previewIndex != -1)
					{
//						FMUMClient.netHandler.sendToServer(
//							
//						);
						
						// Clear preview selected after install
						this.clearPreviewSelected();
						this.offset = 0;
					}
				}
				else if(this.cancelKeyDown && modifying[modifyingLen - 1] >= 0)
				{
//					FMUMClient.netHandler.sendToServer(
//						
//					);
					
					// Clear selected after remove(keep step)
					modifying[modifyingLen - 1] = -1;
					int oriStep = this.step;
					this.updateModifying(stack);
					this.step = oriStep;
				}
			}
		}
		
		/// Co-key is not down ///
		else
		{
			// Switch slot, switch module and quit layer require layer > 0
			if(modifyingLen > 0)
			{
				if(this.cancelKeyDown)
				{
					this.modifyingLen -= 2;
					this.updateModifying(stack);
				}
				else if(this.upKeyDown || this.downKeyDown)
				{
					int slots = this.baseType.slots.length;
					int i = modifying[modifyingLen - 2];
					modifying[modifyingLen - 1] = (byte)(
						((NBTTagList)baseTag.get(
							(
								modifying[modifyingLen - 2] = (byte)(
									this.upKeyDown
									? (i > 0 ? i : slots) - 1
									: ++i < slots ? i : 0
								)
							) + 1
						)).tagCount() > 0
						? 0
						: -1
					);
					this.updateModifying(stack);
				}
				else if(this.leftKeyDown || this.rightKeyDown)
				{
					int count = ((NBTTagList)baseTag.get(
						modifying[modifyingLen - 2] + 1
					)).tagCount();
					int i = modifying[modifyingLen - 1];
					
					modifying[modifyingLen - 1] = (byte)(
						this.leftKeyDown
						? (i >= 0 ? i : count) - 1
						: ++i < count ? i : -1
					);
					this.updateModifying(stack);
				}
			}
			
			// Enter next layer if gun is selected or an module is selected
			if(
				this.confirmKeyDown
				&& this.selected()
				&& this.info.type.slots.length > 0
				&& modifyingLen < modifying.length
			) {
				this.modifyingLen += 2;
				modifying[modifyingLen] = 0;
				
				// If has module installed on first slot, then select it 
				modifying[modifyingLen + 1] = (byte)(
					((NBTTagList)this.info.tag.get(1)).tagCount() > 0 ? 0 : -1
				);
				this.updateModifying(stack);
			}
		}
		
		// One tick is done, clear key input
		this.clearInputStates();
		return false;
	}
	
	/**
	 * @note Update {@link #modifying} and {@link #modifyingLen} before calling this method
	 * @note Calling this will dirty {@link #info}
	 * @param stack Gun stack
	 */
	private void updateModifying(ItemStack stack)
	{
		this.clearPreviewSelected();
		
		// If it is the primary base selected
		if(this.modifyingLen == 0)
		{
			this.streamIndex = 0;
			this.step = 0;
			this.offset = 0;
			this.dam = stack.getItemDamage();
			return;
		}
		
		// Set index based on whether non selected or not
		boolean flag = this.modifying[this.modifyingLen - 1] < 0;
		this.streamIndex = this.info.setDefault(stack).type.getStreamIndex(
			TagModular.getTag(stack),
			this.modifying,
			this.modifyingLen - (flag ? 2 : 0),
			0
		);
		
		// Also update base type cause it will be used in rendering during next tick
		this.baseType = this.info.moveTo(this.modifying, this.modifyingLen - 2).type;
		if(flag)
		{
			this.step
				= this.offset
				= this.dam
				= 0;
			return;
		}
		
		int[] states = TagModular.getStates(
			this.info.moveTo(
				this.modifying[this.modifyingLen - 2],
				this.modifying[this.modifyingLen - 1]
			).tag
		);
		this.step = TagModular.getStep(states);
		this.offset = TagModular.getOffset(states);
		this.dam = TagModular.getDam(states);
	}
	
	private void clearPreviewSelected()
	{
		this.previewIndex = -1;
		this.previewStack = ItemStack.EMPTY;
	}
	
	private void clearInputStates()
	{
		this.upKeyDown
			= this.downKeyDown
			= this.leftKeyDown
			= this.rightKeyDown
			= this.confirmKeyDown
			= this.cancelKeyDown
			= this.toggleKeyDown
			= false;
	}
	
	public enum ModifyMode
	{
		ON_SLOT,
		ON_MODULE,
		PAINTJOB;
	}
}
