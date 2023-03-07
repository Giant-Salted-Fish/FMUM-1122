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
import com.mcwb.common.module.IModifyPredicate;
import com.mcwb.common.module.IModifyState;
import com.mcwb.common.module.IModular;
import com.mcwb.common.module.IPreviewPredicate;
import com.mcwb.common.network.PacketModify;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.OperationController;
import com.mcwb.common.operation.TogglableOperation;
import com.mcwb.common.paintjob.IPaintable;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public class OpModifyClient extends TogglableOperation< IModular< ? > >
	implements IAutowirePacketHandler, IAutowirePlayerChat
{
	protected static final Runnable IDLE = () -> { };
	
	protected static final IPaintable PAINTABLE = new IPaintable()
	{
		@Override
		public void setPaintjob( int paintjob ) { }
		
		@Override
		public int paintjobCount() { return 1; }
		
		@Override
		public int paintjob() { return 0; }
		
		// TODO: maybe throw exception
		@Override
		public boolean tryOffer( int paintjob, EntityPlayer player ) { return false; }
		
		@Override
		@SideOnly( Side.CLIENT )
		public boolean tryOfferOrNotifyWhy( int paintjob, EntityPlayer player ) { return false; }
	};
	
	public float refPlayerRotYaw = 0F;
	
	protected ModifyMode mode = ModifyMode.SLOT;
	
	protected byte[] loc;
	protected int locLen;
	
	protected ItemStack stack;
	protected IModular< ? > renderDelegate;
	
	/**
	 * None null. Should be the currently selected module or the preview module or the indicator.
	 */
	protected IModular< ? > cursor;
	protected IPaintable paintable;
	
	/**
	 * {@code -1} if no preview selected
	 */
	protected int previewInvSlot;
	
	protected int curOffset;
	protected int oriOffset;
	
	protected int curStep;
	protected int oriStep;
	
	protected int curPaintjob;
	protected int oriPaintjob;
	
	protected IPreviewPredicate previewState = IPreviewPredicate.NO_PREVIEW;
	protected IModifyPredicate positionState = IModifyPredicate.OK;
	
	protected Runnable inputHandler = IDLE;
	
	public OpModifyClient() {
		super( null, null, new OperationController( 0.1F ), new OperationController( -0.1F ) );
	}
	
	@SuppressWarnings( "unchecked" )
	public final < T extends IModular< ? > > T delegate( T original ) {
		return this.renderDelegate != null ? ( T ) this.renderDelegate : original;
	}
	
	public IOperation reset()
	{
		this.player = MCWBClient.MC.player;
		this.stack = this.player.inventory.getCurrentItem();
		return this;
	}
	
	public void handleInput( IKeyBind key )
	{
		final String keyName = key.name();
		if( keyName.equals( Key.SELECT_TOGGLE ) )
		{
			// Change modify mode
			this.inputHandler = () -> {
				final ModifyMode[] modes = ModifyMode.values();
				this.mode = modes[ ( this.mode.ordinal() + 1 ) % modes.length ];
				this.sendPlayerPrompt( I18n.format( this.mode.notifyMsg ) );
				
				this.cursor.setModifyState( this.modifyState() );
			};
		}
		else if( InputHandler.CO.down ) switch( keyName )
		{
		case Key.SELECT_UP:
		case Key.SELECT_DOWN:
			// Loop preview module to install
			this.inputHandler = () -> {
				// Only loop preview module when none selected
				if( this.index() != -1 ) return;
				
				// Copy contexted to clear the effect of previous preview modification
				this.copyPrimary();
				final IModular< ? > base = this.contexted.getInstalled( this.loc, this.locLen - 2 );
				
				// Get selected slot and player inventory
				final int slot = this.slot();
				final InventoryPlayer inv = this.player.inventory;
				final int size = inv.getSizeInventory();
				final int incr = key == InputHandler.SELECT_UP ? size : 2;
				
				int invSlot = this.previewInvSlot;
				while( ( invSlot = ( invSlot + incr ) % size - 1 ) != -1 )
				{
					final ItemStack stack = inv.getStackInSlot( invSlot );
					final IItemType type = IItemTypeHost.getTypeOrDefault( stack );
					final IItem item = type.getContexted( stack );
					if( !( item instanceof IModular< ? > ) ) continue;
					
					// Test if it is valid to install // Copy before use
					final IModular< ? > preview = ( IModular< ? > )
						type.getContexted( stack.copy() );
					this.previewState = base.tryInstall( slot, preview );
					if( this.previewState == IPreviewPredicate.NO_PREVIEW ) continue;
					
					// Is preview allowed, re-fetch and setup it as preview module
					this.fetchCursor();
					this.cursor.setOffsetStep( this.curOffset = 0, this.curStep );
					
					// Step and offset will always be updated hence no need to record original \
					// value. Paintjob otherwise does not.
					final int paintjob = this.paintable.paintjob();
					this.curPaintjob = paintjob;
					this.oriPaintjob = paintjob;
					
					this.fetchCursorAndSetup();
					break;
				}
				this.previewInvSlot = invSlot;
				
				// Setup indicator if no preview found
				if( invSlot == -1 ) this.setupIndicator();
			};
			break;
			
		case Key.SELECT_LEFT:
		case Key.SELECT_RIGHT:
			// Change step/offset/paintjob
			this.inputHandler = () -> {
				// Not available for indicator
				if( this.index() == -1 && this.previewInvSlot == -1 ) return;
				
				switch( this.mode )
				{
				case PAINTJOB:
					{
						final int bound = this.paintable.paintjobCount();
						final int incr = key == InputHandler.SELECT_LEFT ? bound - 1 : 1;
						this.curPaintjob = ( this.curPaintjob + incr ) % bound;
						this.paintable.setPaintjob( this.curPaintjob );
						this.fetchCursorAndSetup(); // Cursor may have changed
					}
					break;
					
				case MODULE:
					{
						final int bound = this.cursor.offsetCount();
						final int incr = key == InputHandler.SELECT_LEFT ? bound - 1 : 1;
						this.curOffset = ( this.curOffset + incr ) % bound;
						this.cursor.setOffsetStep( this.curOffset, this.curStep );
						this.fetchCursorAndSetup();
					}
					break;
					
				case SLOT:
					// Not available for primary base. But wrapper provides a slot with 0 max step \
					// hence makes it work for this case. // if( this.locLen > 0 )
					{
						final int bound = this.cursor.base().getSlot( this.slot() ).maxStep();
						
						// Move upon player rotation to satisfy human intuition
						final float rot = ( this.player.rotationYaw % 360F + 360F ) % 360F;
						final int incr = key == InputHandler.SELECT_LEFT ^ rot < 180F ? 1 : bound;
						this.curStep = ( this.curStep + incr ) % ( bound + 1 );
						this.cursor.setOffsetStep( this.curOffset, this.curStep );
						this.fetchCursorAndSetup();
					}
					break;
				}
			};
			break;
			
		case Key.SELECT_CONFIRM:
			// Confirm modifications that has been applied
			this.inputHandler = () -> {
				if( this.index() != -1 )
				{
					if( !this.positionState.okOrNotifyWhy() ) return;
					
					final boolean offsetChanged = this.curOffset != this.oriOffset;
					final boolean stepChanged = this.curStep != this.oriStep;
					if( offsetChanged || stepChanged )
					{
						this.sendToServer( new PacketModify(
							this.curOffset, this.curStep,
							this.loc, this.locLen
						) );
					}
					
					if(
						this.curPaintjob != this.oriPaintjob
						&& this.paintable.tryOfferOrNotifyWhy( this.curPaintjob, this.player )
					) {
						this.sendToServer( new PacketModify(
							this.curPaintjob,
							this.loc, this.locLen
						) );
					}
					
					// Clear selection after confirm
					this.loc[ this.locLen - 1 ] = -1;
					this.copyPrimary();
					this.setupIndicator();
				}
				else if(
					this.previewInvSlot != -1
					&& this.previewState.ok() && this.positionState.ok()
				) {
					// Install preview module
					this.sendToServer( new PacketModify(
						this.previewInvSlot,
						this.curOffset, this.curStep,
						this.loc, this.locLen
					) );
					
					if(
						this.curPaintjob != this.oriPaintjob
						&& this.paintable.tryOfferOrNotifyWhy( this.curPaintjob, this.player )
					) {
						// Notice that preview actually has been installed in client side
						final int count = this.cursor.base().getInstalledCount( this.slot() );
						this.loc[ this.locLen - 1 ] = ( byte ) ( count - 1 );
						this.sendToServer( new PacketModify(
							this.curPaintjob,
							this.loc, this.locLen
						) );
						this.loc[ this.locLen - 1 ] = -1;
					}
					
					this.copyPrimary();
					this.setupIndicator();
					this.previewInvSlot = -1; // Clear preview after installation
				}
			};
			break;
			
		case Key.SELECT_CANCEL:
			// Remove selected module
			this.inputHandler = () -> {
				// Only can remove when it is selected and not primary base
				if( this.index() == -1 || this.locLen == 0 ) return;
				
				this.sendToServer( new PacketModify( this.loc, this.locLen ) );
				this.loc[ this.locLen - 1 ] = -1;
//				this.positionState = IModifyPredicate.OK; // FIXME: if this is needed?
				this.copyPrimary();
				this.setupIndicator();
			};
			break;
		}
		
		/// *** Co-key not down *** ///
		else if( key == InputHandler.SELECT_CONFIRM )
		{
			// Enter next layer
			this.inputHandler = () -> {
				// Enter next layer if has module selected
				if( this.index() == -1 || this.cursor.slotCount() == 0 ) return;
				
				if( this.locLen >= this.loc.length )
					; // TODO: mcwb.msg.reach_max_modify_layer
				else
				{
					final int count = this.cursor.getInstalledCount( 0 );
					
					this.loc[ this.locLen ] = 0;
					this.loc[ this.locLen + 1 ] = ( byte ) Math.min( 0, count - 1 );
					this.locLen += 2;
					
					this.copyPrimaryAndSetupSelection();
				}
			};
		}
		
		// Switch slot, switch module and quit layer require current layer > 0
		else if( this.locLen > 0 ) switch( keyName )
		{
		case Key.SELECT_CANCEL:
			// Quit current layer
			this.inputHandler = () -> {
				this.locLen -= 2;
				this.copyPrimaryAndSetupSelection();
			};
			break;
			
		case Key.SELECT_UP:
		case Key.SELECT_DOWN:
			// Switch slot
			this.inputHandler = () -> {
				final IModular< ? > base = this.cursor.base();
				if( base.slotCount() < 2 ) return;
				
				final int bound = base.slotCount();
				final int incr = key == InputHandler.SELECT_UP ? bound - 1 : 1;
				final int next = ( this.slot() + incr ) % bound;
				final int count = base.getInstalledCount( next );
				
				this.loc[ this.locLen - 2 ] = ( byte ) next;
				this.loc[ this.locLen - 1 ] = ( byte ) Math.min( 0, count - 1 );
				
				this.copyPrimaryAndSetupSelection();
			};
			break;
			
		case Key.SELECT_LEFT:
		case Key.SELECT_RIGHT:
			// Switch modules in current slot
			this.inputHandler = () -> {
				final IModular< ? > base = this.cursor.base();
				
				// There will be an indicator or preview module installed if #index() == -1
				final int offset = Math.min( 1, this.index() + 1 );
				final int bound = base.getInstalledCount( this.slot() ) + offset;
				final int incr = key == InputHandler.SELECT_LEFT ? bound : 2;
				final int idx = ( this.index() + incr ) % bound - 1;
				this.loc[ this.locLen - 1 ] = ( byte ) idx;
				
				this.copyPrimaryAndSetupSelection();
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
		
		this.copyPrimaryAndSetupSelection();
		
		this.refPlayerRotYaw = this.player.rotationYaw;
		return super.launch( oldOp );
	}
	
	@Override
	public IOperation toggle()
	{
		// Only update player reference yaw rotation when this operation is fully launched
		final boolean fullyLaunched = this.prevProgress == 1F;
		this.refPlayerRotYaw = fullyLaunched ? this.player.rotationYaw : this.refPlayerRotYaw;
		
		this.mode = ModifyMode.SLOT;
		this.locLen = 0;
		
		this.copyPrimaryAndSetupSelection();
		return super.toggle();
	}
	
	@Override
	public IOperation tick()
	{
		if( super.tick() == NONE ) return this.terminate();
		
		// Do not do modification if operation has not been fully launched
		if( this.prevProgress != 1F ) return this;
		
		this.inputHandler.run();
		this.inputHandler = IDLE;
		return this;
	}
	
	@Override
	public IOperation terminate()
	{
		this.renderDelegate = null;
		this.clearProgress();
		return NONE;
	}
	
	@Override
	public IOperation onInHandStackChange( IItem newItem )
	{
		// We basically has not way to ensure that this change is triggered by something like fire \
		// fire mode switch rather than swap hand. So in compromise, we just check whether current \
		// #loc is compatible with the new context or not. If it is then just continue.
		// NOTE:
		//     We do not check meta as it could change upon modification. For example, installing \
		//     special attachment could actually change the meta of primary module base.
		IModular< ? > newMod = ( ( IModular< ? > ) newItem ).getInstalled( this.loc, 0 );
		IModular< ? > oldMod = this.contexted.getInstalled( this.loc, 0 );
		for( int i = 0; i < this.locLen; i += 2 )
		{
			final int slot = 0xFF & this.loc[ i ];
			final int idx = ( ( 0xFF & this.loc[ i + 1 ] ) + 1 ) % 256 - 1; // Map 255 to -1
			
			// This will also work if is last layer and nothing selected
			if( slot >= newMod.slotCount() || idx >= newMod.getInstalledCount( slot ) )
				return this.terminate();
			
			if( i + 2 < this.locLen )
			{
				newMod = newMod.getInstalled( slot, idx );
				oldMod = oldMod.getInstalled( slot, idx );
			}
		}
		
		// Pass location check
		this.stack = this.player.inventory.getCurrentItem();
		this.copyPrimary();
		
		if( this.index() != -1 )
		{
			this.fetchCursor();
			final boolean flag = this.curOffset >= this.cursor.offsetCount()
				|| this.curStep > this.cursor.base().getSlot( this.slot() ).maxStep()
				|| this.curPaintjob >= this.paintable.paintjobCount();
			if( flag ) return this.terminate();
			
			this.cursor.setOffsetStep( this.curOffset, this.curStep );
			this.fetchCursor();
			this.paintable.setPaintjob( this.curPaintjob );
			this.fetchCursorAndSetup();
		}
		else if( this.previewInvSlot == -1 )
			this.setupIndicator();
		else
		{
			final IModular< ? > base = this.contexted.getInstalled( this.loc, this.locLen - 2 );
			final ItemStack stack = this.player.inventory.getStackInSlot( this.previewInvSlot );
			final IItemType type = IItemTypeHost.getTypeA( stack );
			final IModular< ? > preview = ( IModular< ? > ) type.getContexted( stack.copy() );
			this.previewState = base.tryInstall( this.slot(), preview );
			if( this.previewState == IPreviewPredicate.NO_PREVIEW )
				this.terminate();
			
			this.fetchCursor();
			this.cursor.setOffsetStep( this.curOffset, this.curStep );
			this.fetchCursor();
			this.paintable.setPaintjob( this.curPaintjob );
			this.fetchCursorAndSetup();
		}
		return this;
	}
	
	/**
	 * @return {@code 0} if is primary selected
	 */
	protected int slot() { return this.locLen > 0 ? 0xFF & this.loc[ this.locLen - 2 ] : 0; }
	
	protected int index()
	{
		final byte idx = this.locLen > 0 ? this.loc[ this.locLen - 1 ] : 0;
		return idx != -1 ? 0xFF & idx : -1;
	}
	
	protected void copyPrimary()
	{
		final ItemStack copiedStack = this.stack.copy();
		final IItemType type = IItemTypeHost.getTypeA( copiedStack );
		this.contexted = ( IModular< ? > ) type.getContexted( copiedStack );
		this.renderDelegate = this.contexted.getInstalled( this.loc, 0 );
	}
	
	protected void fetchCursorAndSetup()
	{
		this.fetchCursor();
		this.positionState = this.contexted.checkHitboxConflict( this.cursor );
		this.contexted.setModifyState( IModifyState.PRIMARY_START );
		this.cursor.setModifyState( this.modifyState() );
	}
	
	protected void fetchCursor()
	{
		final int idx = this.index();
		this.cursor = idx != -1 ? this.contexted.getInstalled( this.loc, this.locLen )
			: this.contexted.getInstalled( this.loc, this.locLen - 2 )
				.getInstalled( this.slot(), this.previewState.index() );
		this.paintable = this.cursor instanceof IPaintable ? ( IPaintable ) this.cursor : PAINTABLE;
	}
	
	protected void copyPrimaryAndSetupSelection()
	{
		this.copyPrimary();
		this.previewInvSlot = -1;
		
		if( this.index() != -1 )
		{
			this.previewState = IPreviewPredicate.NO_PREVIEW;
			this.positionState = IModifyPredicate.OK;
			
			this.fetchCursor();
			this.contexted.setModifyState( IModifyState.PRIMARY_START );
			this.cursor.setModifyState( this.modifyState() );
			
			final int offset = this.cursor.offset();
			this.curOffset = offset;
			this.oriOffset = offset;
			final int step = this.cursor.step();
			this.curStep = step;
			this.oriStep = step;
			final int paintjob = this.paintable.paintjob();
			this.curPaintjob = paintjob;
			this.oriPaintjob = paintjob;
		}
		else
		{
			this.curOffset = 0;
			this.oriOffset = 0;
			this.curStep = 0;
			this.oriStep = 0;
			this.setupIndicator();
		}
	}
	
	protected void setupIndicator()
	{
		this.contexted.setModifyState( IModifyState.PRIMARY_START );
		// Do not use #cursor#base() as it could be called upon primary copy
		final IModular< ? > base = this.contexted.getInstalled( this.loc, this.locLen - 2 );
		final IModular< ? > indicator = base.newModifyIndicator();
		final int slot = this.slot();
		indicator.setOffsetStep( 0, base.getSlot( slot ).maxStep() / 2 );
		indicator.setModifyState( IModifyState.SELECTED_OK );
		base.install( slot, indicator );
		this.cursor = indicator;
		this.paintable = PAINTABLE;
	}
	
	protected IModifyState modifyState()
	{
		return this.locLen == 0 ? IModifyState.PRIMARY_START
			: this.mode == ModifyMode.PAINTJOB ? IModifyState.NOT_SELECTED
				: this.previewState.ok() && this.positionState.ok()
					? IModifyState.SELECTED_OK : IModifyState.SELECTED_CONFLICT;
	}
	
	protected static enum ModifyMode
	{
		SLOT,
		MODULE,
		PAINTJOB;
		
		public final String notifyMsg = "mcwb.msg.modify_mode_" + this.name().toLowerCase();
	}
}
