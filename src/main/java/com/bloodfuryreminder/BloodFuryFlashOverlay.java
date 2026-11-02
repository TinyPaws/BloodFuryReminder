package com.bloodfuryreminder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
public class BloodFuryFlashOverlay extends Overlay
{
    private final Client client;
    private int framesRemaining;

    @Inject
    public BloodFuryFlashOverlay(Client client)
    {
        this.client = client;

        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
        setPriority(PRIORITY_HIGHEST);
    }

    public void triggerFlash()
    {
        framesRemaining = 150;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        Dimension dimensions = client.getRealDimensions();

        if (framesRemaining <= 0)
        {
            return null;
        }

        int frame = 150 - framesRemaining;

        // Alternate every 25 frames for a slower flash
        if ((frame / 25) % 2 == 0)
        {
            graphics.setColor(new Color(255, 0, 0, 75));
            graphics.fillRect(
                    0,
                    0,
                    dimensions.width,
                    dimensions.height
            );
        }

        framesRemaining--;

        return dimensions;
    }
}