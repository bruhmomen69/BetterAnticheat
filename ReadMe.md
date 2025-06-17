# BetterAnticheat

**BEFORE INSTALLING, PLEASE READ:** 

At this point, BetterAnticheat is in an early developmental state. It is not confirmed to be stable and should be used
with caution.

BetterAnticheat is an auxiliary anticheat. BetterAnticheat does not aim to and will not catch cheats like flight, speed,
reach, etc. BetterAnticheat exists to catch more niche cheats that many anticheats do not catch on their own, meaning it
should be run alongside another anticheat.

BetterAnticheat is written in a way where it does not send any packets other than chat messages and will not alter any
packets coming to the server. This means that there should not be any incompatibilities with other plugins unless that
other plugin creates an incompatibility.

It is also designed to be lightweight, platform-independent, and designed with a 0-buffer system where if a check flags
it means the player is running a non-vanilla client. It should be noted that it is entirely possible that the plugin
will not help you at all - a lot of checks are extremely niche or patch exploits that aren't common anymore. However,
all checks in the anticheat do protect your server from possibly malicious behavior.

This project and its collaborators use the following regularly and are thankful for their maintenance:
- [PacketEvents](https://github.com/retrooper/packetevents)
- [sharkbyte-configuration](https://github.com/amnoah/sharkbyte-configuration)

## Requirements

This plugin is designed for 1.21.4+ on Spigot, Paper/Folia, and Sponge.

It requires [PacketEvents](https://github.com/retrooper/packetevents)

## Checks

BetterAnticheat has the following checks in the following categories:

### Chat

These checks look for issues with chatting. These may catch macros and chat automation tools.

- HiddenChat | Sending chat messages with the chat box hidden (often macros).
- ImpossibleCompletion | Tab completing invalid messages.
- ImpossibleMessage | Sending empty or invalidly formatted messages.

### Combat

These checks look for issues with combat. These may catch a variety of combat cheats.

- ActionInteractOrder | Sending an EntityAction packet prior to an InteractEntity in a tick.
- DualClick | Sending simultaneous left and right clicks.
- InvalidReleaseValues | Releasing a used item with values filled out.
- InvalidUseActions | Attacking or placing while using an item.
- MultipleAction | Changing sneak or sprint status multiple times in a tick.
- MultipleHit | Hitting multiple entities in a tick.
- NoSwingCombat | Attacking an entity without sending an arm swing packet.
- SelfHit | Attacking yourself.
- SlotInteractOrder | Sending a SlotChange prior to InteractEntity packet in a tick.

### Dig

These checks look for issues with digging. These may catch nuker, fast break, and other cheats.

- DigBlockFacePosition | Digging block faces which cannot be seen.
- DigOrder | Sending wrongful dig stages.
- MultiBreak | Digging multiple blocks at once.
- NoSwingDig | Digging without swinging an arm.

### Flying

These checks look for issues with flying packets. These may catch position/rotation alterations and related cheats.

- ArtificialFlying | Faking flying packets.
- FlyingSequence | Not sending flying packets within 20 ticks.
- ImpossiblePosition | Sending position values that are impossible.
- ImpossibleRotation | Sending rotation values that are impossible.
- RepeatedSteer | Sending steer packets with no corresponding rotation change.

### Heuristic

These checks use statistics and observed behavior to identify cheats. Unlike other checks, these are not built on
defined behavior and theoretically could be false flagged by perfect player behavior. As such, I'd recommend that these 
should be used as an indicator to watch players and, at most, kick them rather than issuing more permanent punishments.

- CombatAcceleration | Constant acceleration during combat.
- PitchSnap | Snapping vertical rotation changes.

### Misc

These checks are a variety that don't fit into other categories.

- ImpossibleHorseJump | Sending mathematically wrong horse jumps.
- ImpossibleSlot | Accessing slots which are not real.
- LargeName | Renaming an item an excessively long name.
- MultipleSlot | Changing slots multiple times in a tick.
- SmallRender | Having a render distance less than 2.

### Packet

These checks look for general packet issues. These may catch packet order alterations.

- PingPongOrder | Sending ping pong packets in the wrong order.
- Post | Sending packets in the wrong order.
- TeleportConfirmOrder | Sending teleport confirm packets in the wrong order.

### Place

These checks look for issues with block placement. These may catch scaffold, fast place, ghost hand, and other cheats.

- PlaceBlockFacePosition | Placing on block faces which cannot be seen.

# Documentation

TODO

# Support

For general support, please join my [Discord server](https://discord.gg/ey9uTg3hcy).

For issues with the project, please open an issue in the issues tab.