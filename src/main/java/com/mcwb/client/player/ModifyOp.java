 package com.mcwb.client.player;

import com.mcwb.client.IAutowirePlayerChat;
import com.mcwb.client.MCWBClient;
import com.mcwb.client.input.IKeyBind;
import com.mcwb.client.input.InputHandler;
import com.mcwb.client.input.Key;
import com.mcwb.common.IAutowirePacketHandler;
import com.mcwb.common.item.IContextedItem;
import com.mcwb.common.item.IItemMeta;
import com.mcwb.common.item.IItemMetaHost;
import com.mcwb.common.modify.IContextedModifiable;
import com.mcwb.common.modify.IContextedModifiable.ModifyState;
import com.mcwb.common.modify.IModifiableMeta;
import com.mcwb.common.modify.IModuleSlot;
import com.mcwb.common.network.PacketModify;
import com.mcwb.common.player.IOperation;
import com.mcwb.common.player.TogglableOperation;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class ModifyOp< T extends IContextedItem & IContextedModifiable >
	extends TogglableOperation< T > implements IAutowirePacketHandler, IAutowirePlayerChat
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
	
	/**
	 * None null. Should be the currently selected module or the preview module or the indicator.
	 */
	protected IContextedModifiable selected = null;
	
	/**
	 * {@code -1} if no preview selected
	 */
	protected int previewInvSlot = 0;
	
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
				
				// TODO: check if this breaks for indicator
				this.selected.$modifyState( this.modifyState() );
			};
		}
		else if( InputHandler.CO.down ) switch( kName )
		{
		case Key.SELECT_UP:
		case Key.SELECT_DOWN:
			this.inputHandler = () -> {
				// Only loop preview module when none selected
				if( this.index() != -1 ) return;
				
				// Remove previously installed indicator/preview
				final int islot = this.slot();
				final IContextedModifiable base = this.selected.base();
				base.remove( islot, base.getInstalledCount( islot ) - 1 );
				
				// Get selected slot and player inventory
				final IModuleSlot slot = base.getSlot( islot );
				final InventoryPlayer inv = this.player.inventory;
				final int size = inv.getSizeInventory() + 1;
				final int incr = key == InputHandler.SELECT_UP ? size : 2;
				
				while( ( this.previewInvSlot = ( this.previewInvSlot + incr ) % size - 1 ) != -1 )
				{
					final ItemStack stack = inv.getStackInSlot( this.previewInvSlot );
					final IItemMeta meta = IItemMetaHost.getMeta( stack );
					if(
						meta instanceof IModifiableMeta
						&& slot.isAllowed( ( ( IModifiableMeta ) meta ).getContexted( stack ) )
					) {
						// Get a copy of the preview module context
						final IContextedModifiable preview = ( ( IModifiableMeta ) meta )
							.getContexted( stack ).newModifyDelegate();
						
						preview.$step( this.curStep );
						preview.$offset( this.curOffset = 0 );
						
						// Step and offset will always be set when install a new module, but \
						// paintjob would not be update if original paintjob does not change. \
						// Hence #oriPaintjob needs to be setup but those two do not.
						this.curPaintjob
							= this.oriPaintjob
							= preview.paintjob();
						
						this.conflict = !base.canInstall( islot, preview );
						// TODO: check hit box
						
						preview.$modifyState( this.modifyState() );
						base.install( islot, preview );
						
						this.selected = preview; // Do not forget to update #selected
						break;
					}
				}
				
				// If not found, setup indicator
				if( this.previewInvSlot == -1 )
					this.setupIndicator(); // this.conflict = false; Seems not needed
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
						
						// TODO: Check whether the new position will cause conflict or not
						this.conflict = false;
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
						
						// TODO: Check whether the new position will cause conflict or not
						this.conflict = false;
					}
					break;
				}
			};
			break;
			
		case Key.SELECT_CONFIRM:
			this.inputHandler = () -> {
				if( this.index() != -1 )
				{
					final boolean stepChanged = this.curStep != this.oriStep;
					final boolean offsetChanged = this.curOffset != this.oriOffset;
					if( ( stepChanged || offsetChanged ) && !this.conflict )
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
				else if( this.previewInvSlot != -1 && !this.conflict )
				{
					this.sendToServer(
						new PacketModify(
							this.previewInvSlot,
							this.curStep,
							this.curOffset,
							this.loc,
							this.locLen
						)
					);
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
				// Only can remove when it is selected and is not primary base
				if( this.index() == -1 || this.locLen == 0 ) return;
				
				this.sendToServer( new PacketModify( this.loc, this.locLen ) );
				
				this.selected.base().remove( this.slot(), this.index() );
				this.loc[ this.locLen - 1 ] = -1;
				this.conflict = false; // Module position could be changed before unstall
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
					this.restoreSelectedState();
					
					// Set index to 0 if has module installed in new slot, or -1 otherwise
					// Notice that when current index != -1, #preview must be null
					final int count = this.selected.getInstalledCount( 0 );
					
					this.loc[ this.locLen ] = 0;
					this.loc[ this.locLen - 1 ] = ( byte ) ( Math.min( 1, count ) - 1 );
					this.locLen += 2;
					
					this.setupSelection();
				}
			};
		}
		
		// Switch slot, switch module and quit layer require current layer > 0
		else if( this.locLen > 0 ) switch( kName )
		{
		case Key.SELECT_CANCEL:
			// Quit current modify layer
			this.inputHandler = () -> {
				this.clearForLeave();
				this.locLen -= 2;
				
				this.selected = this.primary.getInstalled( this.loc, this.locLen );
				this.saveSelectedState();
			};
			break;
			
		case Key.SELECT_UP:
		case Key.SELECT_DOWN:
			// Switch modify slot
			this.inputHandler = () -> {
				final IContextedModifiable base = this.selected.base();
				if( base.slotCount() < 2 ) return;
				
				final int bound = base.slotCount();
				final int incr = key == InputHandler.SELECT_UP ? bound - 1 : 1;
				final int next = ( this.slot() + incr ) % bound;
				final int count = base.getInstalledCount( next );
				
				this.clearForLeave();
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
				final IContextedModifiable base = this.selected.base();
				this.clearForLeave();
				
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
	
	public IContextedItem getRenderDelegate( IContextedItem original ) {
		return this.primary != null ? this.primary : original;
	}
	
	@Override
	@SuppressWarnings( "unchecked" )
	public IOperation launch( IOperation oldOp )
	{
		this.mode = ModifyMode.SLOT;
		
		this.loc = MCWBClient.modifyLoc;
		this.locLen = 0;
		
		this.previewInvSlot = -1;
		this.primary = ( T ) this.contexted.newModifyDelegate();
		this.selected = this.primary;
		this.saveSelectedState();
		
		this.refPlayerRotYaw = this.player.rotationYaw;
		return super.launch( oldOp );
	}
	
	@Override
	@SuppressWarnings( "unchecked" )
	public IOperation toggle()
	{
		// Only update player reference yaw rotation when this operation is fully launched
		if( this.prevProgress == 1F )
			this.refPlayerRotYaw = this.player.rotationYaw;
		
		this.locLen = 0;
		
		this.previewInvSlot = -1;
		this.primary = ( T ) this.contexted.newModifyDelegate();
		this.selected = this.primary;
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
		return this;
	}
	
	@Override
	public IOperation terminate()
	{
		this.primary = null;
		this.clearProgress();
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
			
			// This will also work if is last layer and nothing selected
			if( slot >= newMod.slotCount() || idx >= newMod.getInstalledCount( slot ) )
				return this.terminate();
			else if( i + 2 < this.locLen )
			{
				newMod = newMod.getInstalled( slot, idx );
				prevMod = prevMod.getInstalled( slot, idx );
			}
		}
		
		// May be a valid context to continue modify, copy it
		this.contexted = ( T ) newItem;
		this.primary = ( T ) this.contexted.newModifyDelegate();
		
		// If has module selected the just update it current state
		if( this.index() != -1 )
		{
			this.selected = this.primary.getInstalled( this.loc, this.locLen );
			if( this.curStep > this.selected.base().getSlot( this.slot() ).maxStep() )
				return this.terminate();
			
			this.selected.$step( this.curStep );
			this.selected.$offset( this.curOffset );
			this.selected.$paintjob( this.curPaintjob );
			this.selected.$modifyState( this.modifyState() );
			return this;
		}
		
		// Otherwise, if no preview, install indicator
		// NOTE: Not directly install selected as this may crash if it is not allowed
		final IContextedModifiable base = this.primary.getInstalled( this.loc, this.locLen - 2 );
		if( this.previewInvSlot == -1 )
		{
			base.install( this.slot(), this.selected );
			return this;
		}
		
		final IModuleSlot slot = base.getSlot( this.slot() );
		if( !slot.isAllowed( this.selected ) || this.curStep > slot.maxStep() )
			return this.terminate();
		
		base.install( this.slot(), this.selected );
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
	
	/**
	 * Set original state with selected module if has. Otherwise, setup indicator.
	 */
	protected void setupSelection()
	{
		if( this.index() != -1 )
		{
			final IContextedModifiable module = this.primary.getInstalled( this.loc, this.locLen );
			this.selected = module;
			this.saveSelectedState();
		}
		else
		{
			this.setupIndicator();
			this.oriStep = this.curStep = 0;
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
		final IContextedModifiable base = this.primary.getInstalled( this.loc, this.locLen - 2 );
		final IContextedModifiable indicator = base.newModifyIndicator();
		indicator.$step( base.getSlot( this.slot() ).maxStep() / 2 );
		indicator.$modifyState( ModifyState.SELECTED_OK );
		base.install( this.slot(), indicator );
		this.selected = indicator;
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
	
	/**
	 * <p> Do following things: </p>
	 * <ol>
	 *     <li> Unstall indicator or preview if has </li>
	 *     <li> Clear {@link #previewInvSlot} if has </li>
	 *     <li> Call {@link #restoreSelectedState()} if has module selected </li>
	 *     <li> Set {@link #conflict} to {@code false} </li>
	 * </ol>
	 */
	protected void clearForLeave()
	{
		if( this.index() == -1 )
		{
			final IContextedModifiable base = this.selected.base();
			final int islot = this.slot();
			base.remove( islot, base.getInstalledCount( islot ) - 1 );
			this.previewInvSlot = -1;
			this.conflict = false;
		}
		else this.restoreSelectedState();
	}
	
	/**
	 * <p> Do three things: </p>
	 * <ol>
	 *     <li> Set the state of the selected to original </li>
	 *     <li> Set modify state of the selected to {@link ModifyState#NOT_SELECTED} </li>
	 *     <li> Set {@link #conflict} to {@code false} </li>
	 * </ol>
	 */
	protected void restoreSelectedState()
	{
		this.selected.$step( this.oriStep );
		this.selected.$offset( this.oriOffset );
		this.selected.$paintjob( this.oriPaintjob );
		this.selected.$modifyState( ModifyState.NOT_SELECTED );
		this.conflict = false;
	}
	
	protected ModifyState modifyState()
	{
		return this.locLen == 0 || this.mode == ModifyMode.PAINTJOB ? ModifyState.NOT_SELECTED
			: this.conflict ? ModifyState.SELECTED_CONFLICT : ModifyState.SELECTED_OK;
	}
	
	protected static enum ModifyMode
	{
		SLOT,
		MODULE,
		PAINTJOB;
		
		public final String notifyMsg = "mcwb.msg.modify_mode_" + this.name().toLowerCase();
	}
}
