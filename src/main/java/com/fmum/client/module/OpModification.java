package com.fmum.client.module;

import com.fmum.client.FMUMClient;
import com.fmum.client.KeyManager.Key;
import com.fmum.client.OperationProgressive;
import com.fmum.common.CommonProxy;
import com.fmum.common.module.InfoModule;
import com.fmum.common.module.ItemModular;
import com.fmum.common.module.TagModular;
import com.fmum.common.module.TypeModular;
import com.fmum.common.network.PacketModuleInstall;
import com.fmum.common.network.PacketModuleRemove;
import com.fmum.common.network.PacketModuleUpdate;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class OpModification extends OperationProgressive
{
	public static final OpModification INSTANCE = new OpModification();
	
	public byte[] loc = null;
	public int locLen = 0;
	
	public ModifyMode mode = ModifyMode.ON_SLOT;
	
	public int streamIndex = 0;
	
	public int step = 0;
	public int offset = 0;
	public int dam = 0;
	
	public TypeModular baseType = null;
	
	public int previewInvSlot = 0;
	public ItemStack previewStack = ItemStack.EMPTY;
	
	public boolean valid = false;
	
	public boolean
		upKeyDown = false,
		downKeyDown = false,
		leftKeyDown = false,
		rightKeyDown = false,
		confirmKeyDown = false,
		cancelKeyDown = false,
		toggleKeyDown = false;
	
	private final InfoModule info = InfoModule.get();
	
	public void onConfigSync() { this.loc = new byte[CommonProxy.maxLocLen]; }
	
	public boolean isPaintMode() { return this.mode == ModifyMode.PAINTJOB; }
	
	public boolean selected() { return this.locLen == 0 || this.loc[this.locLen - 1] != -1; }
	
	public boolean nonPrimarySelected() {
		return this.locLen > 0 && this.loc[this.locLen - 1] != -1;
	}
	
	@Override
	protected void launch(ItemStack stack)
	{
		this.prevProgress
			= this.progress
			= 0D;
		this.progressor = 0.1D;
		this.locLen = 0;
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
		final byte[] loc = this.loc;
		final int locLen = this.locLen;
		
		// Get base module we are currently on
		this.info.setDefault(stack).moveTo(loc, Math.max(0, locLen - 2));
		final NBTTagList baseTag = this.info.tag;
		this.baseType = this.info.type;
		
		if(this.nonPrimarySelected())
			this.info.moveTo(0xFF & loc[locLen - 2], 0xFF & loc[locLen - 1]);
		
		// Handle toggle mode
		if(this.toggleKeyDown)
		{
			switch(this.mode)
			{
			case ON_SLOT: this.mode = ModifyMode.ON_MODULE; break;
			case ON_MODULE: this.mode = ModifyMode.PAINTJOB; break;
			default: this.mode = ModifyMode.ON_SLOT;
			}
			FMUMClient.addPromptMsg(I18n.format(this.mode.notifyMessage));
		}
		
		if(Key.CO.down)
		{
			switch(this.mode)
			{
			case PAINTJOB:
				if(this.leftKeyDown || this.rightKeyDown)
				{
					int maxDam = this.info.type.paintjobs.size();
					this.dam = (this.dam + (this.leftKeyDown ? maxDam - 1 : 1)) % maxDam;
					break;
				}
			
			default:
				if(locLen == 0) break;
				
				if(
					(this.upKeyDown || this.downKeyDown)
					&& loc[locLen - 1] == -1
				) {
					int stepper = this.upKeyDown ? -1 : 1;
					InventoryPlayer inv = getPlayer().inventory;
					int size = inv.getSizeInventory();
					int next = this.previewInvSlot + stepper;
					
					for(
						next = next < -1 ? size - 1 : next;
						next >= 0 && next < size;
						next += stepper
					) {
						ItemStack s = inv.getStackInSlot(next);
						if(
							s.getItem() instanceof ItemModular
							&& this.baseType.slots[0xFF & loc[locLen - 2]].isAllowed(
								((ItemModular)s.getItem()).getType()
							)
						) {
							this.previewInvSlot = next;
							this.previewStack = s;
							
							// Set indicator that we have find a module to attach
							size = -1;
							break;
						}
					}
					
					if(size > 0)
					{
						this.previewInvSlot = -1;
						this.previewStack = ItemStack.EMPTY;
					}
					else
					{
						// Find one allowed module, check if it is valid
						this.valid = this.baseType.availableForInstall(baseTag, loc[locLen - 2]);
						
						// TODO: hitbox test
					}
					
					this.offset = 0;
				}
				else if(
					(this.leftKeyDown || this.rightKeyDown)
					&& (loc[locLen - 1] != -1 || this.previewStack != ItemStack.EMPTY)
				) {
					switch(this.mode)
					{
					case ON_SLOT:
						int bound = this.baseType.slots[0xFF & loc[locLen - 2]].maxStep + 1;
						this.step = (this.step + (this.leftKeyDown ? bound - 1 : 1)) % bound;
						break;
					default: // ON_MODULE
						bound = this.info.type.offsets.length;
						this.offset = (this.offset + (this.leftKeyDown ? bound - 1 : 1)) % bound;
					}
				}
				else if(this.confirmKeyDown)
				{
					// Update position, offset and damage
					if(loc[locLen - 1] != -1)
					{
						int[] states = TagModular.getStates(this.info.tag);
						
						if(TagModular.getStep(states) != this.step)
							FMUMClient.netHandler.sendToServer(
								new PacketModuleUpdate(
									loc,
									locLen,
									PacketModuleUpdate.STEP,
									this.step
								)
							);
						if(TagModular.getOffset(states) != this.offset)
							FMUMClient.netHandler.sendToServer(
								new PacketModuleUpdate(
									loc,
									locLen,
									PacketModuleUpdate.OFFSET,
									this.offset
								)
							);
						if(TagModular.getDam(states) != this.dam)
						{
							// TODO: validate if the materials are equipped
							FMUMClient.netHandler.sendToServer(
								new PacketModuleUpdate(
									loc,
									locLen,
									PacketModuleUpdate.PAINT,
									this.dam
								)
							);
						}
					}
					else if(this.previewInvSlot != -1)
					{
						FMUMClient.netHandler.sendToServer(
							new PacketModuleInstall(
								loc,
								locLen,
								this.previewInvSlot,
								this.step,
								this.offset
							)
						);
						
						// Clear preview selected after install
						this.clearPreviewSelected();
						this.offset = 0;
					}
				}
				else if(this.cancelKeyDown && loc[locLen - 1] != -1)
				{
					FMUMClient.netHandler.sendToServer(new PacketModuleRemove(loc, locLen));
					
					// Clear selected after remove(keep step)
					loc[locLen - 1] = -1;
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
			if(locLen > 0)
			{
				if(this.cancelKeyDown)
				{
					this.locLen -= 2;
					this.updateModifying(stack);
				}
				else if(this.upKeyDown || this.downKeyDown)
				{
					int slots = this.baseType.slots.length;
					int i = ((0xFF & loc[locLen - 2]) + (this.upKeyDown ? slots - 1 : 1)) % slots;
					loc[locLen - 2] = (byte)i;
					loc[locLen - 1] = (byte)(
						((NBTTagList)baseTag.get(1 + i)).tagCount() > 0 ? 0 : -1
					);
					this.updateModifying(stack);
				}
				else if(this.leftKeyDown || this.rightKeyDown)
				{
					int count = ((NBTTagList)baseTag.get(1 + (0xFF & loc[locLen - 2]))).tagCount();
					loc[locLen - 1] = (byte)(
						((0xFF & loc[locLen - 1] + 1)
							+ (this.leftKeyDown ? count : 1)) % (count + 1) - 1
					);
					this.updateModifying(stack);
				}
			}
			
			// Enter next layer if gun is selected or an module is selected
			if(
				this.confirmKeyDown
				&& this.selected()
				&& this.info.type.slots.length > 0
			) {
				if(locLen < loc.length)
				{
					this.locLen += 2;
					loc[locLen] = 0;
					
					// If has module installed on first slot, then select it 
					loc[locLen + 1] = (byte)(
						((NBTTagList)this.info.tag.get(1)).tagCount() > 0 ? 0 : -1
					);
					this.updateModifying(stack);
				}
				else FMUMClient.addChatMsg(
					I18n.format(
						"msg.fmum.reachmaxlayer",
						Integer.toString(CommonProxy.maxLocLen >>> 1)
					),
					I18n.format("msg.fmum.promptconfigmaxlayers")
				);
			}
		}
		
		// One tick is done, clear key input
		this.clearInputStates();
		return false;
	}
	
	/**
	 * @note Update {@link #loc} and {@link #locLen} before calling this method
	 * @note Calling this will dirty {@link #info}
	 * @param stack Gun stack
	 */
	private void updateModifying(ItemStack stack)
	{
		this.clearPreviewSelected();
		this.valid = true;
		
		// If it is the primary base selected
		if(this.locLen == 0)
		{
			this.streamIndex = 0;
			this.step = 0;
			this.offset = 0;
			this.dam = stack.getItemDamage();
			return;
		}
		
		// Set index based on whether non selected or not
		boolean flag = this.loc[this.locLen - 1] == -1;
		this.streamIndex = this.info.setDefault(stack).type.getStreamIndex(
			TagModular.getTag(stack),
			this.loc,
			this.locLen - (flag ? 2 : 0),
			0
		);
		
		// Also update base type cause it will be used in rendering during next tick
		this.baseType = this.info.moveTo(this.loc, this.locLen - 2).type;
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
				0xFF & this.loc[this.locLen - 2],
				0xFF & this.loc[this.locLen - 1]
			).tag
		);
		this.step = TagModular.getStep(states);
		this.offset = TagModular.getOffset(states);
		this.dam = TagModular.getDam(states);
	}
	
	private void clearPreviewSelected()
	{
		this.previewInvSlot = -1;
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
		ON_SLOT("onslot"),
		ON_MODULE("onmodule"),
		PAINTJOB("paintjob");
		
		public final String notifyMessage;
		
		private ModifyMode(String translationKey) {
			this.notifyMessage = "msg.fmum.modifymode" + translationKey;
		}
	}
}
