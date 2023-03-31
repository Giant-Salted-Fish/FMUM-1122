package com.mcwb.client;

import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IAutowirePlayerChat
{
	public static final int CHAT_LINE_ID = 'F' + 'M' + 'U' + 'M';
	
	@SideOnly( Side.CLIENT )
	public default void sendPlayerMsg( String... msg )
	{
		final GuiNewChat chatGui = MCWBClient.MC.ingameGUI.getChatGUI();
		for ( String s : msg ) chatGui.printChatMessage( new TextComponentString( s ) );
	}
	
	@SideOnly( Side.CLIENT )
	public default void sendPlayerPrompt( String... msg )
	{
		final GuiNewChat chatGui = MCWBClient.MC.ingameGUI.getChatGUI();
		for ( int i = 0; i < msg.length; ++i )
		{
			chatGui.printChatMessageWithOptionalDeletion(
				new TextComponentString( msg[ i ] ),
				CHAT_LINE_ID + i
			);
		}
	}
}
