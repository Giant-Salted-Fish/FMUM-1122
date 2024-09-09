package com.fmum.module;

import com.fmum.FMUM;
import com.fmum.network.IPacket;
import com.fmum.network.PacketAdjustModule;
import com.fmum.network.PacketInstallModule;
import com.fmum.network.PacketRemoveModule;
import com.fmum.player.ChatBoxUtil;
import com.fmum.render.Texture;
import com.mojang.realmsclient.util.Pair;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Supplier;

@SideOnly( Side.CLIENT )
public class ModifySession
{
	protected Pair< ? extends IModule, Supplier< ? extends IModule > > cursor_ctx;
	protected LinkedList< Integer > cursor_location = new LinkedList<>();
	
	/**
	 * {@code null} if no previewing module. Negative if preview placeholder is
	 * installed. Otherwise, this gives the inventory index of the previewing
	 * module.
	 */
	protected Integer preview_inv_slot = null;
	
	protected LinkedList< IModifyMode > modify_modes = new LinkedList<>();
	
	public ModifySession( Supplier< ? extends IModule > root_factory )
	{
		this.cursor_ctx = Pair.of( root_factory.get(), root_factory );
		
		final PaintjobSwitchMode mode = new PaintjobSwitchMode();
		mode.resetWithCursor();
		this.modify_modes.add( mode );
	}
	
	public IModule getRoot()
	{
		IModule cursor = this.cursor_ctx.first();
		for (
			Optional< ? extends IModule > base = cursor.getBase();
			base.isPresent();
			base = cursor.getBase()
		) {
			cursor = base.get();
		}
		return cursor;
	}
	
	/**
	 * @return Whether there is inconsistency in the cursor context.
	 */
	public boolean refresh(
		Supplier< ? extends IModule > root_factory,
		IntFunction< Optional< IModule > > inv_pick
	) {
		final LinkedList< Integer > loc = this.cursor_location;
		final boolean is_root_mod = loc.isEmpty();
		if ( is_root_mod )
		{
			this.cursor_ctx = Pair.of( root_factory.get(), root_factory );
			return false;
		}
		
		IModule cursor = root_factory.get();
		final Iterator< Integer > itr = loc.iterator();
		while ( true )
		{
			final int slot_idx = itr.next();
			if ( slot_idx >= cursor.getSlotCount() ) {
				return true;
			}
			
			final int mod_idx = itr.next();
			final boolean has_next_layer = itr.hasNext();
			if ( has_next_layer )
			{
				if ( mod_idx >= cursor.countModuleInSlot( slot_idx ) ) {
					return true;
				}
				
				cursor = cursor.getInstalled( slot_idx, mod_idx );
				continue;
			}
			
			if ( this.preview_inv_slot == null )
			{
				if ( mod_idx >= cursor.countModuleInSlot( slot_idx ) ) {
					return true;
				}
				
				this.cursor_ctx = cursor.getModifyCursor( slot_idx, mod_idx, this._getCtx() );
				this.modify_modes.forEach( IModifyMode::onCursorRefresh );
			}
			else if ( this.preview_inv_slot >= 0 )
			{
				final Optional< IModule > mod = inv_pick.apply( this.preview_inv_slot );
				if ( !mod.isPresent() ) {
					return true;
				}
				
				final IModifyPreview< Integer > preview = cursor.tryInstall( slot_idx, mod.get() );
				if ( preview.getPreviewError().isPresent() ) {
					return true;
				}
				
				final int mod_inst_idx = preview.apply();
				loc.set( 0, mod_inst_idx );
				this.cursor_ctx = cursor.getModifyCursor( slot_idx, mod_inst_idx, this._getCtx() );
				this.modify_modes.forEach( IModifyMode::resetWithCursor );
			}
			else
			{
				loc.removeLast();
				this._setupPlaceholder( cursor, slot_idx );
			}
			return false;
		}
	}
	
