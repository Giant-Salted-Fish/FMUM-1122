package com.mcwb.client;

import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IAutowirePlayerChat
{
	public static final int CHAT_LINE_ID = 'F' + 'M' + 'U' + 'M';
	
	@SideOnly( Side.CLIENT )
	public default void sendPlayerMsg( String... msg )
	{
		for( String s : msg )
			MCWBClient.MC.ingameGUI.getChatGUI().printChatMessage( new TextComponentString( s ) );
	}
	
	@SideOnly( Side.CLIENT )
	public default void sendPlayerPrompt( String... msg )
	{
		for( int i = 0; i < msg.length; ++i )
			MCWBClient.MC.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(
				new TextComponentString( msg[ i ] ),
				CHAT_LINE_ID + i
			);
	}
}
