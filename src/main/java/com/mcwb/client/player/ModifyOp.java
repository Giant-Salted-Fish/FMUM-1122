package com.mcwb.client.player;

import com.mcwb.client.IAutowirePlayerChat;
import com.mcwb.client.MCWBClient;
import com.mcwb.client.input.IKeyBind;
import com.mcwb.client.input.InputHandler;
import com.mcwb.client.input.Key;
import com.mcwb.common.item.IContextedItem;
import com.mcwb.common.item.IItemMeta;
import com.mcwb.common.item.IItemMetaHost;
import com.mcwb.common.item.ItemContext;
import com.mcwb.common.modify.IContextedModifiable;
import com.mcwb.common.modify.IContextedModifiable.ModifyState;
import com.mcwb.common.modify.IModifiableMeta;
import com.mcwb.common.modify.IModuleSlot;
import com.mcwb.common.modify.ModifiableContext;
import com.mcwb.common.modify.ModifiableMeta;
import com.mcwb.common.network.PacketModify;
import com.mcwb.common.player.IOperation;
import com.mcwb.common.player.Operation;
import com.mcwb.common.player.TogglableOperation;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class ModifyOp< T extends IContextedItem & IContextedModifiable >
	extends TogglableOperation< T > implements IAutowirePlayerChat
{
	protected static final Runnable INPUT_HANDLER = () -> { };
	
	public float refPlayerRotYaw = 0F;
	
	protected ModifyMode mode = ModifyMode.SLOT;
	
	protected byte[] loc = null;
	protected int locLen = 0;
	
	protected int oriStep = 0;
	protected int oriOffset = 0;
	protected int oriPaintjob = 0;
	
	protected int curStep = 0;
	protected int curOffset = 0;
	protected int curPaintjob = 0;
	
	protected boolean conflict = false;
	
	/**
	 * A copy of {@link #contexted}. Preview changes will be performed on this context.
	 */
	protected T primary = null;
	
	protected IContextedModifiable selected = null;
	
	protected int previewInvSlot = 0;
	protected IContextedModifiable preview = null;
	
	protected Runnable inputHandler = INPUT_HANDLER;
	
	public ModifyOp() { super( null, null, () -> 0.1F, () -> -0.1F ); }
	
	public void handleKeyInput( IKeyBind key )
	{
		final String kName = key.name();
		if( Key.SELECT_TOGGLE.equals( kName ) )
		{
			this.inputHandler = () -> {
				// Handle toggle model
				final ModifyMode[] modes = ModifyMode.values();
				this.mode = modes[ ( this.mode.ordinal() + 1 ) % modes.length ];
				this.sendPlayerPrompt( I18n.format( this.mode.notifyMsg ) );
				
				// Update state for selected module
				if( this.preview != null )
					this.preview.$modifyState( this.modifyState() );
				else if( this.index() != -1 && this.locLen > 0 )
					this.selected.$modifyState( this.modifyState() );
			};
		}
		else if( InputHandler.CO.down ) switch( kName )
		{
		case Key.SELECT_UP:
		case Key.SELECT_DOWN:
			// Try to loop preview module
			if( this.index() != -1 ) break;
			this.inputHandler = () -> {
				// Get selected slot and player inventory
				final IModuleSlot slot = this.selected.meta().getSlot( this.slot() );
				final InventoryPlayer inv = this.player.inventory;
				final int size = inv.getSizeInventory() + 1;
				final int incr = key == InputHandler.SELECT_UP ? size : 2;
				
				while( ( this.previewInvSlot = ( this.previewInvSlot + incr ) % size - 1 ) != -1 )
				{
					final ItemStack stack = inv.getStackInSlot( this.previewInvSlot );
					final IItemMeta meta = IItemMetaHost.getMeta( stack );
					if(
						meta instanceof IModifiableMeta
						&& slot.isAllowed( ( IModifiableMeta ) meta )
					) {
						// Get a copy of the preview module context
						final IModifiableMeta modMeta = ( ( IModifiableMeta ) meta );
						this.preview = modMeta.newContexted( new NBTTagCompound() ); // TODO: a static tag maybe
						this.preview.deserializeNBT(
							modMeta.getContexted( stack ).serializeNBT().copy()
						);
						
						this.preview.$step( this.curStep );
						this.preview.$offset( 0 );
						this.curOffset = 0;
						
						// Step and offset will always be set when install a new module, but \
						// paintjob would not be update if original paintjob does not change. \
						// Hence #oriPaintjob needs to be setup but those two do not.
						this.curPaintjob
							= this.oriPaintjob
							= this.preview.paintjob();
						break;
					}
				}
				
				// If we have found a valid module for preview
				if( this.previewInvSlot != -1 )
				{
					final int iSlot = this.slot();
					
					// Copy tag to reset the state
					this.setupSelectedContext( false );
					
					// Check if this preview module is valid to install
					this.conflict = this.selected.getInstalledCount( iSlot )
						>= Math.min( MCWBClient.maxSlotCapacity, slot.capacity() );
					// TODO: check hit box
					
					// Set modify state
					this.preview.$modifyState( this.modifyState() );
					
					// Append it to the base
					this.selected.install( iSlot, this.preview );
				}
				
				// Setup indicator otherwise
				else this.setupSelectedContext( true );
			};
			break;
			
		case Key.SELECT_LEFT:
		case Key.SELECT_RIGHT:
			// Change step|offset|paintjob
			this.inputHandler = () -> {
				final IContextedModifiable target =
					this.index() != -1 ? this.selected : this.preview;
				
				if( target == null )
					; // TODO: this.sendPlayerPrompt( I18n.format( "mcwb.msg.module_select_required" ) );
				else if( this.mode == ModifyMode.PAINTJOB )
				{
					final int bound = target.meta().paintjobCount();
					final int incr = key == InputHandler.SELECT_LEFT ? bound - 1 : 1;
					this.curPaintjob = ( this.curPaintjob + incr ) % bound;
					target.$paintjob( this.curPaintjob );
				}
				else
				{
					if( this.mode == ModifyMode.MODULE )
					{
						final int bound = target.meta().offsetCount();
						final int incr = key == InputHandler.SELECT_LEFT ? bound - 1 : 1;
						this.curOffset = ( this.curOffset + incr ) % bound;
						target.$offset( this.curOffset );
					}
					
					// Not available for primary base
					else if( this.locLen > 0 )
					{
						final int bound = target.base().meta().getSlot( this.slot() ).maxStep();
						
						// Move upon player rotation to satisfy human intuition
						final float rot = ( this.player.rotationYaw % 360F + 360F ) % 360F;
						final int incr = key == InputHandler.SELECT_LEFT ^ rot < 180F ? 1 : bound;
						this.curStep = ( this.curStep + incr ) % ( bound + 1 );
						target.$step( this.curStep );
					}
					
					// TODO: Check whether the new position will cause conflict or not
					this.conflict = false;
				}
			};
			break;
			
		case Key.SELECT_CONFIRM:
			this.inputHandler = () -> {
				if( this.index() != -1 )
				{
					// Update step and offset if has changed and has not conflict
					if(
						!this.conflict
						&& ( this.curStep != this.oriStep || this.curOffset != this.oriOffset )
					) {
						MCWBClient.NET.sendToServer(
							new PacketModify( this.curStep, this.curOffset, this.loc, this.locLen )
						);
						
						// Do not forget to update original step and offset
						this.oriStep = this.curStep;
						this.oriOffset = this.curOffset;
					}
					
					// Check paintjob update
					if( this.curPaintjob != this.oriPaintjob ) // TODO: validate material
					{
						MCWBClient.NET.sendToServer(
							new PacketModify( this.curPaintjob, this.loc, this.locLen )
						);
						this.oriPaintjob = this.curPaintjob;
					}
				}
				
				// Install a new module
				else if( this.preview != null && !this.conflict )
				{
					MCWBClient.NET.sendToServer(
						new PacketModify(
							this.previewInvSlot,
							this.curStep,
							this.curOffset,
							this.loc,
							this.locLen
						)
					);
					
					// Update paintjob if also has changed
					if( this.curPaintjob != this.oriPaintjob )
					{
						// The module has not been truly installed yet, so make a prediction of \
						// its future install index (do not forget there is a preview tag installed)
						this.loc[ this.locLen - 1 ] = ( byte )
							( this.selected.getInstalledCount( this.slot() ) - 1 );
						MCWBClient.NET.sendToServer(
							new PacketModify( this.curPaintjob, this.loc, this.locLen )
						);
						
						// Do not forget to set it back
						this.loc[ this.locLen - 1 ] = -1;
					}
					
					// Clear preview selected after install
					this.setupSelectedContext( true );
				}
			};
			break;
			
		case Key.SELECT_CANCEL:
			this.inputHandler = () -> {
				// Only can remove when it is selected and is not primary base
				if( this.index() == -1 || this.locLen == 0 )
				{
					MCWBClient.NET.sendToServer( new PacketModify( this.loc, this.locLen ) );
					
					// Set current index to -1 as the selected module will be invalid soon
					this.loc[ this.locLen - 1 ] = -1;
					this.setupSelectedContext( true );
				}
			};
			break;
		}
		
		// Co-key is not down
		else if( key == InputHandler.SELECT_CONFIRM )
		{
			this.inputHandler = () -> {
				// Enter next layer if has module selected
				if( this.index() == -1 ) return;
				
				if( this.selected.meta().slotCount() == 0 )
					; // TODO: mcwb.msg.module_has_no_slot
				else if( this.locLen == this.loc.length )
					; // TODO: mcwb.msg.reach_max_modify_layer
				else
				{
					// Set index to 0 if has module installed in new slot, or -1 otherwise
					// Notice that when current index != -1, #previewCtx must be null
					int count = this.selected.getInstalledCount( 0 );
					
					this.loc[ this.locLen ] = 0;
					this.loc[ this.locLen - 1 ] = ( byte ) ( Math.min( 1, count ) - 1 );
					this.locLen += 2;
					
					this.setupSelectedContext( true );
				}
			};
		}
		
		// Switch slot, switch module and quit layer require current layer > 0
		else if( this.locLen > 0 ) switch( kName )
		{
		case Key.SELECT_CANCEL:
			// Quit layer
			this.inputHandler = () -> {
				this.locLen -= 2;
				this.setupSelectedContext( true );
			};
			break;
			
		default:
			final IContextedModifiable base = this.index() != -1
				? this.selected.base() : this.selected;
			
			switch( kName )
			{
			case Key.SELECT_UP:
			case Key.SELECT_DOWN:
				// Switch slot
				this.inputHandler = () -> {
					if( base.meta().slotCount() > 1 )
					{
						final int bound = base.meta().slotCount();
						final int incr = key == InputHandler.SELECT_UP ? bound - 1 : 1;
						final int next = ( this.slot() + incr ) % bound;
						final int count = base.getInstalledCount( next );
						
						this.loc[ this.locLen - 2 ] = ( byte ) next;
						this.loc[ this.locLen - 1 ] = ( byte ) ( Math.min( 1, count ) - 1 );
						
						this.setupSelectedContext( true );
					}
				};
				break;
				
			case Key.SELECT_LEFT:
			case Key.SELECT_RIGHT:
				// Switch selected module
				this.inputHandler = () -> {
					// Here we shift it before do mod so -1 is included
					final int bound = base.getInstalledCount( this.slot() )
						+ Math.max( 0, -this.previewInvSlot + 1 );
					final int incr = key == InputHandler.SELECT_LEFT ? bound : 2;
					final int idx = ( this.index() | incr ) % bound - 1;
					this.loc[ this.locLen - 1 ] = ( byte ) idx;
					
					this.setupSelectedContext( true );
				};
				break;
			}
		}
	}
	
	public IContextedItem getRenderDelegate( IContextedItem original ) {
		return this.primary != null ? this.primary : original;
	}
	
	@Override
	public IOperation launch( IOperation oldOp )
	{
		this.mode = ModifyMode.SLOT;
		
		// Initialize #loc if has not
		if( this.loc == null || this.loc.length < MCWBClient.modifyLocLen )
			this.loc = new byte[ MCWBClient.modifyLocLen ];
		this.locLen = 0;
		
		this.setupSelectedContext( true );
		this.refPlayerRotYaw = this.player.rotationYaw;
		
		this.clearInputState();
		return super.launch( oldOp );
	}
	
	@Override
	public IOperation toggle()
	{
		// Only update player reference yaw rotation when this operation is fully launched
		if( this.prevProgress == 1F )
			this.refPlayerRotYaw = this.player.rotationYaw;
		
		this.locLen = 0;
		this.setupSelectedContext( true );
		return super.toggle();
	}
	
	@Override
	public IOperation tick()
	{
		if( super.tick() == NONE )
			return this.terminate();
		
		// Do not do modification if operation has not been fully launched
		else if( this.prevProgress != 1F )
			return this;
		
		this.inputHandler.run();
		return this;
	}
	
	@Override
	public IOperation terminate()
	{
		this.primary = null;
		this.clearProgress();
		this.clearPreview();
		return NONE;
	}
	
	@Override
	@SuppressWarnings( "unchecked" )
	public IOperation onHoldingStackChange( IContextedItem newItem )
	{
		// We basically has not way to ensure that this change is triggered by something like fire \
		// fire mode switch rather than swap hand. So in compromise we just check whether current \
		// #loc is compatible with the new context or not. If it is then just continue.
		// NOTE:
		//     We do not check meta as it could change upon modification. For example, installing \
		//     special attachment could actually change the meta of primary module base.
		IContextedModifiable newMod = ( IContextedModifiable ) newItem; // Meta does not change
		IContextedModifiable prevMod = this.primary;
		for( int i = 0; i < this.locLen; i += 2 )
		{
			final int slot = 0xFF & this.loc[ i ];
			final int idx = ( ( 0xFF & this.loc[ i + 1 ] ) + 1 ) % 256 - 1; // Map 255 to -1
			
			// This will also work if is last layer and preview selected
			if( slot >= newMod.meta().slotCount() || idx >= newMod.getInstalledCount( slot ) )
				return this.terminate();
			else if( i + 2 < this.locLen )
			{
				newMod = newMod.getInstalled( slot, idx );
				prevMod = prevMod.getInstalled( slot, idx );
			}
		}
		
		// Do be a valid primary for current state, then just update and continue
		this.contexted = ( T ) newItem;
		this.setupSelectedContext( true );
		return this;
	}
	
	/**
	 * @return
	 *     Unsigned value of {@link #loc}{@code [}{@link #locLen}{@code -2]}. {@code 0} if
	 *     {@link #locLen} is {@code 0}.
	 */
	protected int slot() {
		return 0xFF & ( this.locLen > 0 ? this.loc[ this.locLen - 2 ] : 0 );
	}
	
	/**
	 * @return
	 *     Unsigned value of {@link #loc}{@code [}{@link #locLen}{@code -1]} if it is not
	 *     {@code -1}. {@code 0} if {@link #locLen} is {@code 0}.
	 */
	protected int index()
	{
		final byte idx = this.locLen > 0 ? this.loc[ this.locLen - 1 ] : 0;
		return idx != -1 ? 0xFF & idx : -1;
	}
	
	@SuppressWarnings( "unchecked" )
	protected void setupSelectedContext( boolean clearPreview )
	{
		// Move to base first
		this.primary = ( T ) this.context.copy();
		this.selectedCtx = this.primaryCtx.getInstalled( this.loc, Math.max( 0, this.locLen - 2 ) );
		
		// Check if is installed selected
		if( this.index() != -1 )
		{
			// Move to selected module if not primary selected
			if( this.locLen > 0 )
			{
				this.selectedCtx = this.selectedCtx.getInstalled( this.slot(), this.index() );
				
				// Module will only be highlighted when it is not primary base
				this.selectedCtx.$modifyState( this.modifyState() );
			}
			
			if( clearPreview )
			{
				this.clearPreview();
				
				// TODO: check if it is needed to ensure this will only execute if has module \
				// selected. This requires to check when clearPreview is true
				this.curStep
					= this.oriStep
					= this.selectedCtx.step();
				this.curOffset
					= this.oriOffset
					= this.selectedCtx.offset();
				this.curPaintjob
					= this.oriPaintjob
					= this.selectedCtx.paintjob();
			}
			else
			{
				this.selectedCtx.$step( this.curStep );
				this.selectedCtx.$offset( this.curOffset );
				this.selectedCtx.$paintjob( this.curPaintjob );
			}
		}
		else if( clearPreview )
		{
			this.clearPreview();
			
			// TODO: setup indicator
		}
	}
	
	protected ModifyState modifyState()
	{
		return this.mode == ModifyMode.PAINTJOB ? ModifyState.NOT_SELECTED
			: this.conflict ? ModifyState.SELECTED_CONFLICT : ModifyState.SELECTED_OK;
	}
	
	protected void clearPreview()
	{
		this.previewInvSlot = -1;
		this.previewCtx = null;
		this.conflict = false;
	}
	
	protected void clearInputState()
	{
		this.selectedToggle
			= this.selectedUp
			= this.selectedDown
			= this.selectedLeft
			= this.selectedRight
			= this.selectedConfirm
			= this.selectedCancel
			= false;
	}
	
	protected enum ModifyMode
	{
		SLOT,
		MODULE,
		PAINTJOB;
		
		public final String notifyMsg = "mcwb.msg.modify_mode_" + this.name().toLowerCase();
	}
}
