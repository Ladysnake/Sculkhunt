# Sculkhunt

## The Game

### Preparation Phase

- Starts with "/sculkhunt start [prepDuration]", stops with "/sculkhunt stop"
- Preparation phase:
  - 1 out of 5 players are chosen to turn into sculk trackers at the end of the prep phase. They are notified at the start of the prep phase
  - Prep phase lasts 15 minutes by default, can be customised with the command (in seconds)
  - At the end of the preparation phase, the hunting phase starts, chosen players turn into Sculk Trackers

### Hunt Phase

- Sculk Trackers vs. Survivors

  - Survivors need to escape through the end portal by gathering sculk eyes (one use ender eyes)
  - Sculk trackers need to hunt down all remaining Survivors
  - Survivors have one life: upon dying, they join the Sculk Trackers

- #### Sculk Trackers

  - Cool high contrast vision that allows to see better in the dark, but get blinded in bright areas (also, sick animated vignette!)
  - Lower health, depends on the percentage of Sculk Trackers in the game
  - No fall or suffocation damage
  - Swim faster and don't drown underwater
  - 4 attack damage (2 hearts) + 2 attack damage (1 heart) per player (above 1 player) in a 16 block range
  - Cannot pick up items
  - Can mine and pick up Sculk Blocks
  - Upon dying, respawn at the Sculk Catalyst closest to a Survivor, rising from the ground
  - Can see vibrations through walls, emitted when players are moving non-sneakily, blocks are broken with tools, bells are rung, chests / doors are opened / closed, projectiles landing, etc...
  - Cannot see immobile, sneaky players, players walking in water / swimming underwater as well as projectiles
  - Whenever in a wall, phases through it and rises to the top
  - Can interact with sculk blocks to teleport into them
  - Can interact with sculk catalysts to relocate (to the closest survivor if there are any, a random catalyst if there aren't any)
  - When killed, drops a random raw food / trash / projectile item

- Sculk Catalysts

  - Spawn around players during the hunt phase, expand and spawn sculk mobs indefinitely
  - Retains in memory all blocks it converted into Sculk Veins or Sculk blocks
  - When taking damage, the sculk catalyst restores a certain amount of blocks it converted into sculk
  - When there are no sculk blocks remaining, a Catalyst taking damage will get destroyed
  - Each catalyst drops a sculk eye that can be used by survivors to track the end portal and open it
  - When killed, drops a random raw food / trash / projectile item

- Sculk

  - Slows down survivor players (if they don't wear boots) and gives them mining fatigue
  - Slows down and damages non-sculk mobs
  - Makes sculk mobs and trackers faster and gives them regeneration
  - When a mob dies on sculk, a sculk copy of the mob will rise from it
  - Absorbs and destroys all non-living entities like items, boats, tnt, etc...

- Sculk Sensors

  - When detecting a vibration, will reveal and detect all close-by entities and players for 10 seconds
  - When detected, an entity can get targeted by hostile sculk mobs and is fully visible to Sculk Trackers

- Sculk mobs

  - When dying from sculk, a sculk version of a mob will rise from the sculk
  - Cannot see their targets if they are not detected
  - When killed, drops a random raw food / trash / projectile item

### Gamerules

- `sculkCatalystSpawning`: Handles whether or not sculk catalysts are naturally spawning.
- `sculkCatalystSpawningDelay`: Handles the delay between each catalyst spawn.
- `sculkCatalystSpawningRadius`: Handles the maximum radius at which catalysts spawn from players (minimum is 8 and unmodifiable).
- `sculkCatalystBloomDelay`: Handles the delay between each catalyst bloom (the phase when a catalyst spreads).
- `sculkCatalystBloomRadius`: Handles how many blocks a catalyst will convert upon blooming.
- `sculkCatalystTerritoryRadius`: Handles the spread between the spawned catalysts (eg: 30 means there will be one catalyst per 30 blocks).
- `sculkCatalystMobSpawnFrequency`: Handles the frequency at which mob spawns from catalysts (lower = faster spawn rate).

#### Sculk Tracker Tips

- Mobs in the night will target players, so look where zombies are going, where skeletons are shooting, etc... to locate elusive players.
- You can teleport to sculk blocks, so you can use them to phase to the surface in a cave (by placing one on the ceiling and interacting with it), make a giant pillar elevator, or hide under sculk blocks near a catalyst and phase through when you know your prey is close.
- Don't hesitate to interact with Sculk Catalysts to relocate to other survivors if you're having a hard time with one or want to coordinate an assault with other trackers

#### Survivor Tips

- Sculk trackers can't see you when you sneak, stay immobile, walk in water or swim, so favor stealth plays to maximize you survival chances.
- Sculk mobs, trackers and catalysts can drop raw food, junk items and projectiles such as arrows or eggs.
- Use projectiles like arrows, eggs, snowballs, etc... to bamboozle sculk trackers to another position.
- Noise machines with opening trapdoors, extending pistons, bells, etc... can be useful to hide your position and scramble your tracks.
- Sculk trackers have a hard time distinguishing blocks in bright environments, so use this to your advantage by making lava traps for example.