# Requirements Document

## Introduction

A RuneLite plugin that provides an in-game overlay guiding players through B0aty's Hardcore Ironman (HCIM) Guide V3. The plugin displays step-by-step instructions from the guide and highlights relevant game entities (items, NPCs, objects, locations) in the game world, similar to the Optimal Quest Guide Helper plugin.

## Glossary

- **Plugin**: A RuneLite plugin that integrates into the OSRS client
- **Overlay**: A UI panel rendered on top of the game client displaying guide step information
- **Step**: A single actionable instruction from the B0aty HCIM Guide V3
- **Guide_Data**: The structured representation of all steps from the B0aty HCIM Guide V3
- **Highlight_System**: The subsystem responsible for visually marking relevant game entities
- **Entity**: A game object, NPC, item, or ground item that is relevant to the current step
- **World_Map_Marker**: A marker placed on the in-game world map indicating a destination
- **Step_Tracker**: The subsystem that tracks the player's current position in the guide
- **Config_Panel**: The RuneLite configuration panel for plugin settings

## Requirements

### Requirement 1: Guide Data Storage

**User Story:** As a plugin developer, I want the guide steps stored as structured data within the plugin, so that the overlay can display them without requiring network access.

#### Acceptance Criteria

1. THE Plugin SHALL store all steps from the B0aty HCIM Guide V3 as embedded structured data
2. THE Guide_Data SHALL include for each step: a step number, section name, instruction text, and associated entity identifiers
3. THE Guide_Data SHALL include entity metadata specifying entity type (NPC, item, object, or location) and game ID or coordinates for each referenced entity

### Requirement 2: Step Overlay Display

**User Story:** As a player, I want to see the current guide step in an overlay panel, so that I can follow the guide without leaving the game.

#### Acceptance Criteria

1. THE Overlay SHALL display the current step number, section name, and instruction text
2. THE Overlay SHALL be visible as a draggable panel within the game client
3. WHEN the player advances to the next step, THE Overlay SHALL update to display the new step information
4. THE Overlay SHALL display navigation controls allowing the player to move to the next or previous step
5. WHILE the plugin is enabled, THE Overlay SHALL remain visible unless the player hides it via configuration

### Requirement 3: Step Navigation

**User Story:** As a player, I want to navigate forward and backward through guide steps, so that I can skip completed steps or revisit previous ones.

#### Acceptance Criteria

1. WHEN the player clicks the next-step control, THE Step_Tracker SHALL advance to the next step in the guide
2. WHEN the player clicks the previous-step control, THE Step_Tracker SHALL return to the previous step in the guide
3. IF the player is on the first step and clicks previous, THEN THE Step_Tracker SHALL remain on the first step
4. IF the player is on the last step and clicks next, THEN THE Step_Tracker SHALL remain on the last step
5. THE Step_Tracker SHALL persist the current step across client sessions
6. THE Config_Panel SHALL provide an option to bind a mouse button to advance to the next step
7. THE Config_Panel SHALL provide an option to bind a mouse button to return to the previous step
8. WHEN the player presses the bound mouse button for next-step, THE Step_Tracker SHALL advance to the next step
9. WHEN the player presses the bound mouse button for previous-step, THE Step_Tracker SHALL return to the previous step

### Requirement 4: Entity Highlighting

**User Story:** As a player, I want relevant NPCs, items, and objects highlighted in the game world, so that I can quickly identify what I need to interact with for the current step.

#### Acceptance Criteria

1. WHILE a step is active, THE Highlight_System SHALL render a colored outline around all NPCs referenced by the current step
2. WHILE a step is active, THE Highlight_System SHALL render a colored outline around all interactable objects referenced by the current step
3. WHILE a step is active, THE Highlight_System SHALL render a colored highlight on all ground items referenced by the current step
4. WHILE a step is active, THE Highlight_System SHALL render a colored highlight on all inventory items referenced by the current step
5. WHEN the player advances to a new step, THE Highlight_System SHALL remove highlights from the previous step and apply highlights for the new step

### Requirement 5: World Map Markers

**User Story:** As a player, I want destination locations marked on the world map, so that I can navigate to the correct area for each step.

#### Acceptance Criteria

1. WHILE a step references a specific location, THE World_Map_Marker SHALL place a marker on the world map at the referenced coordinates
2. WHEN the player advances to a new step, THE World_Map_Marker SHALL remove the previous marker and place a new marker if the new step references a location
3. WHEN the player clicks a world map marker, THE Plugin SHALL display the step instruction associated with that marker

### Requirement 6: Plugin Configuration

**User Story:** As a player, I want to configure the plugin appearance and behavior, so that I can customize it to my preferences.

#### Acceptance Criteria

1. THE Config_Panel SHALL provide an option to toggle overlay visibility
2. THE Config_Panel SHALL provide an option to change the highlight color for entities
3. THE Config_Panel SHALL provide an option to toggle entity highlighting on or off
4. THE Config_Panel SHALL provide an option to toggle world map markers on or off
5. THE Config_Panel SHALL provide an option to reset progress to a specific step number

### Requirement 7: Section Filtering

**User Story:** As a player, I want to jump to specific sections of the guide, so that I can start from a relevant point without scrolling through all steps.

#### Acceptance Criteria

1. THE Overlay SHALL provide a section selector listing all guide sections
2. WHEN the player selects a section, THE Step_Tracker SHALL jump to the first step of that section
3. THE Overlay SHALL display the current section name alongside the step information

### Requirement 8: Minimap Indicators

**User Story:** As a player, I want to see directional indicators on the minimap for nearby highlighted entities, so that I can locate them without scanning the full game view.

#### Acceptance Criteria

1. WHILE a highlighted entity is within render distance, THE Plugin SHALL display an indicator dot on the minimap at the entity's position
2. THE Plugin SHALL use the same color as the entity highlight for the minimap indicator
3. WHEN no highlighted entities are within render distance, THE Plugin SHALL not display any minimap indicators
