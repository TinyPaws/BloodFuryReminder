
package com.bloodfuryreminder;

import com.google.inject.Provides;
import java.awt.Toolkit;
import javax.inject.Inject;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;

import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
        name = "Blood Fury Reminder",
        description = "Alerts when HP is high enough to conserve Blood Fury charges",
        tags = {"blood fury", "health", "hitpoints", "combat"}
)
public class BloodFuryReminderPlugin extends Plugin
{
    private static final int BLOOD_FURY_ID = 24780;

    @Inject
    private Client client;

    @Inject
    private BloodFuryReminderConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private BloodFuryFlashOverlay flashOverlay;

    private boolean alreadyAlerted;
    private boolean initialHpChecked;

    @Override
    protected void startUp()
    {
        overlayManager.add(flashOverlay);
        alreadyAlerted = false;
        initialHpChecked = false;
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(flashOverlay);
        alreadyAlerted = false;
        initialHpChecked = false;
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            resetReminder();
            return;
        }

        // Never monitor HP unless the Blood Fury is equipped.
        if (!isBloodFuryEquipped())
        {
            resetReminder();
            return;
        }

        int currentHp = client.getBoostedSkillLevel(Skill.HITPOINTS);
        int maximumHp = client.getRealSkillLevel(Skill.HITPOINTS);

        if (maximumHp <= 0)
        {
            return;
        }

        int hpPercentage = (currentHp * 100) / maximumHp;

        // First check after equipping: don't alert immediately.
        if (!initialHpChecked)
        {
            alreadyAlerted = hpPercentage >= config.hpThreshold();
            initialHpChecked = true;
            return;
        }

        if (hpPercentage >= config.hpThreshold())
        {
            if (!alreadyAlerted)
            {
                alertPlayer(hpPercentage);
                alreadyAlerted = true;
            }
        }
        else
        {
            alreadyAlerted = false;
        }
    }

    private void resetReminder()
    {
        alreadyAlerted = false;
        initialHpChecked = false;
    }

    private boolean isBloodFuryEquipped()
    {
        ItemContainer equipment =
                client.getItemContainer(InventoryID.EQUIPMENT);

        if (equipment == null)
        {
            return false;
        }

        Item amulet = equipment.getItem(
                EquipmentInventorySlot.AMULET.getSlotIdx()
        );

        return amulet != null && amulet.getId() == BLOOD_FURY_ID;
    }

    private void alertPlayer(int hp)
    {
        if (config.showChatMessage())
        {
            client.addChatMessage(
                    ChatMessageType.GAMEMESSAGE,
                    "",
                    "[Blood Fury Reminder] HP is " + hp
                            + "%! Consider switching off your Blood Fury.",
                    null
            );
        }

        if (config.flashScreen())
        {
            flashOverlay.triggerFlash();
        }

        if (config.playSound())
        {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    @Provides
    BloodFuryReminderConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(BloodFuryReminderConfig.class);
    }
}
