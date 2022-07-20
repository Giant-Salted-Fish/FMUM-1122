package com.fmum.client.modular;

import com.fmum.client.Operation;
import com.fmum.client.input.InputHandler;
import com.fmum.client.FMUMClient;
import com.fmum.client.OpProgressive;
import com.fmum.common.FMUM;
import com.fmum.common.item.MetaHostItem;
import com.fmum.common.item.MetaItem;
import com.fmum.common.module.InfoModule;
import com.fmum.common.module.MetaModular;
import com.fmum.common.module.ModuleSlot;
import com.fmum.common.paintjob.MetaPaintable;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public final class OpModify extends OpProgressive
{
	public static final OpModify INSTANCE = new OpModify();
	
	public byte[] loc = null;
	public int locLen = 0;
	
	public ModifyMode mode = ModifyMode.SLOT;
	
//	public int streamIndex = 0;
	
	public int step = 0;
	public int offset = 0;
	public int dam = 0;
	
	public MetaModular baseMeta = null;
	
	public ItemStack previewStack = null;
	public MetaModular previewMeta = null;
	
	public boolean valid = false;
	
	public boolean
		upKeyDown = false,
		downKeyDown = false,
		leftKeyDown = false,
		rightKeyDown = false,
		confirmKeyDown = false,
		cancelKeyDown = false,
		toggleKeyDown = false;
	
	private NBTTagList baseTag = null;
	
	private int previewInvSlot = 0;
	
	private final InfoModule info = InfoModule.locate();
	
	private OpModify() { }
	
	@Override
	public OpModify launch( ItemStack stack, MetaItem meta )
	{
		super.launch( stack, meta );
		
		this.progressor = 0.1D;
		
		// Make sure #loc is valid for use
		if( this.loc == null || this.loc.length != FMUM.MOD.maxLocLen )
			this.loc = new byte[ FMUM.MOD.maxLocLen ];
		this.locLen = 0;
		
		this.mode = ModifyMode.SLOT;
		this.setupWith( stack, ( MetaModular ) meta );
		
		this.clearInputStates();
		return this;
	}
	
	@Override
	public Operation tick( ItemStack itemStack, MetaItem metaItem )
	{
		if( super.tick( itemStack, metaItem ) != NONE )
			return this.prevProgress < 0D && this.progressor < 0D ? NONE : this;
		
		// Handle toggle mode
		if( this.toggleKeyDown )
		{
			ModifyMode[] modes = ModifyMode.values();
			this.mode = modes[ ( this.mode.ordinal() + 1 ) % modes.length ];
			FMUMClient.addPromptMsg( I18n.format( this.mode.notifyMessage ) );
		}
		
		if( InputHandler.CO.down )
		{
			// Loop preview module to install
			if( ( this.upKeyDown || this.downKeyDown ) && !this.moduleSelected() )
			{
				// Get selected slot
				final int selectedSlot = this.selectedSlot();
				final ModuleSlot moduleSlot = this.baseMeta.slot( selectedSlot );
				
				// Get player inventory
				final InventoryPlayer inv = this.getPlayer().inventory;
				final int invSize = inv.getSizeInventory();
				int stepper = this.upKeyDown ? -1 : 1;
				
				int next = this.previewInvSlot + stepper;
				for(
					next = next < -1 ? invSize - 1 : next;
					next >= 0 && next < invSize;
					next += stepper
				) {
					ItemStack stack = inv.getStackInSlot( next );
					MetaItem meta = MetaHostItem.getMeta( stack );
					if(
						meta instanceof MetaModular
						&& moduleSlot.isAllowed( ( MetaModular ) meta )
					) {
						this.previewInvSlot = next;
						this.previewStack = stack;
						this.previewMeta = ( MetaModular ) meta;
						
						// Set a flag to tell outside that we have find a module to install
						stepper = 0;
						break;
					}
				}
				
				if( stepper != 0 )
				{
					this.previewInvSlot = -1;
					this.previewStack = null;
					this.previewMeta = null;
				}
				else
				{
					// Find the valid module, check if it is valid in slot
					this.valid = this.baseMeta.installedInSlot(
						this.baseTag,
						selectedSlot
					) <= Math.min(
						FMUM.MOD.maxCanInstall,
						moduleSlot.maxCanInstall()
					);
					// && !hitbox.conflict( hitbox );
					// TODO: check hit box
				}
				
				this.offset = 0;
				this.dam = 0;
			}
			
			// Special functionality based on modify mode
			else if( this.leftKeyDown || this.rightKeyDown )
			{
				switch( this.mode )
				{
				case PAINTJOB:
					MetaModular meta = this.moduleSelected() ? this.info.meta : this.previewMeta;
					if( meta instanceof MetaPaintable )
					{
						final MetaPaintable paintable = ( MetaPaintable ) meta;
						// TODO: check if player can offer the paintjob
						
						final int bound = paintable.numTextures();
						final int stepper = this.leftKeyDown ? bound - 1 : 1;
						this.dam = ( this.dam + stepper ) % bound;
					}
					break;
					
				case SLOT:
					int bound = this.baseMeta.slot( this.selectedSlot() ).maxPosStep() + 1;
					int stepper = this.leftKeyDown ? bound - 1 : 1;
					this.step = ( this.step + stepper ) % bound;
					break;
					
				case MODULE:
					bound = (
						this.previewInvSlot >= 0
						? this.previewMeta.numOffsets()
						: this.installedSelected() ? this.info.meta.numOffsets() : 1
					);
					stepper = this.leftKeyDown ? bound - 1 : 1;
					this.offset = ( this.offset + stepper ) % bound;
				}
			}
			else if( this.confirmKeyDown )
			{
				// Update position, offset and damage
				// FIXME
			}
			else if( this.cancelKeyDown && this.installedSelected() )
			{
				// FIXME
			}
		}
		
		/// Co-key is not down ///
		else
		{
			// Switch slot, switch module and quit operation require layer > 0
			if( locLen > 0 )
			{
				if( this.cancelKeyDown )
				{
					this.locLen -= 2;
					
				}
			}
		}
		
		// One tick is done, clear key input
		this.clearInputStates();
		return this;
	}
	
	public int selectedSlot() { return this.loc[ this.locLen - 2 ]; }
	
	public int selectedModule() { return this.loc[ this.locLen - 1 ]; }
	
	/**
	 * @return {@code true} if the primary base module is selected
	 */
	public boolean primarySelected() { return this.locLen == 0; }
	
	/**
	 * @return {@code true} if any module is in selected state including primary base module itself
	 */
	public boolean moduleSelected() {
		return this.locLen == 0 || this.loc[ this.locLen - 1 ] != -1;
	}
	
	/**
	 * @return {@code true} if any module installed on primary base is in selected state
	 */
	public boolean installedSelected() {
		return this.locLen > 0 && this.loc[ this.locLen - 1 ] != -1;
	}
	
//	public boolean isPaintMode() { return this.mode == ModifyMode.PAINTJOB; }
	
	/**
	 * Clear preview select, reset {@link #valid} to {@code true}, initialize {@link #baseTag},
	 * {@link #baseMeta}, {@link #step}, {@link #offset} and {@link #dam} based on given module item
	 * stack and meta
	 * 
	 * @param stack Item stack of the module
	 * @param meta Meta of the module
	 */
	private void setupWith( ItemStack stack, MetaModular meta )
	{
		// TODO: Check if this is needed
		this.clearPreviewSelected();
		this.valid = true;
		
		// Move to base first and record base tag and base meta
		this.info.reset(
			meta.tag( stack.getTagCompound() ),
			meta
		).moveTo( this.loc, Math.max( 0, this.locLen - 2 ) );
		
		this.baseTag = this.info.tag;
		this.baseMeta = this.info.meta;
		
		// Move to specified module if selected
		if( this.installedSelected() )
			this.info.moveTo( this.selectedSlot(), this.selectedModule() );
		
		if( this.moduleSelected() )
		{
			// Retrieve and set step, offset and damage
			final NBTTagList moduleTag = this.info.tag;
			final MetaModular moduleMeta = this.info.meta;
			this.step = moduleMeta.step( moduleTag );
			this.offset = moduleMeta.offset( moduleTag );
			this.dam = moduleMeta.dam( moduleTag );
//			this.streamIndex = ; TODO check if should remove
		}
		else
		{
			this.step = 0;
			this.offset = 0;
			this.dam = 0;
		}
	}
	
	private void clearPreviewSelected()
	{
		this.previewInvSlot = -1;
		this.previewStack = null;
		this.previewMeta = null;
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
		SLOT( "slot" ),
		MODULE( "module" ),
		PAINTJOB( "paintjob" );
		
		public final String notifyMessage;
		
		private ModifyMode( String translationKey ) {
			this.notifyMessage = "msg.modifymode" + translationKey;
		}
	}
}
