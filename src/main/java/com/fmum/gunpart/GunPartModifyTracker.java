package com.fmum.gunpart;

import com.fmum.FMUM;
import com.fmum.module.IModule;
import com.fmum.module.ModifyTracker;
import com.fmum.network.IPacket;
import com.fmum.network.PacketAdjustModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.LinkedList;
import java.util.function.Supplier;

@SideOnly( Side.CLIENT )
public class GunPartModifyTracker extends ModifyTracker
{
	public GunPartModifyTracker( Supplier< ? extends IModule > root_factory )
	{
		super( root_factory );
		
		OffsetAdjustMode offset_mode = new OffsetAdjustMode();
		offset_mode.resetWithCursor();
		this.modify_modes.addFirst( offset_mode );
		
		StepAdjustMode step_mode = new StepAdjustMode();
		step_mode.resetWithCursor();
		this.modify_modes.addFirst( step_mode );
	}
	
	
	protected class StepAdjustMode implements IModifyMode
	{
		protected int ori_step;
		protected int cur_step;
		
		@Override
		public String getTranslationKey() {
			return "step_adjust";
		}
		
		public void resetWithCursor()
		{
			final IGunPart cursor = ( IGunPart ) GunPartModifyTracker.this.cursor_ctx.first();
			final int step = cursor.getStep();
			this.ori_step = step;
			this.cur_step = step;
		}
		
		public void onCursorRefresh()
		{
			final IGunPart cursor = ( IGunPart ) GunPartModifyTracker.this.cursor_ctx.first();
			this.ori_step = cursor.getStep();
			if ( this.cur_step != this.ori_step )
			{
				final int offset = cursor.getOffset();
				this.cur_step = cursor.trySetOffsetAndStep( offset, this.cur_step ).apply().second();
			}
		}
		
		@Override
		public void onPreviewSwitched() {
			this.onCursorRefresh();
		}
		
		@Override
		public void loopChange( boolean loop_next )
		{
			final LinkedList< Integer > loc = GunPartModifyTracker.this.cursor_location;
			final boolean is_root_mod = loc.isEmpty();
			if ( is_root_mod ) {
				return;
			}
			
			final IGunPart cursor = ( IGunPart ) GunPartModifyTracker.this.cursor_ctx.first();
			final IGunPart base = cursor.getBase().orElseThrow( IllegalStateException::new );
			final int slot_idx = loc.get( 1 );
			final int count = base.getStepCount( slot_idx );
			
			// Move upon player rotation to satisfy human intuition.
			final EntityPlayerSP player = Minecraft.getMinecraft().player;
			final float raw_rot_yaw = 90.0F + player.rotationYaw;
			final float rot_yaw = ( raw_rot_yaw % 360.0F + 360.0F ) % 360.0F;
			final int incr = loop_next != ( rot_yaw < 180.0F ) ? 1 : ( count - 1 );
			final int next = ( this.cur_step + incr ) % count;
			if ( next != this.cur_step )
			{
				final int offset = cursor.getOffset();
				this.cur_step = cursor.trySetOffsetAndStep( offset, next ).apply().second();
			}
		}
		
		@Override
		public void confirmChange()
		{
			if ( this.cur_step == this.ori_step ) {
				return;
			}
			
			final int len = GunPartModifyTracker.this.cursor_location.size();
			final byte[] loc = GunPartModifyTracker.this._locToArr( len );
			final IPacket packet = PacketAdjustModule.ofStepAdjust( loc, this.cur_step );
			FMUM.NET.sendPacketC2S( packet );
			
			this.ori_step = this.cur_step;
		}
	}
	
	protected class OffsetAdjustMode implements IModifyMode
	{
		protected int ori_offset;
		protected int cur_offset;
		
		@Override
		public String getTranslationKey() {
			return "offset_adjust";
		}
		
		@Override
		public void resetWithCursor()
		{
			final IGunPart cursor = ( IGunPart ) GunPartModifyTracker.this.cursor_ctx.first();
			final int offset = cursor.getOffset();
			this.ori_offset = offset;
			this.cur_offset = offset;
		}
		
		@Override
		public void onCursorRefresh()
		{
			final IGunPart cursor = ( IGunPart ) GunPartModifyTracker.this.cursor_ctx.first();
			this.ori_offset = cursor.getOffset();
			if ( this.cur_offset != this.ori_offset )
			{
				final int step = cursor.getStep();
				this.cur_offset = cursor.trySetOffsetAndStep( this.cur_offset, step ).apply().first();
			}
		}
		
		@Override
		public void onPreviewSwitched() {
			this.resetWithCursor();
		}
		
		@Override
		public void loopChange( boolean loop_next )
		{
			final IGunPart cursor = ( IGunPart ) GunPartModifyTracker.this.cursor_ctx.first();
			final int count = cursor.getOffsetCount();
			final int incr = loop_next ? 1 : ( count - 1 );
			final int next = ( this.cur_offset + incr ) % count;
			if ( next != this.cur_offset )
			{
				final int step = cursor.getStep();
				this.cur_offset = cursor.trySetOffsetAndStep( next, step ).apply().first();
			}
		}
		
		@Override
		public void confirmChange()
		{
			if ( this.cur_offset == this.ori_offset ) {
				return;
			}
			
			final int len = GunPartModifyTracker.this.cursor_location.size();
			final byte[] loc = GunPartModifyTracker.this._locToArr( len );
			final IPacket packet = PacketAdjustModule.ofOffsetAdjust( loc, this.cur_offset );
			FMUM.NET.sendPacketC2S( packet );
			
			this.ori_offset = this.cur_offset;
		}
	}
}
