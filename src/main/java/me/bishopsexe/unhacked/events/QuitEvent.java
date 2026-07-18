/*
 * SPDX-FileCopyrightText: Copyright 2026 Bishops_exe and Unhacked authors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package me.bishopsexe.unhacked.events;

import me.bishopsexe.unhacked.check.CheckProcess;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitEvent implements Listener {

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    CheckProcess.getCheckProcess(event.getPlayer().getUniqueId()).cancelCheck();
  }
}
