package com.fmum.client.input;

import com.fmum.common.load.IContentProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

/**
 * This type of key bind can be used to proxy the vanilla {@link KeyBinding} in {@link Minecraft}.
 * 
 * @author Giant_Salted_Fish
 */
@SideOnly( Side.CLIENT )
public abstract class VanillaKeyBindProxy extends KeyBind
{
	protected VanillaKeyBindProxy(
		String name,
		IContentProvider provider,
		@Nullable String category,
		KeyBinding target
	) {
		this.name = name;
		this.provider = provider;
		this.category = category;
		
		IKeyBind.REGISTRY.regis( this );
		
		this.keyBind = target;
		this.keyCode = target.getKeyCode();
		InputHandler.GLOBAL_KEYS.add( this );
	}
	
	@Override
	public void restoreMcKeyBind() { }
	
	@Override
	public boolean clearMcKeyBind()
	{
		this.keyCode = this.keyBind.getKeyCode();
		return false;
	}
	
	@Override
	public void setKeyCode( int code ) { } // Always get its key code from #keyBind.
	
	@Override
	protected void onFire() {
		while ( this.keyBind.isPressed() ) { this.onAction(); }
	}
	
	@Override
	protected void onRelease() { } // Avoid normal notification.
	
	/**
	 * Do what you need to proxy the target vanilla key bind.
	 */
	protected abstract void onAction();
}