	public void enterLayer()
	{
		final LinkedList< Integer > loc = this.cursor_location;
		final boolean no_selected_module = this.preview_inv_slot != null;
		if ( no_selected_module ) {
			return;
		}
		
		// TODO: Only apply for installing module?
//		final int modify_depth = loc.size() / 2;
//		final boolean reach_max_layer = modify_depth >= SyncConfig.max_module_depth;
//		if ( reach_max_layer ) {
//			return;
//		}
		
		final IModule cursor = this.cursor_ctx.first();
		final boolean module_has_not_slot = cursor.getSlotCount() == 0;
		if ( module_has_not_slot ) {
			return;
		}
		
		// Check pass. Restore module and use it as base.
		final boolean is_root = loc.isEmpty();
		final IModule base = is_root ? this.cursor_ctx.first() : this.cursor_ctx.second().get();
		
		loc.addLast( 0 );
		final int mod_count = base.countModuleInSlot( 0 );
		if ( mod_count > 0 )
		{
			this.cursor_ctx = base.getModifyCursor( 0, 0, this._getCtx() );
			loc.addLast( 0 );
			
			this.modify_modes.forEach( IModifyMode::resetWithCursor );
		}
		else {
			this._setupPlaceholder( base, 0 );
		}
	}
	
	public void quitLayer( Supplier< ? extends IModule > root_factory )
	{
		final LinkedList< Integer > loc = this.cursor_location;
		final boolean is_root_mod = loc.isEmpty();
		if ( is_root_mod ) {
			return;
		}
		
		// Restore cursor module.
		final IModule cursor = this.cursor_ctx.second().get();
		final IModule base = cursor.getBase().orElseThrow( IllegalStateException::new );
		if ( this.preview_inv_slot != null )
		{
			final int module_idx = loc.removeLast();
			final int slot_idx = loc.removeLast();
			base.tryRemove( slot_idx, module_idx ).apply();
			this.preview_inv_slot = null;
		}
		else
		{
			loc.removeLast();
			loc.removeLast();
		}
		
		// Update location and check if it is root module.
		if ( loc.isEmpty() )
		{
			this.cursor_ctx = Pair.of( base, root_factory );
			this.modify_modes.forEach( IModifyMode::resetWithCursor );
		}
		else
		{
			// Re-fetch cursor from its parent module.
			final IModule parent = base.getBase().orElseThrow( IllegalStateException::new );
			final int slot_idx = loc.get( loc.size() - 2 );
			final int mod_idx = loc.getLast();
			this.cursor_ctx = parent.getModifyCursor( slot_idx, mod_idx, this._getCtx() );
			this.modify_modes.forEach( IModifyMode::resetWithCursor );
		}
	}
	
	public void loopModifyMode()
	{
		final LinkedList< IModifyMode > modes = this.modify_modes;
		final IModifyMode ori = modes.removeFirst();
		modes.addLast( ori );
		
		final IModifyMode cur = Objects.requireNonNull( modes.getFirst() );
		final String key = "chat.modify_mode." + cur.getTranslationKey();
		ChatBoxUtil.addFixedPrompt( I18n.format( key ) );
	}
	
	public void loopSlot( boolean loop_next )
	{
		final LinkedList< Integer > loc = this.cursor_location;
		final boolean is_root_mod = loc.isEmpty();
		if ( is_root_mod ) {
			return;
		}
		
		final IModule cursor = this.cursor_ctx.first();
		final IModule base = cursor.getBase().orElseThrow( IllegalStateException::new );
		final int slot_count = base.getSlotCount();
		if ( slot_count < 2 ) {
			return;
		}
		
		// Restore cursor module.
		if ( this.preview_inv_slot != null )
		{
			base.tryRemove( loc.get( loc.size() - 2 ), loc.getLast() ).apply();
			this.preview_inv_slot = null;
		}
		else {
			this.cursor_ctx.second().get();
		}
		
		// Switch slot.
		loc.removeLast();  // Module index.
		final int slot_index = loc.removeLast();
		final int step = loop_next ? 1 : ( slot_count - 1 );
		final int next_slot = ( slot_index + step ) % slot_count;
		
		loc.addLast( next_slot );
		final int mod_count = base.countModuleInSlot( next_slot );
		if ( mod_count > 0 )
		{
			this.cursor_ctx = base.getModifyCursor( next_slot, 0, this._getCtx() );
			loc.addLast( 0 );
			
			this.modify_modes.forEach( IModifyMode::resetWithCursor );
		}
		else {
			this._setupPlaceholder( base, next_slot );
		}
	}
	
