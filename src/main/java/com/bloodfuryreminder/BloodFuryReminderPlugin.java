package com.bloodfuryreminder;

import com.google.inject.Provides;
import java.awt.Toolkit;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;

@PluginDescriptor(
    name = "Blood Fury Reminder",
    description = "Alerts when HP is high enough to conserve Blood Fury charges",
    tags = {"blood fury", "health", "hitpoints", "combat"}
)
public class BloodFuryReminderPlugin extends Plugin
{
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
    private static final int BLOOD_FURY_ID = 24780;

    @Override
    protected void startUp()
    {
        overlayManager.add(flashOverlay);
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(flashOverlay);
        alreadyAlerted = false;
    }
    @Subscribe
    public void onGameTick(GameTick event)
    {
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            alreadyAlerted = false;
            initialHpChecked = false;
            return;
        }

        if (!isBloodFuryEquipped())
        {
            alreadyAlerted = false;
            initialHpChecked = false;
            return;
        }

        int currentHp = client.getBoostedSkillLevel(Skill.HITPOINTS);
        int maximumHp = client.getRealSkillLevel(Skill.HITPOINTS);

        if (maximumHp <= 0)
        {
            return;
        }

        int hpPercentage = (currentHp * 100) / maximumHp;

        // Check initial HP without triggering an alert
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

    private void alertPlayer(int hp)
    {
        client.addChatMessage(
            ChatMessageType.GAMEMESSAGE,
            "",
            "[Blood Fury Reminder] HP is " + hp
                + "%! Consider switching off your Blood Fury.",
            null

        );

        if (config.flashScreen())
        {
            flashOverlay.triggerFlash();
        }
        if (config.playSound())
        {
            Toolkit.getDefaultToolkit().beep();
        }
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

    @Provides
    BloodFuryReminderConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(BloodFuryReminderConfig.class);
    }
}
