/*
 * SPDX-FileCopyrightText: Copyright 2026 Bishops_exe and Unhacked authors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package me.bishopsexe.unhacked.utils;

import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.bishopsexe.unhacked.UnhackedPlugin;
import me.bishopsexe.unhacked.check.ModCheckResult;
import me.bishopsexe.unhacked.config.ModCheck;
import me.bishopsexe.unhacked.utils.messages.PlaceholderUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class PunishmentUtil {

  @NonNull
  private final UnhackedPlugin plugin;

  public @NonNull String executePunishment(@NonNull Player player,
      @NonNull ModCheck mod, @NonNull ModCheckResult result) {
    List<String> cmds = mod.executeOn().get(result);
    if (cmds == null || cmds.isEmpty()) {
      return "";
    }
    List<String> resolved = cmds.stream()
        .map(cmd -> PlaceholderUtil.parseRaw(cmd, Map.of("player", player.getName(), "mod", mod.displayName())))
        .toList();

    plugin.getServer().getScheduler().runTask(plugin, () -> {
      for (String cmd : resolved) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
      }
    });

    String joined = String.join("; ", resolved);
    plugin.getLogger().info("Punishment: " + joined);
    return joined;
  }
}