	public void loopModule( boolean loop_next )
	{
		final LinkedList< Integer > loc = this.cursor_location;
		final boolean is_root_mod = loc.isEmpty();
		if ( is_root_mod ) {
			return;
		}
		
		final IModule cursor = this.cursor_ctx.first();
		final IModule base = cursor.getBase().orElseThrow( IllegalStateException::new );
		final int slot_idx = loc.get( loc.size() - 2 );
		if ( this.preview_inv_slot != null )
		{
			final int mod_count = base.countModuleInSlot( slot_idx );
			final boolean has_module_in_slot = mod_count > 1;
			if ( has_module_in_slot )
			{
				final int mod_idx = loc.removeLast();
				base.tryRemove( slot_idx, mod_idx ).apply();
				this.preview_inv_slot = null;
				
				final int next_mod_idx = mod_idx % ( mod_count - 1 );
				this.cursor_ctx = base.getModifyCursor( slot_idx, next_mod_idx, this._getCtx() );
				loc.addLast( next_mod_idx );
				
				this.modify_modes.forEach( IModifyMode::resetWithCursor );
			}
			else if ( this.preview_inv_slot >= 0 )
			{
				final int mod_idx = loc.removeLast();
				base.tryRemove( slot_idx, mod_idx ).apply();
				this._setupPlaceholder( base, slot_idx );
			}
		}
		else
		{
			this.cursor_ctx.second().get();
			
			final int mod_idx = loc.removeLast();
			final int mod_count = base.countModuleInSlot( slot_idx );
			final int step = loop_next ? 1 : mod_count;
			final int next_mod_idx = ( mod_idx + step ) % ( mod_count + 1 );
			if ( next_mod_idx != mod_count )
			{
				this.cursor_ctx = base.getModifyCursor( slot_idx, next_mod_idx, this._getCtx() );
				loc.addLast( next_mod_idx );
				
				this.modify_modes.forEach( IModifyMode::resetWithCursor );
			}
			else {
				this._setupPlaceholder( base, slot_idx );
			}
		}
	}
	
	public void loopPreview( IntFunction< Iterator< Pair< Integer, IModule > > > itr_factory )
	{
		final boolean has_selected_module = this.preview_inv_slot == null;
		if ( has_selected_module ) {
			return;
		}
		
		// Uninstall current previewing module.
		final LinkedList< Integer > loc = this.cursor_location;
		final int mod_idx = loc.removeLast();
		final int slot_idx = loc.getLast();
		
		final IModule base = this.cursor_ctx.first().getBase().orElseThrow( IllegalAccessError::new );
		base.tryRemove( slot_idx, mod_idx ).apply();
		
		// Install first module that is compatible for preview.
		final int prev_idx = Math.max( -1, this.preview_inv_slot );
		final Iterator< Pair< Integer, IModule > > mod_itr = itr_factory.apply( prev_idx );
		while ( mod_itr.hasNext() )
		{
			final Pair< Integer, IModule > next = mod_itr.next();
			final IModule mod = next.second();
			final IModifyPreview< Integer > preview = base.tryInstall( slot_idx, mod );
			if ( preview.getPreviewError().isPresent() ) {
				continue;
			}
			
			// Pass check! Install it for preview.
			final int mod_inst_idx = preview.apply();
			loc.addLast( mod_inst_idx );
			this.preview_inv_slot = next.first();
			this.cursor_ctx = base.getModifyCursor( slot_idx, mod_inst_idx, this._getCtx() );
			this.modify_modes.forEach( IModifyMode::onPreviewSwitched );
			return;  // Careful! This is a return.
		}
		
		// No module is compatible for preview.
		this._setupPlaceholder( base, slot_idx );
	}
	
	public void loopChange( boolean loop_next )
	{
		final IModifyMode mode = this.modify_modes.getFirst();
		mode.loopChange( loop_next );
	}
	
	public void confirmChange()
	{
		final boolean is_previewing = this.preview_inv_slot != null;
		if ( is_previewing )
		{
			final boolean is_placeholder = this.preview_inv_slot < 0;
			if ( is_placeholder ) {
				return;
			}
			
			// Install previewing module.
			final byte[] loc = this._locToArr( this.cursor_location.size() - 1 );
			final IPacket packet = new PacketInstallModule( loc, this.preview_inv_slot );
			FMUM.NET.sendPacketC2S( packet );
		}
		
		this.modify_modes.forEach( IModifyMode::confirmChange );
		
		// Reset previewing module.
		this.loopModule( true );
	}
	
