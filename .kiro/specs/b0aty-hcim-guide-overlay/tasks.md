# Implementation Plan: B0aty HCIM Guide Overlay

## Overview

Implement a RuneLite plugin that displays B0aty's HCIM Guide V3 as an in-game overlay with step navigation, entity highlighting, world map markers, and minimap indicators. The implementation follows standard RuneLite plugin architecture with Guice dependency injection, event subscriptions, and overlay rendering.

## Tasks

- [x] 1. Set up project structure, data models, and enums
  - [x] 1.1 Create the plugin package structure and data model classes
    - Create package `com.b0atyhcimguide`
    - Implement `EntityType` enum (NPC, OBJECT, GROUND_ITEM, INVENTORY_ITEM)
    - Implement `MouseButton` enum (NONE, MOUSE4, MOUSE5)
    - Implement `EntityReference` data class with `type` and `gameId` fields
    - Implement `GuideStep` data class with `stepNumber`, `section`, `instruction`, `entities`, and `location` fields
    - Implement `StepChangeListener` functional interface
    - _Requirements: 1.2, 1.3_

  - [ ]* 1.2 Write property test for guide data structural completeness
    - **Property 1: Guide data structural completeness**
    - **Validates: Requirements 1.2, 1.3**
    - Use jqwik to generate arbitrary `GuideStep` instances and verify all structural invariants hold

- [x] 2. Implement GuideDataStore and embedded guide data
  - [x] 2.1 Create the GuideDataStore class and guide_data.json resource
    - Implement `GuideDataStore` with Gson-based JSON loading from classpath resource
    - Implement `getAllSteps()`, `getStep(int index)`, `getTotalSteps()`, `getSectionNames()`, `getFirstStepOfSection(String)`
    - Create initial `guide_data.json` with at least a representative subset of B0aty HCIM Guide V3 steps
    - Handle parse errors gracefully (log error, return empty state)
    - _Requirements: 1.1, 1.2, 1.3_

  - [ ] 2.2 Write property test for section list completeness
    - **Property 9: Section list completeness**
    - **Validates: Requirements 7.1**
    - Verify `getSectionNames()` returns exactly the distinct section names in first-appearance order

- [x] 3. Implement B0atyHcimGuideConfig
  - [x] 3.1 Create the config interface
    - Implement `B0atyHcimGuideConfig` extending `Config`
    - Define all config items: `highlightColor`, `showOverlay`, `enableHighlighting`, `enableWorldMap`, `currentStep`, `nextStepMouseButton`, `prevStepMouseButton`
    - Use `@ConfigGroup("b0atyhcimguide")` annotation
    - _Requirements: 3.5, 3.6, 3.7, 6.1, 6.2, 6.3, 6.4, 6.5_

- [x] 4. Implement StepTracker with navigation and persistence
  - [x] 4.1 Create the StepTracker class
    - Implement `getCurrentStep()`, `getCurrentStepIndex()`, `nextStep()`, `previousStep()`, `jumpToStep(int)`, `jumpToSection(String)`
    - Implement boundary clamping (first step stays at 0, last step stays at max)
    - Persist current step index via `ConfigManager` on every change
    - Load persisted step on initialization, clamp if out of range
    - Implement listener registration and fire step change events
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 7.2_

  - [x] 4.2 Write property test for step navigation correctness
    - **Property 4: Step navigation correctness**
    - **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.8, 3.9**
    - For any index i, verify nextStep produces min(i+1, total-1) and previousStep produces max(i-1, 0)

  - [x] 4.3 Write property test for step persistence round trip
    - **Property 5: Step persistence round trip**
    - **Validates: Requirements 3.5**
    - Verify persisting and reading back a step index returns the same value

  - [x] 4.4 Write property test for reset progress
    - **Property 8: Reset progress sets step correctly**
    - **Validates: Requirements 6.5**
    - Verify jumpToStep(n) results in currentStepIndex == n for all valid n

  - [x] 4.5 Write property test for jump to section
    - **Property 10: Jump to section lands on first step**
    - **Validates: Requirements 7.2**
    - Verify jumpToSection sets index to the first step of that section

