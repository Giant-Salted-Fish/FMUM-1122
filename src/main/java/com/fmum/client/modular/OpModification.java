package com.fmum.client.modular;

import com.fmum.client.Operation;
import com.fmum.client.OpProgressive;
import com.fmum.common.FMUM;
import com.fmum.common.module.MetaModular;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly( Side.CLIENT )
public final class OpModification extends OpProgressive
{
	public static final OpModification INSTANCE = new OpModification();
	
	public byte[] loc = null;
	public int locLen = 0;
	
	public ModifyMode mode = ModifyMode.ON_SLOT;
	
	public int streamIndex = 0;
	
	public int step = 0;
	public int offset = 0;
	public int dam = 0;
	
	public MetaModular baseMeta = null;
	
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
	
	private OpModification() { }
	
	public boolean isPaintMode() { return this.mode == ModifyMode.PAINTJOB; }
	
	public boolean selected() { return this.locLen == 0 || this.loc[ this.locLen - 1 ] != -1; }
	
	public boolean nonPrimarySelected() {
		return this.locLen > 0 && this.loc[ this.locLen - 1 ] != -1;
	}
	
	@Override
	public OpModification launch( ItemStack stack )
	{
		super.launch( stack );
		
		this.progressor = 0.1D;
		
		// Check if field "loc" has been initialized
		if( this.loc == null || this.loc.length != FMUM.MOD.maxLocLen )
			this.loc = new byte[ FMUM.MOD.maxLocLen ];
		this.locLen = 0;
		
		this.mode = ModifyMode.ON_SLOT;
		this.step
			= this.offset
			= this.dam
			= this.streamIndex
			= 0;
		this.baseMeta = null;
		this.clearPreviewSelected();
		this.clearInputStates();
		return this;
	}
	
	@Override
	public Operation tick( ItemStack stack )
	{
		if( super.tick( stack ) != NONE )
			return this.prevProgress < 0D && this.progressor < 0D ? NONE : this;
		
		// Buffer variables that will be frequently used
		final byte[] loc = this.loc;
		final int locLen = this.locLen;
		
		// TODO Get base module that is currently on
		
		// One tick is done, clear key input
		this.clearInputStates();
		return this;
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
		ON_SLOT( "onslot" ),
		ON_MODULE( "onmodule" ),
		PAINTJOB( "paintjob" );
		
		public final String notifyMessage;
		
		private ModifyMode( String translationKey ) {
			this.notifyMessage = "msg.modifymode" + translationKey;
		}
	}
}