	public void removeModule()
	{
		final LinkedList< Integer > loc = this.cursor_location;
		final boolean is_root_mod = loc.isEmpty();
		if ( is_root_mod ) {
			return;
		}
		
		final boolean no_module_selected = this.preview_inv_slot != null;
		if ( no_module_selected ) {
			return;
		}
		
		// Send packet to remove module.
		final byte[] arr_loc = this._locToArr( loc.size() );
		final IPacket packet = new PacketRemoveModule( arr_loc );
		FMUM.NET.sendPacketC2S( packet );
		
		// Remove module from client side.
		final IModule cursor = this.cursor_ctx.second().get();
		final IModule base = cursor.getBase().orElseThrow( IllegalStateException::new );
		final int module_idx = loc.removeLast();
		final int slot_idx = loc.getLast();
		base.tryRemove( slot_idx, module_idx ).apply();
		this._setupPlaceholder( base, slot_idx );
	}
	
	protected void _setupPlaceholder( IModule base, int slot_idx )
	{
		final int idx = base.installPreviewPlaceholder( slot_idx, this._getCtx() );
		final IModule placeholder = base.getInstalled( slot_idx, idx );
		this.cursor_ctx = Pair.of( placeholder, () -> placeholder );
		this.preview_inv_slot = -idx - 1;
		this.cursor_location.addLast( idx );
	}
	
	protected IModifyContext _getCtx()
	{
		return tex -> Texture.GREEN;
	}
	
	protected byte[] _locToArr( int len )
	{
		final LinkedList< Integer > loc = this.cursor_location;
		final byte[] arr_loc = new byte[ len ];
		final Iterator< Integer > itr = loc.iterator();
		for ( int i = 0; i < len; i += 1 ) {
			arr_loc[ i ] = itr.next().byteValue();
		}
		return arr_loc;
	}
	
	
	protected interface IModifyMode
	{
		String getTranslationKey();
		
		void resetWithCursor();
		
		void onCursorRefresh();
		
		void onPreviewSwitched();
		
		void loopChange( boolean loop_next );
		
		void confirmChange();
	}
	
	
	protected class PaintjobSwitchMode implements IModifyMode
	{
		protected int ori_paintjob;
		protected int cur_paintjob;
		
		@Override
		public String getTranslationKey() {
			return "paintjob_switch";
		}
		
		@Override
		public void resetWithCursor()
		{
			final IModule cursor = ModifySession.this.cursor_ctx.first();
			final int paintjob = cursor.getPaintjobIdx();
			this.ori_paintjob = paintjob;
			this.cur_paintjob = paintjob;
		}
		
		@Override
		public void onCursorRefresh()
		{
			final IModule cursor = ModifySession.this.cursor_ctx.first();
			this.ori_paintjob = cursor.getPaintjobIdx();
			if ( this.cur_paintjob != this.ori_paintjob ) {
				this.cur_paintjob = cursor.trySetPaintjob( this.cur_paintjob ).apply();
			}
		}
		
		@Override
		public void onPreviewSwitched() {
			this.resetWithCursor();
		}
		
		@Override
		public void loopChange( boolean loop_next )
		{
			final IModule cursor = ModifySession.this.cursor_ctx.first();
			final int count = cursor.getPaintjobCount();
			final int incr = loop_next ? 1 : ( count - 1 );
			final int next = ( this.cur_paintjob + incr ) % count;
			if ( next != this.cur_paintjob ) {
				this.cur_paintjob = cursor.trySetPaintjob( next ).apply();
			}
		}
		
		@Override
		public void confirmChange()
		{
			if ( this.cur_paintjob == this.ori_paintjob ) {
				return;
			}
			
			final int len = ModifySession.this.cursor_location.size();
			final byte[] loc = ModifySession.this._locToArr( len );
			final IPacket packet = PacketAdjustModule.ofPaintjobSwitch( loc, this.cur_paintjob );
			FMUM.NET.sendPacketC2S( packet );
			
			this.ori_paintjob = this.cur_paintjob;
		}
	}
}
