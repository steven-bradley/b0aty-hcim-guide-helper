package com.b0atyhcimguide;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

/**
 * Overlay panel that displays the current guide step information,
 * navigation controls, and section selector.
 */
@Singleton
public class GuideOverlay extends OverlayPanel implements StepChangeListener
{
    private static final int PANEL_WIDTH = 200;
    private static final String NAV_PREV = "<< Prev";
    private static final String NAV_NEXT = "Next >>";

    private final B0atyHcimGuideConfig config;
    private final StepTracker stepTracker;
    private final GuideDataStore guideDataStore;

    private Rectangle prevButtonBounds = new Rectangle();
    private Rectangle nextButtonBounds = new Rectangle();

    @Inject
    public GuideOverlay(B0atyHcimGuideConfig config, StepTracker stepTracker, GuideDataStore guideDataStore)
    {
        this.config = config;
        this.stepTracker = stepTracker;
        this.guideDataStore = guideDataStore;

        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.HIGHEST);
        setPosition(OverlayPosition.TOP_LEFT);
        setMovable(true);
        setResizable(false);
        setPreferredSize(new Dimension(PANEL_WIDTH, 0));

        stepTracker.addStepChangeListener(this);
    }

    @Override
    public void onStepChanged(GuideStep newStep, int newIndex)
    {
        // Step change triggers re-render on next frame automatically
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.showOverlay())
        {
            return null;
        }

        GuideStep currentStep = stepTracker.getCurrentStep();
        if (currentStep == null)
        {
            panelComponent.getChildren().add(TitleComponent.builder()
                .text("B0aty HCIM Guide")
                .color(Color.YELLOW)
                .build());
            panelComponent.getChildren().add(LineComponent.builder()
                .left("No guide data loaded")
                .build());
            return super.render(graphics);
        }

        int currentIndex = stepTracker.getCurrentStepIndex();
        int totalSteps = guideDataStore.getTotalSteps();

        // Section name as title
        panelComponent.getChildren().add(TitleComponent.builder()
            .text(currentStep.getSection())
            .color(Color.YELLOW)
            .build());

        // Step number / total
        panelComponent.getChildren().add(LineComponent.builder()
            .left("Step:")
            .right((currentIndex + 1) + " / " + totalSteps)
            .build());

        // Instruction text
        panelComponent.getChildren().add(LineComponent.builder()
            .left(currentStep.getInstruction())
            .build());

        // Navigation controls
        panelComponent.getChildren().add(LineComponent.builder()
            .left(NAV_PREV)
            .leftColor(Color.WHITE)
            .right(NAV_NEXT)
            .rightColor(Color.WHITE)
            .build());

        // Section selector display
        panelComponent.getChildren().add(LineComponent.builder()
            .left("Section:")
            .leftColor(Color.GRAY)
            .right(currentStep.getSection())
            .rightColor(Color.CYAN)
            .build());

        Dimension rendered = super.render(graphics);

        // Calculate button bounds for click handling after render
        if (rendered != null)
        {
            updateButtonBounds(graphics, rendered);
        }

        return rendered;
    }

    /**
     * Handles mouse click events on the overlay panel.
     * Returns true if the click was consumed by a navigation action.
     */
    public boolean handleClick(Point clickPoint)
    {
        if (!config.showOverlay())
        {
            return false;
        }

        Rectangle bounds = getBounds();
        if (bounds == null || !bounds.contains(clickPoint))
        {
            return false;
        }

        // Translate click to panel-relative coordinates
        Point relative = new Point(clickPoint.x - bounds.x, clickPoint.y - bounds.y);

        if (prevButtonBounds.contains(relative))
        {
            stepTracker.previousStep();
            return true;
        }

        if (nextButtonBounds.contains(relative))
        {
            stepTracker.nextStep();
            return true;
        }

        return false;
    }

    /**
     * Handles section selection by name.
     * Called when the player selects a section from the section list.
     */
    public void selectSection(String sectionName)
    {
        stepTracker.jumpToSection(sectionName);
    }

    /**
     * Returns the list of available section names for the section selector.
     */
    public List<String> getSectionNames()
    {
        return guideDataStore.getSectionNames();
    }

    private void updateButtonBounds(Graphics2D graphics, Dimension panelSize)
    {
        FontMetrics fm = graphics.getFontMetrics();
        int lineHeight = fm.getHeight();

        // Navigation line is approximately the 4th line from top
        // Estimate y position based on panel layout
        int navLineY = panelSize.height - (2 * lineHeight) - 4;
        int halfWidth = panelSize.width / 2;

        prevButtonBounds = new Rectangle(0, navLineY, halfWidth, lineHeight);
        nextButtonBounds = new Rectangle(halfWidth, navLineY, halfWidth, lineHeight);
    }

    /**
     * Cleans up listener registration.
     */
    public void shutdown()
    {
        stepTracker.removeStepChangeListener(this);
    }
}
