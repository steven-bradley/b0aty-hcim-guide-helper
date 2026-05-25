---
inclusion: manual
---

# Writing Bank Files for the B0aty HCIM Guide

This document describes how to create and maintain the guide step JSON files in `src/main/resources/guide_steps/`.

## Source Material

The guide data comes from the OSRS Wiki page: https://oldschool.runescape.wiki/w/B0aty_guide

This is B0aty's HCIM Guide V1/V2/V3 — a macro-questing route for Ironman/HCIM accounts. The guide is organized into numbered "Bank" sections (Bank 1, Bank 2, etc.) with individual action steps within each bank.

## File Structure

- Each bank section gets its own JSON file: `bank_XX.json` (e.g., `bank_01.json`, `bank_02.json`)
- The pre-bank "Starting Out" section uses `bank_00_starting_out.json`
- All files are listed in load order in `src/main/resources/guide_steps/index.txt`
- Step numbers are globally sequential across all files (bank_00 starts at 1, bank_01 continues from where bank_00 left off, etc.)

## JSON Schema

Each file contains a JSON array of step objects:

```json
[
  {
    "stepNumber": 1,
    "section": "Bank 1",
    "instruction": "Full verbatim text from the guide",
    "entities": [
      { "type": "NPC", "gameId": 2812 },
      { "type": "OBJECT", "gameId": 1278 },
      { "type": "GROUND_ITEM", "gameId": 1511 },
      { "type": "INVENTORY_ITEM", "gameId": 1929 }
    ],
    "location": { "x": 3243, "y": 3210, "plane": 0 }
  }
]
```

### Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `stepNumber` | int | Yes | Globally sequential step number across all files |
| `section` | string | Yes | Section name matching the bank (e.g., "Bank 1", "Bank 39B", "Starting Out") |
| `instruction` | string | Yes | Full verbatim instruction text from the guide (see below) |
| `entities` | array | Yes | Array of entity references (can be empty `[]`) |
| `location` | object or null | Yes | WorldPoint coordinates `{x, y, plane}` or `null` if no specific location |

### Entity Types

| Type | Description | gameId meaning |
|------|-------------|----------------|
| `NPC` | An NPC to highlight | NPC ID from OSRS wiki |
| `OBJECT` | A game object (tree, rock, altar, etc.) | Object ID from OSRS wiki |
| `GROUND_ITEM` | An item on the ground to pick up | Item ID |
| `INVENTORY_ITEM` | An item to use/buy (shown in inventory) | Item ID |

## Instruction Text Rules

**CRITICAL: Preserve the full verbatim text from the guide.** This includes:

1. **Dialogue options** — Keep exact format: `(3,1)`, `(2,1,1)`, `(1,2,3,4)`
2. **Quest references** — Keep in brackets: `[Restless Ghost]`, `[Lumbridge Easy Diary]`
3. **Warnings and tips** — Include all caps warnings: `DO NOT TANK THEM WHILE MINING`
4. **Inventory slot counts** — Keep: `(8 Inventory Slots)`, `(15 Inventory Spaces)`
5. **Multiple quest tags** — Keep all: `[Black Knights Fortress][Mournings End Pt 2][Kings Ransom]`
6. **Safespot instructions** — Include: `Safespot the Tough Guy over a chair`
7. **Video links** — Include safespot video URLs when present
8. **Optional steps** — Prefix with `OPTIONAL:` as in the guide
9. **Notes** — Include `NOTE:` prefixed tips

Do NOT summarize, abbreviate, or paraphrase. The player needs the exact instructions.

## When to Split Steps

Each distinct action should be its own step. Split when:
- The player moves to a new location
- The player interacts with a different NPC/object
- There's a distinct "do X then Y" that involves different entities

Keep together when:
- Multiple items are bought from the same NPC in one interaction
- A quest completion is a single logical action
- A warning/note applies to the immediately preceding action

## Location Coordinates

- Use the OSRS wiki to find NPC/object locations
- Coordinates are in the format `{ "x": XXXX, "y": YYYY, "plane": 0 }`
- `plane` is 0 for ground level, 1 for first floor up, 2 for second floor, etc.
- Underground areas use different coordinate ranges (e.g., y > 9000 for some dungeons)
- Set to `null` when there's no specific location (e.g., "Make Soft Clay" which can be done anywhere)

## NPC/Object IDs

- Look up IDs on the OSRS Wiki: `https://oldschool.runescape.wiki/w/NPC_NAME`
- The ID is in the "Advanced data" section of the wiki infobox
- If an NPC has multiple IDs (e.g., `815,5327,8051`), use the first/primary one
- Common IDs to know:
  - Aubury: 5913
  - Zaff: 546
  - Duke Horacio: 815
  - King Roald: 5215
  - Ali Morrisane: 1862
  - Veos (Port Sarim): 8484
  - Veos (Piscarilius): 10724

## Entity Selection Rules

- Only include entities that the player directly interacts with in that step
- For "Kill X" steps, include the NPC
- For "Buy from X" steps, include the shop NPC
- For "Collect X from ground" steps, include the ground item
- For "Bank and deposit" steps, use empty entities `[]`
- For "Teleport to X" steps, use empty entities `[]`
- For "Complete quest" steps, include the final NPC you talk to (if known)
- For withdraw steps, use empty entities `[]`

## Adding New Bank Files

1. Create the JSON file in `src/main/resources/guide_steps/`
2. Add the filename to `index.txt` in the correct position
3. Ensure step numbers continue sequentially from the previous file
4. Run tests: `docker run --rm -v "${PWD}:/workspace" b0aty-hcim-guide gradle test`

## Current Progress

Banks 00 (Starting Out) through 43 are complete, covering Episodes 1-3 of the guide (up to Canifis/Priest in Peril). The next bank to create is Bank 44, which starts with the Nature Spirit / Morytania content.

The last step number used is 571 (in bank_43.json). Bank 44 should start at step 572.

## Section Naming

Use the bank number as the section name:
- `"Starting Out"` for bank_00
- `"Bank 1"` through `"Bank 43"` for the numbered banks
- If the guide splits a bank (e.g., "Bank 39A" and "Bank 39B"), use those as section names

## Testing

After creating or modifying bank files, always run:
```
docker run --rm -v "${PWD}:/workspace" b0aty-hcim-guide gradle test
```

The tests validate that:
- All steps have non-empty instructions
- All steps have non-negative step numbers
- All steps have non-empty section names
- Entity references have valid types and positive game IDs
- The data loads successfully from all files
