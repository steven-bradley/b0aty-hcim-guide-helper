package com.b0atyhcimguide;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

/**
 * Manages current step state and navigation logic for the B0aty HCIM Guide.
 * Persists the current step index via ConfigManager and fires change events
 * when the step changes.
 */
@Slf4j
@Singleton
public class StepTracker
{
    private static final String CONFIG_GROUP = "b0atyhcimguide";
    private static final String CURRENT_STEP_KEY = "currentStep";

    private final GuideDataStore guideDataStore;
    private final ConfigManager configManager;
    private final List<StepChangeListener> listeners = new ArrayList<>();

    private int currentStepIndex;

    @Inject
    public StepTracker(GuideDataStore guideDataStore, ConfigManager configManager)
    {
        this.guideDataStore = guideDataStore;
        this.configManager = configManager;
    }

    /**
     * Initializes the step tracker by loading the persisted step index.
     * Must be called after GuideDataStore.load().
     */
    public void initialize()
    {
        int totalSteps = guideDataStore.getTotalSteps();
        if (totalSteps == 0)
        {
            currentStepIndex = 0;
            return;
        }

        int persisted = loadPersistedStep();
        currentStepIndex = clamp(persisted, 0, totalSteps - 1);

        if (persisted != currentStepIndex)
        {
            log.warn("Persisted step index {} out of range, clamped to {}", persisted, currentStepIndex);
            persistStep();
        }
    }

    /**
     * Returns the current guide step, or null if no steps are loaded.
     */
    public GuideStep getCurrentStep()
    {
        return guideDataStore.getStep(currentStepIndex);
    }

    /**
     * Returns the current step index.
     */
    public int getCurrentStepIndex()
    {
        return currentStepIndex;
    }

    /**
     * Advances to the next step. If already on the last step, stays on the last step.
     */
    public void nextStep()
    {
        int totalSteps = guideDataStore.getTotalSteps();
        if (totalSteps == 0)
        {
            return;
        }

        int newIndex = Math.min(currentStepIndex + 1, totalSteps - 1);
        if (newIndex != currentStepIndex)
        {
            setStep(newIndex);
        }
    }

    /**
     * Returns to the previous step. If already on the first step, stays on the first step.
     */
    public void previousStep()
    {
        int totalSteps = guideDataStore.getTotalSteps();
        if (totalSteps == 0)
        {
            return;
        }

        int newIndex = Math.max(currentStepIndex - 1, 0);
        if (newIndex != currentStepIndex)
        {
            setStep(newIndex);
        }
    }

    /**
     * Jumps to the specified step index. The index is clamped to valid bounds.
     *
     * @param index the target step index
     */
    public void jumpToStep(int index)
    {
        int totalSteps = guideDataStore.getTotalSteps();
        if (totalSteps == 0)
        {
            return;
        }

        int newIndex = clamp(index, 0, totalSteps - 1);
        if (newIndex != currentStepIndex)
        {
            setStep(newIndex);
        }
    }

    /**
     * Jumps to the first step of the specified section.
     * If the section is not found, no change occurs.
     *
     * @param sectionName the section to jump to
     */
    public void jumpToSection(String sectionName)
    {
        int index = guideDataStore.getFirstStepOfSection(sectionName);
        if (index >= 0)
        {
            if (index != currentStepIndex)
            {
                setStep(index);
            }
        }
        else
        {
            log.warn("Section not found: {}", sectionName);
        }
    }

    /**
     * Registers a listener to be notified when the current step changes.
     *
     * @param listener the listener to add
     */
    public void addStepChangeListener(StepChangeListener listener)
    {
        if (listener != null && !listeners.contains(listener))
        {
            listeners.add(listener);
        }
    }

    /**
     * Removes a previously registered step change listener.
     *
     * @param listener the listener to remove
     */
    public void removeStepChangeListener(StepChangeListener listener)
    {
        listeners.remove(listener);
    }

    private void setStep(int newIndex)
    {
        currentStepIndex = newIndex;
        persistStep();
        fireStepChanged();
    }

    private void persistStep()
    {
        configManager.setConfiguration(CONFIG_GROUP, CURRENT_STEP_KEY, currentStepIndex);
    }

    private int loadPersistedStep()
    {
        String value = configManager.getConfiguration(CONFIG_GROUP, CURRENT_STEP_KEY);
        if (value == null)
        {
            return 0;
        }
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            log.warn("Invalid persisted step value: {}", value);
            return 0;
        }
    }

    private void fireStepChanged()
    {
        GuideStep step = getCurrentStep();
        for (StepChangeListener listener : listeners)
        {
            try
            {
                listener.onStepChanged(step, currentStepIndex);
            }
            catch (Exception e)
            {
                log.error("Error in step change listener", e);
            }
        }
    }

    private static int clamp(int value, int min, int max)
    {
        return Math.max(min, Math.min(value, max));
    }
}
