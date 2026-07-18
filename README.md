# Unhacked

Paper plugin that detects client-side mods (Meteor Client, Wurst, Baritone, Freecam, autoclickers, etc.) using sign-component packet checks, then optionally kicks/bans the player.

## How it works

Each mod check maps to a resource-pack translation key (`key.*`) rendered on a sign sent only to the checked player. The client's response reveals whether the mod's client-side key overrides the sign text - a mismatch means the mod is present. Detection modes (`config.yml` `mode:` field): `TRANSLATE`, `KEYBIND`, `EXPLOIT_PREVENTER`.

## Requirements

- Paper 1.21.x server, Java 21
- [packetevents](https://github.com/retrooper/packetevents)
- [PacketBlocks](https://github.com/BitByLogics/PacketBlocks)

Both are hard dependencies - the plugin won't load without them.

## Commands

All under `/unhacked` (permission `unhacked.use`):

| Command                                    | Permission          | Description                                                     |
|--------------------------------------------|---------------------|-----------------------------------------------------------------|
| `/unhacked check <player> [mod1 mod2 ...]` | `unhacked.check`    | Run a check on a player. Omit mods to use `default-check-mods`. |
| `/unhacked reload`                         | `unhacked.reload`   | Reload `config.yml`.                                            |
| `/unhacked loglevel [level]`               | `unhacked.loglevel` | View/set your own log verbosity.                                |

`unhacked.echo-results` (default: op) gates who receives detection notifications.

## Configuration

See `src/main/resources/config.yml`:

- `mods` - registry of checkable mods: `display-name`, `key`, `mode`, and per-result `execute-on` console commands (supports `{player}`/`{mod}` placeholders; run your ban/kick plugin's command here).
- `default-check-mods` - mods used when `/unhacked check <player>` is run with no mod list.
- `join-check` - auto-check players on join (`enabled`, `only-first-join`, `delay` in ticks, `mods`).
- `timeout` - ticks to wait for a client's sign-packet response before marking the check as skipped.
- `messages` - all user-facing strings.

## Building

```
./gradlew build
```

Produces a shaded jar via `shadowJar`. Run a local test server with:

```
./gradlew runServer
```

(configured for the Minecraft version in `gradle.properties`).