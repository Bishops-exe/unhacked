/*
 * SPDX-FileCopyrightText: Copyright 2026 Bishops_exe and Unhacked authors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package me.bishopsexe.unhacked.logging;

import java.util.logging.Level;
import me.bishopsexe.unhacked.UnhackedPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent.Builder;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;

public record CommandOutput(Plugin plugin, CommandSender[] commandSenderOutput) {

  public CommandOutput(Plugin plugin, CommandSender commandSenderOutput) {
    this(plugin, new CommandSender[]{commandSenderOutput});
  }


  public void log(Level level, String msg) {
    Component component = MiniMessage.miniMessage().deserialize(msg);
    LogLevelManager logLevels = UnhackedPlugin.getInstance().getLogLevelManager();

    if (level.intValue() >= logLevels.getLevel(Bukkit.getConsoleSender()).intValue()) {
      plugin.getLogger().log(level, PlainTextComponentSerializer.plainText().serialize(component));
    }

    NamedTextColor color = switch (level.getName()) {
      case "SEVERE" -> NamedTextColor.DARK_RED;
      case "WARNING" -> NamedTextColor.YELLOW;
      case "INFO" -> NamedTextColor.GREEN;
      case "CONFIG" -> NamedTextColor.DARK_PURPLE;
      case "FINE" -> NamedTextColor.LIGHT_PURPLE;
      default -> NamedTextColor.WHITE;
    };

    Component prefix = Component.text("[%s] ".formatted(plugin.getName()), color);
    Builder sendingComponent = Component.text().color(color).append(prefix).append(component);

    for (CommandSender sender : commandSenderOutput) {
      // We have already sent the response through the logger
      if (sender instanceof ConsoleCommandSender) {
        return;
      }
      if (level.intValue() < logLevels.getLevel(sender).intValue()) {
        continue;
      }
      sender.sendMessage(sendingComponent);
    }
  }

  public void severe(String msg) {
    log(Level.SEVERE, msg);
  }

  public void warning(String msg) {
    log(Level.WARNING, msg);
  }

  public void info(String msg) {
    log(Level.INFO, msg);
  }

  public void config(String msg) {
    log(Level.CONFIG, msg);
  }

  public void fine(String msg) {
    log(Level.FINE, msg);
  }
}
