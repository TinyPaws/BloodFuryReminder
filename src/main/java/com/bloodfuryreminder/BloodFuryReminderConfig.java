package com.bloodfuryreminder;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("com/bloodfuryreminder")
public interface BloodFuryReminderConfig extends Config
{
    @Range(min = 1, max = 100)
    @ConfigItem(
        keyName = "hpThreshold",
        name = "HP alert threshold",
        description = "Hitpoints percentage at which to show the reminder"
    )
    default int hpThreshold()
    {
        return 90;
    }

    @ConfigItem(
        keyName = "playSound",
        name = "Play alert sound",
        description = "Play a sound when the HP threshold is reached"
    )
    default boolean playSound()
    {
        return true;
    }

    @ConfigItem(
            keyName = "flashScreen",
            name = "Flash screen",
            description = "Briefly flash the screen when the HP threshold is reached"
    )
    default boolean flashScreen()
    {
        return false;
    }

    @ConfigItem(
            keyName = "showChatMessage",
            name = "Show chat message",
            description = "Show a chat message when the HP threshold is reached"
    )
    default boolean showChatMessage()
    {
        return true;
    }
}
