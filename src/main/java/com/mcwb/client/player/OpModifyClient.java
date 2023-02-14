package com.mcwb.client.player;

import com.mcwb.client.IAutowirePlayerChat;
import com.mcwb.client.MCWBClient;
import com.mcwb.client.input.IKeyBind;
import com.mcwb.client.input.InputHandler;
import com.mcwb.client.input.Key;
import com.mcwb.common.IAutowirePacketHandler;
import com.mcwb.common.item.IItem;
import com.mcwb.common.item.IItemType;
import com.mcwb.common.item.IItemTypeHost;
import com.mcwb.common.modify.IModifiable;
import com.mcwb.common.modify.IModifiable.ModifyState;
import com.mcwb.common.modify.IModifiableType;
import com.mcwb.common.modify.ModifyPredication;
import com.mcwb.common.network.PacketModify;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.OperationController;
import com.mcwb.common.operation.TogglableOperation;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class OpModifyClient extends TogglableOperation< IModifiable >
	implements IAutowirePacketHandler, IAutowirePlayerChat
{
	protected static final Runnable NO_TASK = () -> { };
	
	public float refPlayerRotYaw = 0F;
	
	protected ModifyMode mode = ModifyMode.SLOT;
	
	protected byte[] loc;
	protected int locLen;
	
	protected ItemStack copiedStack;
	
	/**
	 * None null. Should be the currently selected module or the preview module or the indicator.
	 */
	protected IModifiable selected = null;
	
	/**
	 * {@code -1} if no preview selected
	 */
	protected int previewInvSlot;
	
	protected int curStep;
	protected int oriStep;
	
	protected int curOffset;
	protected int oriOffset;
	
	protected int curPaintjob;
	protected int oriPaintjob;
	
	protected ModifyPredication previewState = ModifyPredication.NO_PREVIEW;
	protected ModifyPredication positionState = ModifyPredication.OK;
	
	protected Runnable inputHandler = NO_TASK;
	
	public OpModifyClient() {
		super( null, null, new OperationController( 0.1F ), new OperationController( -0.1F ) );
	}
	
	public OpModifyClient reset( IModifiable primary )
	{
		this.player = MCWBClient.MC.player;
		this.contexted = primary;
		this.copiedStack = this.player.inventory.getCurrentItem().copy();
		return this;
	}
	
	public void handleKeyInput( IKeyBind key )
	{
		final String keyName = key.name();
		if( Key.SELECT_TOGGLE.equals( keyName ) ) // Change modify mode
		{
			this.inputHandler = () -> {
				final ModifyMode[] modes = ModifyMode.values();
				this.mode = modes[ ( this.mode.ordinal() + 1 ) % modes.length ];
				this.sendPlayerPrompt( I18n.format( this.mode.notifyMsg ) );
				
				this.selected.$modifyState( this.modifyState() );
			};
		}
		else if( InputHandler.CO.down ) switch( keyName )
		{
		case Key.SELECT_UP: // Loop preview selected
		case Key.SELECT_DOWN:
			this.inputHandler = () -> {
				// Only loop preview module when none selected
				if( this.index() != -1 ) return;
				
				// Get a copied delegate to clear the effect of previous preview modification
				this.resetPrimary();
				
				// Get selected slot and player inventory
				final int slot = this.slot();
				final InventoryPlayer inv = this.player.inventory;
				final int size = inv.getSizeInventory() + 1;
				final int incr = key == InputHandler.SELECT_UP ? size : 2;
				
				int invSlot = this.previewInvSlot;
				while( ( invSlot = ( invSlot + incr ) % size - 1 ) != -1 )
				{
					final ItemStack stack = inv.getStackInSlot( invSlot );
					final IItemType type = IItemTypeHost.getType( stack );
					if( !( type instanceof IModifiableType ) ) continue;
					
					// Test if it is valid to install. // Copy before use.
					final IModifiable preview = ( ( IModifiableType ) type )
						.getContexted( stack.copy() );
					this.previewState = this.contexted.tryInstallPreview( slot, preview );
					if( this.previewState == ModifyPredication.NO_PREVIEW ) continue;
					
					// Is preview allowed, setup it as preview selected
					preview.$step( this.curStep );
					preview.$offset( this.curOffset );
					
					// Step and offset will always be updated hence no need to record original \
					// value. Paintjob otherwise does not.
					this.oriPaintjob
						= this.curPaintjob
						= preview.paintjob();
					
					// Check position conflict
					this.positionState = this.contexted.checkInstalledPosition( preview );
					
					preview.$modifyState( this.modifyState() );
					
					// Primary could been changed so re-fetch it from wrapper
					this.contexted = this.contexted.base().getInstalled( 0, 0 );
					this.selected = preview;
					break;
				}
				this.previewInvSlot = invSlot;
				
				// If not found, setup indicator
				if( invSlot == -1 )
				{
					this.previewState = ModifyPredication.NO_PREVIEW; // TODO: Seems not needed here
					this.positionState = ModifyPredication.OK;
					this.setupIndicator();
				}
			};
			break;
			
		case Key.SELECT_LEFT:
		case Key.SELECT_RIGHT:
			// Change step|offset|paintjob
			this.inputHandler = () -> {
				// Not available for indicator
				if( this.index() == -1 && this.previewInvSlot == -1 ) return;
				
				switch( this.mode )
				{
				case PAINTJOB:
					{
						final int bound = this.selected.paintjobCount();
						final int incr = key == InputHandler.SELECT_LEFT ? bound - 1 : 1;
						this.curPaintjob = ( this.curPaintjob + incr ) % bound;
						this.selected.$paintjob( this.curPaintjob );
					}
					break;
					
				case MODULE:
					{
						final int bound = this.selected.offsetCount();
						final int incr = key == InputHandler.SELECT_LEFT ? bound - 1 : 1;
						this.curOffset = ( this.curOffset + incr ) % bound;
						
						this.selected.$offset( this.curOffset );
						this.positionState = this.contexted.checkInstalledPosition( this.selected );
					}
					break;
					
				case SLOT:
					// Not available for primary base. But NONE_BASE provides a slot with 0 max \
					// step, which make it works for this case. // if( this.locLen > 0 )
					{
						final int bound = this.selected.base().getSlot( this.slot() ).maxStep();
						
						// Move upon player rotation to satisfy human intuition
						final float rot = ( this.player.rotationYaw % 360F + 360F ) % 360F;
						final int incr = key == InputHandler.SELECT_LEFT ^ rot < 180F ? 1 : bound;
						this.curStep = ( this.curStep + incr ) % ( bound + 1 );
						
						this.selected.$step( this.curStep );
						this.positionState = this.contexted.checkInstalledPosition( this.selected );
					}
					break;
				}
			};
			break;
			
		case Key.SELECT_CONFIRM:
			this.inputHandler = () -> {
				// Confirm changes if has selected
				if( this.index() != -1 )
				{
					final boolean stepChanged = this.curStep != this.oriStep;
					final boolean offsetChanged = this.curOffset != this.oriOffset;
					if( ( stepChanged || offsetChanged ) && this.positionState.okOrNotifyWhy() )
					{
						this.sendToServer(
							new PacketModify( this.curStep, this.curOffset, this.loc, this.locLen )
						);
						
						this.oriStep = this.curStep;
						this.oriOffset = this.curOffset;
					}
					
					if( this.curPaintjob != this.oriPaintjob )
					{
						// TODO: validate material
						if( true )
						{
							this.sendToServer(
								new PacketModify( this.curPaintjob, this.loc, this.locLen )
							);
							this.oriPaintjob = this.curPaintjob;
						}
						else ;// TODO: info can not offer
					}
				}
				
				// Installing a new module
				else if(
					this.previewInvSlot != -1
					&& this.previewState.okOrNotifyWhy() && this.positionState.okOrNotifyWhy()
				) {
					this.sendToServer(
						new PacketModify(
							this.previewInvSlot,
							this.curStep,
							this.curOffset,
							this.loc,
							this.locLen
						)
					);
					
//					this.oriStep = this.curStep; // This will no longer be selected, hence no need
//					this.oriOffset = this.curOffset;
					this.selected.$modifyState( ModifyState.NOT_SELECTED );
					
					if( this.curPaintjob != this.oriPaintjob )
					{
						// TODO: validate material
						if( true )
						{
							// Notice that preview actually has been installed in client side
							this.loc[ this.locLen - 1 ] = ( byte )
								( this.selected.base().getInstalledCount( this.slot() ) - 1 );
							this.sendToServer(
								new PacketModify( this.curPaintjob, this.loc, this.locLen )
							);
//							this.oriPaintjob = this.curPaintjob; // Not needed
							this.loc[ this.locLen - 1 ] = -1;
						}
						else
						{
							// TODO: info can not offer
							this.selected.$paintjob( this.oriPaintjob );
						}
					}
					
					// Clear preview selected after install
					this.previewInvSlot = -1;
					this.setupIndicator();
				}
			};
			break;
			
		case Key.SELECT_CANCEL:
			this.inputHandler = () -> {
				// Only can remove when it is selected and not primary base
				if( this.index() == -1 || this.locLen == 0 ) return;
				
				this.sendToServer( new PacketModify( this.loc, this.locLen ) );
				
//				this.selected.base().remove( this.slot(), this.index() );
				this.loc[ this.locLen - 1 ] = -1;
				this.positionState = ModifyPredication.OK; // Module position could be changed before remove
				this.setupIndicator();
			};
			break;
		}
		
		/// Co-key is not down ///
		else if( key == InputHandler.SELECT_CONFIRM )
		{
			this.inputHandler = () -> {
				// Enter next layer if has module selected
				if( this.index() == -1 ) return;
				
				if( this.selected.slotCount() == 0 )
					; // TODO: mcwb.msg.module_has_no_slot
				else if( this.locLen >= this.loc.length )
					; // TODO: mcwb.msg.reach_max_modify_layer
				else
				{
					// States may be changed before, hence restore it before moving into next layer
					this.selected.$step( this.oriStep );
					this.selected.$offset( this.oriOffset );
					this.selected.$paintjob( this.oriPaintjob );
					this.selected.$modifyState( ModifyState.NOT_SELECTED );
					this.positionState = ModifyPredication.OK;
					
					// Set index to 0 if has module installed in new slot, or -1 otherwise
					// Notice that when current index != -1, #preview must be null
					final int count = this.selected.getInstalledCount( 0 );
					
					this.loc[ this.locLen ] = 0;
					this.loc[ this.locLen + 1 ] = ( byte ) ( Math.min( 1, count ) - 1 );
					this.locLen += 2;
					
					this.setupSelection();
				}
			};
		}
		
		// Switch slot, switch module and quit layer require current layer > 0
		else if( this.locLen > 0 ) switch( keyName )
		{
		case Key.SELECT_CANCEL:
			// Quit current modify layer
			this.inputHandler = () -> {
				// Copy primary to clear preview modification effect
				this.resetPrimaryAndClearPreview();
				this.locLen -= 2;
				
				// Can be better as it will never meet the case to setup indicator
				this.setupSelection();
			};
			break;
			
		case Key.SELECT_UP:
		case Key.SELECT_DOWN:
			// Switch modify slot
			this.inputHandler = () -> {
				final IModifiable base = this.selected.base();
				if( base.slotCount() < 2 ) return;
				
				final int bound = base.slotCount();
				final int incr = key == InputHandler.SELECT_UP ? bound - 1 : 1;
				final int next = ( this.slot() + incr ) % bound;
				final int count = base.getInstalledCount( next );
				
				this.resetPrimaryAndClearPreview();
				this.loc[ this.locLen - 2 ] = ( byte ) next;
				this.loc[ this.locLen - 1 ] = ( byte ) ( Math.min( 1, count ) - 1 );
				
				this.setupSelection();
			};
			break;
			
	case Key.SELECT_LEFT:
	case Key.SELECT_RIGHT:
		// Switch selected module
		this.inputHandler = () -> {
			// Clear before calculate index so the installed count will be same in all cases
			this.resetPrimaryAndClearPreview();
			final IModifiable base = this.contexted.getInstalled( this.loc, this.locLen - 2 );
			
			// Here we shift it before do mod so -1 is included
			final int bound = base.getInstalledCount( this.slot() ) + 1;
			final int incr = key == InputHandler.SELECT_LEFT ? bound : 2;
			final int idx = ( this.index() + incr ) % bound - 1;
			this.loc[ this.locLen - 1 ] = ( byte ) idx;
			
			this.setupSelection();
		};
		break;
		}
	}
	
	@Override
	public IOperation launch( IOperation oldOp )
	{
		this.mode = ModifyMode.SLOT;
		this.loc = MCWBClient.modifyLoc;
		this.locLen = 0;
		
		this.previewInvSlot = -1;
		this.previewState = ModifyPredication.NO_PREVIEW;
		this.positionState = ModifyPredication.OK;
		
		this.selected = this.contexted;
		this.saveSelectedState();
		
		this.refPlayerRotYaw = this.player.rotationYaw;
		return super.launch( oldOp );
	}
	
	@Override
	public IOperation toggle()
	{
		// Only update player reference yaw rotation when this operation is fully launched
		if( this.prevProgress == 1F )
			this.refPlayerRotYaw = this.player.rotationYaw;
		
		this.locLen = 0;
		
		this.resetPrimaryAndClearPreview();
		this.selected = this.contexted;
		this.saveSelectedState();
		
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
		this.inputHandler = NO_TASK;
		return this;
	}
	
	@Override
	public IOperation terminate()
	{
		// Restore the original state on termination
		final IItemType type = IItemTypeHost.getType( this.copiedStack );
		final IModifiable primary = ( IModifiable ) type.getContexted( this.copiedStack );
		this.contexted.base().install( 0, ( IModifiable ) primary );
		
		this.clearProgress();
		return NONE;
	}
	
	@Override
	public IOperation onHoldingStackChange( IItem newItem )
	{
		// We basically has not way to ensure that this change is triggered by something like fire \
		// fire mode switch rather than swap hand. So in compromise, we just check whether current \
		// #loc is compatible with the new context or not. If it is then just continue.
		// NOTE:
		//     We do not check meta as it could change upon modification. For example, installing \
		//     special attachment could actually change the meta of primary module base.
		IModifiable newMod = ( IModifiable ) newItem;
		IModifiable oldMod = this.contexted;
		for( int i = 0; i < this.locLen; i += 2 )
		{
			final int slot = 0xFF & this.loc[ i ];
			final int idx = ( ( 0xFF & this.loc[ i + 1 ] ) + 1 ) % 256 - 1; // Map 255 to -1
			
			// This will also work if is last layer and nothing selected
			if( slot >= newMod.slotCount() || idx >= newMod.getInstalledCount( slot ) )
				return this.terminate();
			else if( i + 2 < this.locLen )
			{
				newMod = newMod.getInstalled( slot, idx );
				oldMod = oldMod.getInstalled( slot, idx );
			}
		}
		
		// Pass primary check, set #contexted to it and copy stack
		this.contexted = ( IModifiable ) newItem;
		this.copiedStack = this.player.inventory.getCurrentItem().copy();
		
		if( this.index() != -1 )
		{
			this.selected = this.contexted.getInstalled( this.loc, this.locLen );
			final boolean flg = this.curStep > this.selected.base().getSlot( this.slot() ).maxStep()
				|| this.curOffset >= this.selected.offsetCount()
				|| this.curPaintjob >= this.selected.paintjobCount();
			if( flg ) return this.terminate();
			
			this.selected.$step( this.curStep );
			this.selected.$offset( this.curOffset );
			this.selected.$paintjob( this.curPaintjob );
			this.selected.$modifyState( this.modifyState() );
			
			this.positionState = this.contexted.checkInstalledPosition( this.selected );
		}
		else if( this.previewInvSlot == -1 )
			this.setupIndicator();
		else
		{
			final IModifiable base = this.contexted.getInstalled( this.loc, this.locLen - 2 );
			this.previewState = base.tryInstallPreview( this.slot(), this.selected );
			if( this.previewState == ModifyPredication.NO_PREVIEW )
				this.terminate();
			
			this.positionState = this.contexted.checkInstalledPosition( this.selected );
		}
		return this;
	}
	
	protected int slot() { return 0xFF & ( this.locLen > 0 ? this.loc[ this.locLen - 2 ] : 0 ); }
	
	protected int index()
	{
		final byte idx = this.locLen > 0 ? this.loc[ this.locLen - 1 ] : 0;
		return idx != -1 ? 0xFF & idx : -1;
	}
	
	/**
	 * <p> Do 4 things: </p>
	 * <ol>
	 *     <li> Copy {@link #contexted} from {@link #copiedStack} </li>
	 *     <li> Reset {@link #previewState} to {@link ModifyPredication#NO_PREVIEW} </li>
	 *     <li> Reset {@link #positionState} to {@link ModifyPredication#OK} </li>
	 *     <li> Reset {@link #previewInvSlot} to {@code -1} </li>
	 * </ol>
	 */
	protected void resetPrimaryAndClearPreview()
	{
		this.resetPrimary();
		this.previewInvSlot = -1;
		this.previewState = ModifyPredication.NO_PREVIEW;
		this.positionState = ModifyPredication.OK;
	}
	
	protected void resetPrimary()
	{
		final ItemStack copiedStack = this.copiedStack.copy();
		final IItemType type = IItemTypeHost.getType( copiedStack );
		final IModifiable copiedPrimary = ( IModifiable ) type.getContexted( copiedStack );
		this.contexted.base().install( 0, copiedPrimary );
		this.contexted = copiedPrimary;
	}
	
	/**
	 * Set original state to the selected module if has. Otherwise, setup indicator.
	 */
	protected void setupSelection()
	{
		if( this.index() != -1 )
		{
			this.selected = this.contexted.getInstalled( this.loc, this.locLen );
			this.selected.$modifyState( this.modifyState() );
			this.saveSelectedState();
		}
		else
		{
			this.setupIndicator();
			this.oriStep = this.curStep = 0; // TODO: check if this is needed
			this.oriOffset = this.curOffset = 0;
		}
	}
	
	/**
	 * <p> Do two things: </p>
	 * <ol>
	 *     <li> Create a new indicator </li>
	 *     <li> Set {@link #selected} to it </li>
	 * </ol>
	 */
	protected void setupIndicator()
	{
		final IModifiable base = this.contexted.getInstalled( this.loc, this.locLen - 2 );
		final IModifiable indicator = base.newModifyIndicator();
		indicator.$step( base.getSlot( this.slot() ).maxStep() / 2 );
		indicator.$modifyState( ModifyState.SELECTED_OK );
		base.install( this.slot(), indicator );
		this.selected = indicator;
		// TODO: update primary maybe??
	}
	
	/**
	 * Update all original state with {@link #selected}
	 */
	protected void saveSelectedState()
	{
		this.oriStep
			= this.curStep
			= this.selected.step();
		this.oriOffset
			= this.curOffset
			= this.selected.offset();
		this.oriPaintjob
			= this.curPaintjob
			= this.selected.paintjob();
	}
	
	protected ModifyState modifyState()
	{
		return this.locLen == 0 || this.mode == ModifyMode.PAINTJOB ? ModifyState.NOT_SELECTED
			: this.previewState.ok() && this.positionState.ok()
				? ModifyState.SELECTED_OK : ModifyState.SELECTED_CONFLICT;
	}
	
	protected static enum ModifyMode
	{
		SLOT,
		MODULE,
		PAINTJOB;
		
		public final String notifyMsg = "mcwb.msg.modify_mode_" + this.name().toLowerCase();
	}
}
