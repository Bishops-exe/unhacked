/*
 * SPDX-FileCopyrightText: Copyright 2026 Bishops_exe and Unhacked authors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package me.bishopsexe.unhacked.events;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bishopsexe.unhacked.UnhackedPlugin;
import me.bishopsexe.unhacked.check.CheckProcess;
import me.bishopsexe.unhacked.config.ModCheck;
import me.bishopsexe.unhacked.logging.CommandOutput;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@RequiredArgsConstructor
public class JoinEvent implements Listener {

  private final UnhackedPlugin plugin;

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onJoin(PlayerJoinEvent event) {
    if (!plugin.getConfigManager().isJoinCheckEnabled()) {
      return;
    }

    Player player = event.getPlayer();

    if (plugin.getConfigManager().isOnlyFirstJoin() && player.hasPlayedBefore()) {
      return;
    }

    List<ModCheck> mods = plugin.getConfigManager().getJoinCheckMods();
    if (mods.isEmpty()) {
      return;
    }

    CommandOutput output = new CommandOutput(plugin, new CommandSender[]{});
    String name = player.getName();
    plugin.getLogger().info("Scheduling join-check for " + name);

    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
      plugin.getLogger().info("Starting join-check for " + name);

      CheckProcess.createAndProcessCheck(plugin, player, mods, output);
    }, plugin.getConfigManager().getJoinCheckDelayTicks());
  }


}