package com.mcwb.client.player;

import static com.mcwb.client.MCWBClient.MC;
import static com.mcwb.common.module.IModifyPredicate.OK;
import static com.mcwb.common.module.IPreviewPredicate.NO_PREVIEW;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.mcwb.client.IAutowirePlayerChat;
import com.mcwb.client.MCWBClient;
import com.mcwb.client.input.IInput;
import com.mcwb.client.input.Key;
import com.mcwb.common.IAutowirePacketHandler;
import com.mcwb.common.item.IEquippedItem;
import com.mcwb.common.item.IItem;
import com.mcwb.common.item.IItemTypeHost;
import com.mcwb.common.module.IModifyPredicate;
import com.mcwb.common.module.IModifyState;
import com.mcwb.common.module.IModule;
import com.mcwb.common.module.IPreviewPredicate;
import com.mcwb.common.network.PacketModify;
import com.mcwb.common.operation.IOperation;
import com.mcwb.common.operation.IOperationController;
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
public abstract class OpModifyClient extends TogglableOperation
	implements IAutowirePacketHandler, IAutowirePlayerChat
{
	protected static final IPaintable PAINTABLE_PLACEHOLDER = new IPaintable()
	{
		@Override
		public boolean tryOfferOrNotifyWhy( int paintjob, EntityPlayer player ) { return false; }
		
		@Override
		public boolean tryOffer( int paintjob, EntityPlayer player ) { return false; }
		
		@Override
		public void setPaintjob( int paintjob ) { }
		
		@Override
		public int paintjobCount() { return 1; }
		
		@Override
		public int paintjob() { return 0; }
	};
	
	protected static final OperationController
		FORWARD_CONTROLLER = new OperationController( 0.1F ),
		BACKWARD_CONTROLLER = new OperationController( -0.1F );
	
	public float refPlayerRotYaw;
	
	protected IEquippedItem< ? > equipped;
	
	protected byte[] loc;
	protected int locLen;
	
	@Nonnull
	protected IModule< ? > primary;
	
	@Nonnull
	protected IModule< ? > cursor;
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
	
	protected IPreviewPredicate previewPredicate = NO_PREVIEW;
	protected IModifyPredicate positionPredicate = OK;
	
	protected Runnable deferredModifyTask = () -> { };
	
	protected final LinkedList< ModifyMode > modifyModes = new LinkedList<>();
	{
		final Supplier< IModifyState > modifyState = () -> {
			if ( this.locLen == 0 ) { return IModifyState.NOT_SELECTED; }
			final boolean ok = this.previewPredicate.ok() && this.positionPredicate.ok();
			return ok ? IModifyState.SELECTED_OK : IModifyState.SELECTED_CONFLICT;
		};
		this.modifyModes.add( new ModifyMode(
			"slot",
			backward -> {
				// Actually not avaible for primary base. But wrapper provides a default slot that \
				// allows us to treat them equally.
				final int bound = this.cursor.base().getSlot( this.slot() ).maxStep();
				
				// Move upon player rotation to satisfy human intuition.
				final float rawRotYaw = MC.player.rotationYaw;
				final float rotYaw = ( rawRotYaw % 360F + 360F ) % 360F;
				final int incr = backward ^ rotYaw < 180F ? 1 : bound;
				this.curStep = ( this.curStep + incr ) % ( bound + 1 );
				this.cursor.setOffsetStep( this.curOffset, this.curStep );
			},
			modifyState
		) );
		this.modifyModes.add( new ModifyMode(
			"module",
			backward -> {
				final int bound = this.cursor.offsetCount();
				final int incr = backward ? bound - 1 : 1;
				this.curOffset = ( this.curOffset + incr ) % bound;
				this.cursor.setOffsetStep( this.curOffset, this.curStep );
			},
			modifyState
		) );
		this.modifyModes.add( new ModifyMode(
			"paintjob",
			backward -> {
				final int bound = this.paintable.paintjobCount();
				final int incr = backward ? bound - 1 : 1;
				this.curPaintjob = ( this.curPaintjob + incr ) % bound;
				this.paintable.setPaintjob( this.curPaintjob );
			},
			() -> IModifyState.NOT_SELECTED
		) );
	}
	
	protected final HashMap< IInput, Consumer< IInput > >
		handlers = new HashMap<>(),
		coHandlers = new HashMap<>();
	{
		final Consumer< IInput > switchModifyMode = key -> {
			final ModifyMode prevMode = this.modifyModes.removeFirst();
			this.modifyModes.add( prevMode );
			
			final ModifyMode newMode = this.modifyModes.getFirst();
			this.sendPlayerPrompt( I18n.format( newMode.notifyMsg ) );
			this.cursor.setModifyState( newMode.modifyState.get() );
		};
		this.handlers.put( Key.SELECT_TOGGLE, switchModifyMode );
		this.coHandlers.put( Key.SELECT_TOGGLE, switchModifyMode );
	}
	
	{
		final Consumer< IInput > loopPreviewModule = key -> {
			final boolean isPreviewReady = this.index() == -1;
			if ( !isPreviewReady ) { return; }
			
			// Discard modifications that has not been submitted.
			this.clearPrimary();
			final IModule< ? > base = this.primary.getInstalled( this.loc, this.locLen - 2 );
			
			// Loop player's inventory to find next preview module.
			final int slot = this.slot();
			final InventoryPlayer inv = MC.player.inventory;
			final int invSize = inv.getSizeInventory();
			final int incr = key == Key.SELECT_UP ? invSize : 2;
			
			final Supplier< Boolean > hasNext = () -> {
				this.previewInvSlot += incr;
				this.previewInvSlot %= invSize;
				this.previewInvSlot -= 1;
				return this.previewInvSlot != -1;
			};
			while ( hasNext.get() )
			{
				final ItemStack stack = inv.getStackInSlot( this.previewInvSlot );
				final IItem item = IItemTypeHost.getItemOrDefault( stack );
				final boolean isModule = item instanceof IModule< ? >;
				if ( !isModule ) { continue; }
				
				// Test if it is valid to install. Do not forget to copy before use.
				final IItem copiedItem = IItemTypeHost.getItem( stack.copy() );
				final IModule< ? > previewModule = ( IModule< ? > ) copiedItem;
				this.previewPredicate = base.tryInstall( slot, previewModule );
				if ( this.previewPredicate == NO_PREVIEW ) { continue; }
				
				// Current module is compatible for preview, re-fetch and setup it.
				this.fetchCursor();
				this.cursor.setOffsetStep( 0, this.curStep );
				this.curOffset = 0;
				
				final int paintjob = this.paintable.paintjob();
				this.curPaintjob = paintjob;
				this.oriPaintjob = paintjob;
				
				// Finally, re-fetch cursor and check hitbox conflict.
				this.fetchCursorForNewPosition();
				break;
			}
			
			final boolean noPreviewFound = this.previewInvSlot == -1;
			if ( noPreviewFound ) { this.setupIndicator(); }
		};
		this.coHandlers.put( Key.SELECT_UP, loopPreviewModule );
		this.coHandlers.put( Key.SELECT_DOWN, loopPreviewModule );
	}
	
	{
		final Consumer< IInput > adjustModule = key -> {
			final boolean nothingSelected = this.index() == -1 && this.previewInvSlot == -1;
			if ( nothingSelected ) { return; }
			
			final ModifyMode mode = this.modifyModes.getFirst();
			mode.handler.accept( key ==  Key.SELECT_LEFT );
			
			this.fetchCursorForNewPosition();
		};
		this.coHandlers.put( Key.SELECT_LEFT, adjustModule );
		this.coHandlers.put( Key.SELECT_RIGHT, adjustModule );
	}
	
	{
		final Consumer< IInput > submitModification = key -> {
			final boolean hasSelectedModule = this.index() != -1;
			if ( hasSelectedModule )
			{
				if ( !this.positionPredicate.okOrNotifyWhy() ) { return; }
				
				final boolean offsetChanged = this.curOffset != this.oriOffset;
				final boolean stepChanged = this.curStep != this.oriStep;
				if ( offsetChanged || stepChanged )
				{
					this.sendPacketToServer( new PacketModify(
						this.curOffset, this.curStep,
						this.loc, this.locLen
					) );
				}
				
				if (
					this.curPaintjob != this.oriPaintjob
					&& this.paintable.tryOfferOrNotifyWhy( this.curPaintjob, MC.player )
				) {
					this.sendPacketToServer( new PacketModify(
						this.curPaintjob,
						this.loc, this.locLen
					) );
				}
				
				// Clear selection after submit.
				this.loc[ this.locLen - 1 ] = -1;
				this.clearPrimary();
				this.setupIndicator();
			}
			else if (
				this.previewInvSlot != -1
				&& this.previewPredicate.okOrNotifyWhy()
				&& this.positionPredicate.okOrNotifyWhy()
			) {
				// Install preview module.
				this.sendPacketToServer( new PacketModify(
					this.previewInvSlot,
					this.curOffset, this.curStep,
					this.loc, this.locLen
				) );
				
				if(
					this.curPaintjob != this.oriPaintjob
					&& this.paintable.tryOfferOrNotifyWhy( this.curPaintjob, MC.player )
				) {
					// Notice that preview actually has been installed in client side.
					final int count = this.cursor.base().getInstalledCount( this.slot() );
					this.loc[ this.locLen - 1 ] = ( byte ) ( count - 1 );
					this.sendPacketToServer( new PacketModify(
						this.curPaintjob,
						this.loc, this.locLen
					) );
					this.loc[ this.locLen - 1 ] = -1;
				}
				
				this.clearPrimary();
				this.setupIndicator();
				this.previewInvSlot = -1; // Clear preview after installation
			}
		};
		this.coHandlers.put( Key.SELECT_CONFIRM, submitModification );
	}
	
	{
		final Consumer< IInput > uninstalledSelected = key -> {
			final boolean hasSelected = this.index() != -1 && this.locLen > 0;
			if ( !hasSelected ) { return; }
			
			this.sendPacketToServer( new PacketModify( this.loc, this.locLen ) );
			this.loc[ this.locLen - 1 ] = -1;
//			this.positionState = IModifyPredicate.OK; // FIXME: if this is needed?
			this.clearPrimary();
			this.setupIndicator();
		};
		this.coHandlers.put( Key.SELECT_CANCEL, uninstalledSelected );
	}
	
	{
		final Consumer< IInput > enterNextLayer = key -> {
			final boolean noSelectedModule = this.index() == -1;
			final boolean selectedModuleHasNoSlots = this.cursor.slotCount() == 0;
			if ( noSelectedModule || selectedModuleHasNoSlots ) { return; }
			
			if ( this.locLen >= this.loc.length )
			{
				// TODO
				return;
			}
			
			final int count = this.cursor.getInstalledCount( 0 );
			this.loc[ this.locLen ] = 0;
			this.loc[ this.locLen + 1 ] = ( byte ) Math.min( 0, count - 1 );
			this.locLen += 2;
			
			this.clearPrimaryAndSetupSelection();
		};
		this.handlers.put( Key.SELECT_CONFIRM, enterNextLayer );
	}
	
	{
		final Consumer< IInput > quitCurrentLayer = key -> {
			this.locLen -= 2;
			this.clearPrimaryAndSetupSelection();
		};
		this.handlers.put( Key.SELECT_CANCEL, quitCurrentLayer );
	}
	
	{
		final Consumer< IInput > switchSlot = key -> {
			final IModule< ? > base = this.cursor.base();
			final int slotCount = base.slotCount();
			if ( slotCount < 2 ) { return; }
			
			final int bound = slotCount;
			final int incr = key == Key.SELECT_UP ? bound - 1 : 1;
			final int next = ( this.slot() + incr ) % bound;
			final int installedCount = base.getInstalledCount( next );
			
			this.loc[ this.locLen - 2 ] = ( byte ) next;
			this.loc[ this.locLen - 1 ] = ( byte ) Math.min( 0, installedCount - 1 );
			
			this.clearPrimaryAndSetupSelection();
		};
		this.handlers.put( Key.SELECT_UP, switchSlot );
		this.handlers.put( Key.SELECT_DOWN, switchSlot );
	}
	
	{
		final Consumer< IInput > switchModule = key -> {
			final IModule< ? > base = this.cursor.base();
			
			// There will be an indicator or preview module installed if {#index() == -1}.
			final int offset = Math.min( 1, this.index() + 1 );
			final int bound = base.getInstalledCount( this.slot() ) + offset;
			final int incr = key == Key.SELECT_LEFT ? bound : 2;
			final int idx = ( this.index() + incr ) % bound - 1;
			this.loc[ this.locLen - 1 ] = ( byte ) idx;
			
			this.clearPrimaryAndSetupSelection();
		};
		this.handlers.put( Key.SELECT_LEFT, switchModule );
		this.handlers.put( Key.SELECT_RIGHT, switchModule );
	}
	
	protected OpModifyClient( IEquippedItem< ? > equipped ) {
		this( FORWARD_CONTROLLER, BACKWARD_CONTROLLER, equipped );
	}
	
	protected OpModifyClient(
		IOperationController forwardController,
		IOperationController backwardController,
		IEquippedItem< ? > equipped
	) {
		super( forwardController, backwardController );
		
		this.equipped = equipped;
	}
	
	public void handleInput( IInput key )
	{
		this.deferredModifyTask = () -> {
			( Key.ASSIST.down ? this.coHandlers : this.handlers ).get( key ).accept( key );
		};
	}
	
	@Override
	public IOperation launch( EntityPlayer player )
	{
		this.loc = MCWBClient.modifyLoc;
		this.refPlayerRotYaw = MC.player.rotationYaw;
		this.clearPrimaryAndSetupSelection();
		return super.launch( player );
	}
	
	@Override
	public IOperation toggle( EntityPlayer player )
	{
		final boolean fullyLaunched = this.prevProgress == 1F;
		this.refPlayerRotYaw = fullyLaunched ? MC.player.rotationYaw : this.refPlayerRotYaw;
		
		this.locLen = 0;
		this.clearPrimaryAndSetupSelection();
		return super.toggle( player );
	}
	
	@Override
	public IOperation tick( EntityPlayer player )
	{
		if ( super.tick( player ) == NONE ) { return NONE; }
		
		final boolean fullyLaunched = this.prevProgress == 1F;
		if ( !fullyLaunched ) { return this; }
		
		this.deferredModifyTask.run();
		this.deferredModifyTask = () -> { };
		return this;
	}
	
	@Override
	public IOperation terminate( EntityPlayer player )
	{
		this.endCallback();
		return NONE;
	}
	
	@Override
	protected IOperation onComplete( EntityPlayer player )
	{
		this.endCallback();
		return NONE;
	}
	
	@Override
	public IOperation onStackUpdate( IEquippedItem< ? > newEquipped, EntityPlayer player )
	{
		this.equipped = newEquipped;
		this.clearPrimary();

		final boolean hasSelected = this.index() != -1;
		final boolean hasPreview = this.previewInvSlot != -1;
		switch ( ( hasSelected ? 2 : 0 ) + ( hasPreview ? 1 : 0 ) )
		{
		case 1:
			final IModule< ? > base = this.primary.getInstalled( this.loc, this.locLen - 2 );
			final InventoryPlayer inv = MC.player.inventory;
			final ItemStack previewStack = inv.getStackInSlot( this.previewInvSlot ).copy();
			final IModule< ? > previewMod = ( IModule< ? > ) IItemTypeHost.getItem( previewStack );
			this.previewPredicate = base.tryInstall( this.slot(), previewMod );
			
		case 2:
		case 3:
			this.fetchCursor();
			this.cursor.setOffsetStep( this.curOffset, this.curStep );
			this.fetchCursor();
			this.paintable.setPaintjob( this.curPaintjob );
			this.fetchCursorForNewPosition();
		break;
		
		case 0: this.setupIndicator();
		}
		return this;
	}
	
	protected abstract void endCallback();
	
	protected abstract IModule< ? > replicateDelegatePrimary();
	
	protected final void clearPrimary() { this.primary = this.replicateDelegatePrimary(); }
	
	protected final void fetchCursor()
	{
		final boolean hasSelectedModule = this.index() != -1;
		this.cursor = (
			hasSelectedModule
			? this.primary.getInstalled( this.loc, this.locLen )
			: this.primary.getInstalled( this.loc, this.locLen - 2 )
				.getInstalled( this.slot(), this.previewPredicate.index() )
		);
		
		final boolean isCursorPaintable = this.cursor instanceof IPaintable;
		this.paintable = isCursorPaintable ? ( IPaintable ) this.cursor : PAINTABLE_PLACEHOLDER;
	}
	
	protected final void fetchCursorForNewPosition()
	{
		this.fetchCursor();
		this.positionPredicate = this.primary.checkHitboxConflict( this.cursor );
		final ModifyMode mode = this.modifyModes.getFirst();
		this.cursor.setModifyState( mode.modifyState.get() );
	}
	
	protected final void setupIndicator()
	{
		final IModule< ? > base = this.primary.getInstalled( this.loc, this.locLen - 2 );
		final IModule< ? > indicator = base.newModifyIndicator();
		final int slot = this.slot();
		
		indicator.setOffsetStep( 0, base.getSlot( slot ).maxStep() / 2 );
		indicator.setModifyState( IModifyState.SELECTED_OK );
		
		base.install( slot, indicator );
		this.cursor = indicator;
		this.paintable = PAINTABLE_PLACEHOLDER;
	}
	
	protected void clearPrimaryAndSetupSelection()
	{
		this.clearPrimary();
		this.previewInvSlot = -1;
		
		final boolean hasSelected = this.index() != -1;
		if ( hasSelected )
		{
			this.previewPredicate = NO_PREVIEW;
			this.positionPredicate = OK;
			
			this.fetchCursor();
			final ModifyMode mode = this.modifyModes.getFirst();
			this.cursor.setModifyState( mode.modifyState.get() );
			
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
	
	/**
	 * @return {@code 0} if is primary selected
	 */
	protected final int slot() { return this.locLen > 0 ? 0xFF & this.loc[ this.locLen - 2 ] : 0; }
	
	protected final int index()
	{
		final byte idx = this.locLen > 0 ? this.loc[ this.locLen - 1 ] : 0;
		return idx != -1 ? 0xFF & idx : -1;
	}
	
	protected class ModifyMode
	{
		protected final String notifyMsg;
		protected final Consumer< Boolean > handler;
		protected final Supplier< IModifyState > modifyState;
		
		protected ModifyMode(
			String name,
			Consumer< Boolean > handler,
			Supplier< IModifyState > modifyState
		) {
			this.notifyMsg = "mcwb.msg.modify_mode_" + name;
			this.handler = handler;
			this.modifyState = modifyState;
		}
	}
}
