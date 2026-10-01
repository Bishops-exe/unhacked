/*
 * SPDX-FileCopyrightText: Copyright 2026 Bishops_exe and Unhacked authors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package me.bishopsexe.unhacked;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import me.bishopsexe.unhacked.check.CheckProcess;
import me.bishopsexe.unhacked.commands.UnhackedCommand;
import me.bishopsexe.unhacked.config.ConfigManager;
import me.bishopsexe.unhacked.events.JoinEvent;
import me.bishopsexe.unhacked.events.SignEvent;
import me.bishopsexe.unhacked.logging.LogLevelManager;
import me.bishopsexe.unhacked.utils.MetricUtil;
import me.bishopsexe.unhacked.utils.PunishmentUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class UnhackedPlugin extends JavaPlugin {

  @Getter
  private static UnhackedPlugin instance;
  @Getter
  private ConfigManager configManager;
  @Getter
  private PunishmentUtil punishmentUtil;
  @Getter
  private SignEvent signEvent;
  @Getter
  private LogLevelManager logLevelManager;
  @Getter
  private MetricUtil metricUtil;

  @Override
  public void onLoad() {
    PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
    PacketEvents.getAPI().load();
  }

  @Override
  public void onEnable() {
    PacketEvents.getAPI().init();
    saveDefaultConfig();

    instance = this;
    logLevelManager = new LogLevelManager(this);
    configManager = new ConfigManager(this);
    punishmentUtil = new PunishmentUtil(this);
    metricUtil = new MetricUtil(this);

    UnhackedCommand checkCmd = new UnhackedCommand(this);

    getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> checkCmd.register(event.registrar()));

    signEvent = new SignEvent();
    PacketEvents.getAPI().getEventManager().registerListener(signEvent);

    getServer().getPluginManager().registerEvents(new JoinEvent(this), this);

    getLogger().info("Unhacked has been enabled.");

    metricUtil.enable();
  }

  @Override
  public void onDisable() {
    CheckProcess.cancelAll();
    if (signEvent != null) {
      PacketEvents.getAPI().getEventManager().unregisterListener(signEvent);
    }
    PacketEvents.getAPI().terminate();
    getLogger().info("Unhacked has been disabled.");

    metricUtil.disable();
  }
}