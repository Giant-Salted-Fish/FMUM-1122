package com.mcwb.client;

import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IAutowirePlayerChat
{
	public static final int CHAT_LINE_ID = 'F' + 'M' + 'U' + 'M';
	
	@SideOnly( Side.CLIENT )
	public default void sendPlayerMsg( String... messages )
	{
		final GuiNewChat chatGui = MCWBClient.MC.ingameGUI.getChatGUI();
		for ( String msg : messages )
		{
			final TextComponentString text = new TextComponentString( msg );
			chatGui.printChatMessage( text );
		}
	}
	
	@SideOnly( Side.CLIENT )
	public default void sendPlayerPrompt( String... messages )
	{
		final GuiNewChat chatGui = MCWBClient.MC.ingameGUI.getChatGUI();
		for ( int i = 0; i < messages.length; ++i )
		{
			final TextComponentString text = new TextComponentString( messages[ i ] );
			chatGui.printChatMessageWithOptionalDeletion( text, CHAT_LINE_ID + i );
		}
	}
}