- [ ] 5. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [-] 6. Implement GuideOverlay panel
  - [x] 6.1 Create the GuideOverlay class
    - Extend `OverlayPanel` with `ABOVE_SCENE` layer, `DYNAMIC` priority, `MOVEABLE` position
    - Render current section name, step number/total, and instruction text
    - Render next/previous navigation buttons (clickable panel components)
    - Render section selector dropdown listing all sections
    - Respect `showOverlay` config toggle (return null from render when disabled)
    - Subscribe to step changes to trigger re-render
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 7.1, 7.3_

  - [x] 6.2 Write property test for overlay renders current step data
    - **Property 2: Overlay renders current step data**
    - **Validates: Requirements 2.1, 2.3, 7.3**
    - Verify overlay output contains step number, section name, and instruction text

  - [x] 6.3 Write property test for overlay visibility respects configuration
    - **Property 3: Overlay visibility respects configuration**
    - **Validates: Requirements 2.5**
    - Verify overlay renders iff showOverlay is true

- [x] 7. Implement HighlightOverlay for entity highlighting
  - [x] 7.1 Create the HighlightOverlay class
    - Extend `Overlay` with `ABOVE_SCENE` layer
    - Highlight NPCs via `modelOutlineRenderer` with configured color
    - Highlight objects via `modelOutlineRenderer` with configured color
    - Highlight ground items with colored tile indicator
    - Highlight inventory items with colored box around slot
    - Maintain a set of current entity IDs partitioned by type, updated on step change
    - Respect `enableHighlighting` config toggle
    - Clear highlights when step changes (remove old, apply new)
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

  - [x] 7.2 Write property test for highlight set matches current step
    - **Property 6: Highlight set matches current step entities**
    - **Validates: Requirements 4.1, 4.2, 4.3, 4.4, 4.5**
    - Verify highlighted entity IDs exactly match current step's entity references

- [x] 8. Implement MinimapOverlay
  - [x] 8.1 Create the MinimapOverlay class
    - Extend `Overlay` with `ABOVE_WIDGETS` layer and `DYNAMIC` position
    - Render colored dots at minimap positions of highlighted entities within render distance
    - Use configured highlight color for dots
    - Render nothing when no entities are in range
    - _Requirements: 8.1, 8.2, 8.3_

  - [x] 8.2 Write property test for minimap indicators
    - **Property 11: Minimap indicators for in-range entities**
    - **Validates: Requirements 8.1, 8.2, 8.3**
    - Verify dots rendered for in-range entities, none rendered when empty

- [x] 9. Implement WorldMapPointManager
  - [x] 9.1 Create the WorldMapPointManager class
    - Use RuneLite's `WorldMapPointManager` API to add/remove map points
    - Implement `updateMarker(WorldPoint, String tooltip)` and `clearMarker()`
    - Subscribe to step changes: place marker if step has location, clear if not
    - Set tooltip to step instruction text (for click display)
    - Respect `enableWorldMap` config toggle
    - _Requirements: 5.1, 5.2, 5.3_

  - [x] 9.2 Write property test for world map marker matches step location
    - **Property 7: World map marker matches current step location**
    - **Validates: Requirements 5.1, 5.2**
    - Verify marker exists at step location when non-null, no marker when null

- [x] 10. Implement MouseButtonInputListener
  - [x] 10.1 Create the MouseButtonInputListener class
    - Implement `MouseListener` interface
    - Read `nextStepMouseButton` and `prevStepMouseButton` from config
    - On mouse press matching configured button, call `stepTracker.nextStep()` or `stepTracker.previousStep()`
    - Consume the mouse event to prevent game interaction
    - Ignore events when binding is NONE
    - _Requirements: 3.6, 3.7, 3.8, 3.9_

- [x] 11. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 12. Implement B0atyHcimGuidePlugin main class and wire everything together
  - [x] 12.1 Create the main plugin class
    - Extend `Plugin` with `@PluginDescriptor` annotation
    - Inject all components via Guice (`GuideDataStore`, `StepTracker`, `GuideOverlay`, `HighlightOverlay`, `MinimapOverlay`, `WorldMapPointManager`, `MouseButtonInputListener`)
    - In `startUp()`: load guide data, initialize step tracker, register overlays and input listener
    - In `shutDown()`: unregister overlays and input listener, clear world map markers
    - Subscribe to `ConfigChanged` events to handle reset-progress and toggle changes
    - Subscribe to `GameTick` for entity scanning (NPC/object presence checks for highlights)
    - _Requirements: All_

  - [x] 12.2 Write unit tests for plugin lifecycle
    - Test startUp registers overlays and input listener
    - Test shutDown unregisters overlays and clears state
    - Test ConfigChanged for step reset triggers jumpToStep
    - _Requirements: 6.5_

- [x] 13. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests use jqwik with minimum 100 iterations per property
- Unit tests use JUnit 5 with Mockito for mocking RuneLite APIs
- The guide_data.json should be populated with actual B0aty HCIM Guide V3 content
