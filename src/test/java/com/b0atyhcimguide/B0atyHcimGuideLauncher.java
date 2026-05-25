package com.b0atyhcimguide;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class B0atyHcimGuideLauncher
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(B0atyHcimGuidePlugin.class);
		RuneLite.main(args);
	}
}
