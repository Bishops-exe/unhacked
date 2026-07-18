/*
 * SPDX-FileCopyrightText: Copyright 2026 Bishops_exe and Unhacked authors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package me.bishopsexe.unhacked.config;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import me.bishopsexe.unhacked.UnhackedPlugin;
import me.bishopsexe.unhacked.check.DetectionMode;
import me.bishopsexe.unhacked.check.ModCheckResult;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

  private final UnhackedPlugin plugin;
  private FileConfiguration config;
  @Getter
  private final Map<String, ModCheck> mods = new LinkedHashMap<>();

  public ConfigManager(@NonNull UnhackedPlugin plugin) {
    this.plugin = plugin;
    this.reload();
  }

  public void reload() {
    plugin.reloadConfig();
    config = plugin.getConfig();
    loadMods();
  }

  private void loadMods() {
    mods.clear();
    ConfigurationSection section = config.getConfigurationSection("mods");
    if (section == null) {
      return;
    }
    for (String id : section.getKeys(false)) {
      String displayName = section.getString(id + ".display-name", id);
      String key = section.getString(id + ".key", "");
      if (key.isBlank()) {
        continue;
      }
      DetectionMode mode;
      try {
        mode = DetectionMode.valueOf(section.getString(id + ".mode", "TRANSLATE").toUpperCase());
      } catch (IllegalArgumentException e) {
        mode = DetectionMode.TRANSLATE;
      }
      Map<ModCheckResult, List<String>> executeOn = new EnumMap<>(ModCheckResult.class);
      ConfigurationSection executeOnSection = section.getConfigurationSection(id + ".execute-on");
      if (executeOnSection != null) {
        putIfPresent(executeOnSection, "detected", ModCheckResult.DETECTED, executeOn);
        putIfPresent(executeOnSection, "not-detected", ModCheckResult.NOT_DETECTED, executeOn);
        putIfPresent(executeOnSection, "failed", ModCheckResult.FAILED, executeOn);
        putIfPresent(executeOnSection, "not-received", ModCheckResult.NOT_RECEIVED, executeOn);
      }
      mods.put(id, new ModCheck(id, displayName, key, mode, executeOn));
    }
    plugin.getLogger().info("Loaded " + mods.size() + " mods: " + mods.keySet());
  }

  private void putIfPresent(@NonNull ConfigurationSection section, @NonNull String key,
      @NonNull ModCheckResult result, @NonNull Map<ModCheckResult, List<String>> executeOn) {
    List<String> cmds = new ArrayList<>();
    if (section.isList(key)) {
      for (String cmd : section.getStringList(key)) {
        if (!cmd.isBlank()) {
          cmds.add(cmd);
        }
      }
    } else {
      String cmd = section.getString(key, "");
      if (!cmd.isBlank()) {
        cmds.add(cmd);
      }
    }
    if (!cmds.isEmpty()) {
      executeOn.put(result, cmds);
    }
  }

  public ModCheck getMod(@NonNull String id) {
    return mods.get(id);
  }

  public List<ModCheck> getDefaultCheckMods() {
    return resolveList("default-check-mods");
  }

  public List<ModCheck> getJoinCheckMods() {
    return resolveList("join-check.mods");
  }

  private List<ModCheck> resolveList(@NonNull String path) {
    List<ModCheck> result = new ArrayList<>();
    for (String id : config.getStringList(path)) {
      ModCheck h = mods.get(id);
      if (h != null) {
        result.add(h);
      }
    }
    return result;
  }

  public String getMessage(@NonNull String key) {
    return config.getString("messages." + key, "<red>Missing: " + key);
  }

  public boolean isJoinCheckEnabled() {
    return config.getBoolean("join-check.enabled", false);
  }

  public boolean isOnlyFirstJoin() {
    return config.getBoolean("join-check.only-first-join", false);
  }

  public int getTimeoutTicks() {
    return config.getInt("timeout", 200);
  }

  public long getJoinCheckDelayTicks() {
    return config.getLong("join-check.delay", 20L);
  }
}