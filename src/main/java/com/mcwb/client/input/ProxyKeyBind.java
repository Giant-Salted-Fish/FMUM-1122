package com.mcwb.client.input;

import javax.annotation.Nullable;

import com.mcwb.common.pack.IContentProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This type of key bind can be used to proxy the vanilla {@link KeyBinding} of {@link Minecraft}
 * 
 * @author Giant_Salted_Fish
 */
@SideOnly( Side.CLIENT )
public abstract class ProxyKeyBind extends KeyBind
{
	protected ProxyKeyBind(
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
	
	// Always get its key code from #keyBind
	@Override
	public void $keyCode( int code ) { }
	
	@Override
	protected void onFire() { while( this.keyBind.isPressed() ) this.doProxyStuff(); }
	
	@Override
	protected void onRelease() { } // Avoid normal notification
	
	/**
	 * Do what you need to proxy the target vanilla key bind
	 */
	protected abstract void doProxyStuff();
}
