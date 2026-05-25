package com.b0atyhcimguide;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;

/**
 * Loads and provides access to the embedded B0aty HCIM Guide V3 data.
 */
@Slf4j
@Singleton
public class GuideDataStore {
    private static final String GUIDE_STEPS_INDEX = "/guide_steps/index.txt";

    private List<GuideStep> steps = new ArrayList<>();
    private List<String> sectionNames = new ArrayList<>();
    private Map<String, Integer> sectionFirstStepIndex = new LinkedHashMap<>();

    @Inject
    public GuideDataStore() {
    }

    /**
     * Loads guide data from multiple JSON files listed in the index.
     * Each file in guide_steps/ contains an array of GuideStep objects.
     * Files are loaded in the order listed in index.txt.
     * Must be called during plugin startup.
     */
    public void load() {
        try {
            List<String> fileNames = loadIndex();
            if (fileNames.isEmpty()) {
                log.error("Guide steps index is empty or not found");
                return;
            }

            Gson gson = new GsonBuilder()
                .registerTypeAdapter(WorldPoint.class, new WorldPointDeserializer())
                .create();

            Type listType = new TypeToken<List<GuideStep>>() {}.getType();
            List<GuideStep> allSteps = new ArrayList<>();

            for (String fileName : fileNames) {
                String resourcePath = "/guide_steps/" + fileName;
                try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
                    if (is == null) {
                        log.warn("Guide step file not found: {}", resourcePath);
                        continue;
                    }

                    List<GuideStep> loaded = gson.fromJson(
                        new InputStreamReader(is, StandardCharsets.UTF_8), listType);

                    if (loaded != null && !loaded.isEmpty()) {
                        allSteps.addAll(loaded);
                    }
                }
            }

            if (allSteps.isEmpty()) {
                log.error("No guide steps loaded from any file");
                return;
            }

            this.steps = allSteps;
            buildSectionIndex();
            log.info("Loaded {} guide steps across {} sections from {} files",
                steps.size(), sectionNames.size(), fileNames.size());
        } catch (Exception e) {
            log.error("Failed to parse guide data", e);
            this.steps = new ArrayList<>();
            this.sectionNames = new ArrayList<>();
            this.sectionFirstStepIndex = new LinkedHashMap<>();
        }
    }

    private List<String> loadIndex() {
        List<String> fileNames = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream(GUIDE_STEPS_INDEX)) {
            if (is == null) {
                log.error("Guide steps index not found: {}", GUIDE_STEPS_INDEX);
                return fileNames;
            }
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    fileNames.add(line);
                }
            }
        } catch (Exception e) {
            log.error("Failed to read guide steps index", e);
        }
        return fileNames;
    }

    private void buildSectionIndex() {
        sectionNames = new ArrayList<>();
        sectionFirstStepIndex = new LinkedHashMap<>();

        for (int i = 0; i < steps.size(); i++) {
            String section = steps.get(i).getSection();
            if (section != null && !sectionFirstStepIndex.containsKey(section)) {
                sectionNames.add(section);
                sectionFirstStepIndex.put(section, i);
            }
        }
    }

    /**
     * Returns all guide steps.
     */
    public List<GuideStep> getAllSteps() {
        return steps;
    }

    /**
     * Returns the step at the given index, or null if out of bounds.
     */
    public GuideStep getStep(int index) {
        if (index < 0 || index >= steps.size()) {
            return null;
        }
        return steps.get(index);
    }

    /**
     * Returns the total number of steps.
     */
    public int getTotalSteps() {
        return steps.size();
    }

    /**
     * Returns the distinct section names in first-appearance order.
     */
    public List<String> getSectionNames() {
        return sectionNames;
    }

    /**
     * Returns the index of the first step in the given section, or -1 if not found.
     */
    public int getFirstStepOfSection(String sectionName) {
        Integer index = sectionFirstStepIndex.get(sectionName);
        return index != null ? index : -1;
    }
}
