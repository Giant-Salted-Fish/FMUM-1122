package com.fmum.player;

import com.mojang.realmsclient.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.stream.IntStream;

@SideOnly( Side.CLIENT )
public final class ChatBoxUtil
{
	private static final int CHAT_LINE_ID = 'F' + 'M' + 'U' + 'M';
	
	public static void addMessage( String... lines )
	{
		final Minecraft mc = Minecraft.getMinecraft();
		final GuiNewChat chat_gui = mc.ingameGUI.getChatGUI();
		Arrays.stream( lines )
			.map( TextComponentString::new )
			.forEachOrdered( chat_gui::printChatMessage );
	}
	
	public static void addFixedPrompt( String... lines )
	{
		final Minecraft mc = Minecraft.getMinecraft();
		final GuiNewChat chat_gui = mc.ingameGUI.getChatGUI();
		IntStream.range( 0, lines.length )
			.mapToObj( i -> Pair.of( new TextComponentString( lines[ i ] ), CHAT_LINE_ID + i ) )
			.forEachOrdered( p -> chat_gui.printChatMessageWithOptionalDeletion( p.first(), p.second() ) );
	}
	
	private ChatBoxUtil() { }
}
