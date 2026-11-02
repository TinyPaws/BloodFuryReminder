package com.bloodfuryreminder;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BloodFuryReminderLauncher
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BloodFuryReminderPlugin.class);
		RuneLite.main(args);
	}
}