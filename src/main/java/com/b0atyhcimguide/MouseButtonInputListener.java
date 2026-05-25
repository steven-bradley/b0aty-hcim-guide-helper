package com.b0atyhcimguide;

import java.awt.event.MouseEvent;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.input.MouseListener;

/**
 * Listens for configured mouse button presses and triggers step navigation.
 * Mouse4 and Mouse5 (extra buttons) can be bound to next/previous step actions.
 * Events matching a configured binding are consumed to prevent game interaction.
 */
@Singleton
public class MouseButtonInputListener implements MouseListener
{
    private static final int MOUSE4_BUTTON = 4;
    private static final int MOUSE5_BUTTON = 5;

    private final B0atyHcimGuideConfig config;
    private final StepTracker stepTracker;

    @Inject
    public MouseButtonInputListener(B0atyHcimGuideConfig config, StepTracker stepTracker)
    {
        this.config = config;
        this.stepTracker = stepTracker;
    }

    @Override
    public MouseEvent mousePressed(MouseEvent event)
    {
        MouseButton nextBinding = config.nextStepMouseButton();
        MouseButton prevBinding = config.prevStepMouseButton();

        int button = event.getButton();

        if (nextBinding != MouseButton.NONE && matchesBinding(button, nextBinding))
        {
            stepTracker.nextStep();
            event.consume();
            return event;
        }

        if (prevBinding != MouseButton.NONE && matchesBinding(button, prevBinding))
        {
            stepTracker.previousStep();
            event.consume();
            return event;
        }

        return event;
    }

    @Override
    public MouseEvent mouseReleased(MouseEvent event)
    {
        return event;
    }

    @Override
    public MouseEvent mouseClicked(MouseEvent event)
    {
        return event;
    }

    @Override
    public MouseEvent mouseEntered(MouseEvent event)
    {
        return event;
    }

    @Override
    public MouseEvent mouseExited(MouseEvent event)
    {
        return event;
    }

    @Override
    public MouseEvent mouseDragged(MouseEvent event)
    {
        return event;
    }

    @Override
    public MouseEvent mouseMoved(MouseEvent event)
    {
        return event;
    }

    private boolean matchesBinding(int button, MouseButton binding)
    {
        switch (binding)
        {
            case MOUSE4:
                return button == MOUSE4_BUTTON;
            case MOUSE5:
                return button == MOUSE5_BUTTON;
            default:
                return false;
        }
    }
}
