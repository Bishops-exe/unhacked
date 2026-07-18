/*
 * SPDX-FileCopyrightText: Copyright 2026 Bishops_exe and Unhacked authors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package me.bishopsexe.unhacked.logging;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import lombok.NonNull;
import me.bishopsexe.unhacked.UnhackedPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class LogLevelManager {

  public static final List<Level> LEVELS = List.of(
      Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE
  );

  public static final Level DEFAULT_LEVEL = Level.INFO;

  private static final String CONSOLE_KEY = "console";

  private final UnhackedPlugin plugin;
  private final File file;
  private YamlConfiguration data;

  public LogLevelManager(@NonNull UnhackedPlugin plugin) {
    this.plugin = plugin;
    this.file = new File(plugin.getDataFolder(), "loglevels.yml");
    load();
  }

  private void load() {
    data = file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();
  }

  private void save() {
    try {
      data.save(file);
    } catch (IOException e) {
      plugin.getLogger().warning("Failed to save loglevels.yml: " + e.getMessage());
    }
  }

  private String keyFor(@NonNull CommandSender sender) {
    return sender instanceof Player player ? player.getUniqueId().toString() : CONSOLE_KEY;
  }

  public @NonNull Level getLevel(@NonNull CommandSender sender) {
    String name = data.getString(keyFor(sender));
    if (name == null) {
      return DEFAULT_LEVEL;
    }
    try {
      return Level.parse(name);
    } catch (IllegalArgumentException e) {
      return DEFAULT_LEVEL;
    }
  }

  public void setLevel(@NonNull CommandSender sender, @NonNull Level level) {
    data.set(keyFor(sender), level.getName());
    save();
  }
}