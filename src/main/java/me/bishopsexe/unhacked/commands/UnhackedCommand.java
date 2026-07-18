/*
 * SPDX-FileCopyrightText: Copyright 2026 Bishops_exe and Unhacked authors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package me.bishopsexe.unhacked.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.bishopsexe.unhacked.UnhackedPlugin;
import me.bishopsexe.unhacked.check.CheckProcess;
import me.bishopsexe.unhacked.config.ModCheck;
import me.bishopsexe.unhacked.logging.CommandOutput;
import me.bishopsexe.unhacked.logging.LogLevelManager;
import me.bishopsexe.unhacked.utils.messages.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class UnhackedCommand {

  private final UnhackedPlugin plugin;

  public void register(@NonNull Commands registrar) {
    LiteralCommandNode<CommandSourceStack> node = Commands.literal("unhacked")
        .requires(src -> src.getSender().hasPermission("unhacked.use"))
        .then(Commands.literal("reload")
            .requires(src -> src.getSender().hasPermission("unhacked.reload"))
            .executes(ctx -> {
              executeReload(new CommandOutput(plugin, ctx.getSource().getSender()));
              return 1;
            }))
        .then(Commands.literal("loglevel")
            .requires(src -> src.getSender().hasPermission("unhacked.loglevel"))
            .executes(ctx -> {
              executeLogLevel(ctx.getSource().getSender(), null,
                  new CommandOutput(plugin, ctx.getSource().getSender()));
              return 1;
            })
            .then(Commands.argument("level", StringArgumentType.word())
                .suggests(logLevelSuggestions())
                .executes(ctx -> {
                  executeLogLevel(ctx.getSource().getSender(),
                      StringArgumentType.getString(ctx, "level"),
                      new CommandOutput(plugin, ctx.getSource().getSender()));
                  return 1;
                })))
        .then(Commands.literal("check")
            .requires(src -> src.getSender().hasPermission("unhacked.check"))
            .then(Commands.argument("player", StringArgumentType.word())
                .suggests(onlinePlayerSuggestions())
                .executes(ctx -> {
                  executeCheck(StringArgumentType.getString(ctx, "player"), null,
                      new CommandOutput(plugin, ctx.getSource().getSender()));
                  return 1;
                })
                .then(Commands.argument("mods", StringArgumentType.greedyString())
                    .suggests(modSuggestions())
                    .executes(ctx -> {
                      executeCheck(StringArgumentType.getString(ctx, "player"),
                          StringArgumentType.getString(ctx, "mods"),
                          new CommandOutput(plugin, ctx.getSource().getSender()));
                      return 1;
                    }))))
        .build();

    registrar.register(node, "Unhacked commands");
  }

  private void executeReload(CommandOutput output) {
    plugin.getConfigManager().reload();
    output.info(MessageUtil.reloadDone());
  }

  private void executeLogLevel(@NonNull CommandSender sender, String requestedLevel,
      CommandOutput output) {
    LogLevelManager logLevelManager = plugin.getLogLevelManager();
    String levels = LogLevelManager.LEVELS.stream().map(Level::getName)
        .collect(Collectors.joining(", "));

    if (requestedLevel == null) {
      String current = logLevelManager.getLevel(sender).getName();
      output.info(MessageUtil.loglevelCurrent(current, levels));
      return;
    }

    String requested = requestedLevel.trim().toUpperCase();
    Level level = LogLevelManager.LEVELS.stream()
        .filter(l -> l.getName().equals(requested))
        .findFirst()
        .orElse(null);

    if (level == null) {
      output.severe(MessageUtil.loglevelInvalid(requestedLevel.trim(), levels));
      return;
    }

    logLevelManager.setLevel(sender, level);
    output.info(MessageUtil.loglevelSet(level.getName()));
  }

  private void executeCheck(@NonNull String playerName, String modsArg, CommandOutput output) {
    Player target = Bukkit.getPlayerExact(playerName);
    if (target == null) {
      output.severe(MessageUtil.playerNotFound(playerName));
      return;
    }
    if (CheckProcess.isCheckInProgress(target.getUniqueId())) {
      output.severe(MessageUtil.alreadyChecking(target.getName()));
      return;
    }

    Set<ModCheck> mods;
    if (modsArg != null && !modsArg.isBlank()) {
      mods = new LinkedHashSet<>();
      for (String modName : modsArg.trim().split("\\s+")) {
        ModCheck h = plugin.getConfigManager().getMod(modName.trim().toLowerCase());

        if (h == null) {
          output.severe(MessageUtil.invalidMod(modName.trim()));
          return;
        }

        mods.add(h);
      }
    } else {
      List<ModCheck> defaults = plugin.getConfigManager().getDefaultCheckMods();

      mods = new LinkedHashSet<>(defaults.size(), 1.0f);
      mods.addAll(defaults);
    }

    if (mods.isEmpty()) {
      // silently fail
      return;
    }

    CheckProcess.createAndProcessCheck(plugin, target, mods, output);
  }

  private SuggestionProvider<CommandSourceStack> onlinePlayerSuggestions() {
    return (ctx, builder) -> {
      String remaining = builder.getRemainingLowerCase();
      Bukkit.getOnlinePlayers().stream()
          .map(Player::getName)
          .filter(name -> name.toLowerCase().startsWith(remaining))
          .forEach(builder::suggest);
      return builder.buildFuture();
    };
  }

  private SuggestionProvider<CommandSourceStack> modSuggestions() {
    return (ctx, builder) -> {
      String remaining = builder.getRemainingLowerCase();
      String lastToken = remaining.contains(" ")
          ? remaining.substring(remaining.lastIndexOf(' ') + 1)
          : remaining;
      String prefix = remaining.substring(0, remaining.length() - lastToken.length());
      plugin.getConfigManager().getMods().keySet().stream()
          .filter(name -> name.toLowerCase().startsWith(lastToken))
          .forEach(name -> builder.suggest(prefix + name));
      return builder.buildFuture();
    };
  }

  private SuggestionProvider<CommandSourceStack> logLevelSuggestions() {
    return (ctx, builder) -> {
      String remaining = builder.getRemainingLowerCase();
      LogLevelManager.LEVELS.stream()
          .map(Level::getName)
          .filter(name -> name.toLowerCase().startsWith(remaining))
          .forEach(builder::suggest);
      return builder.buildFuture();
    };
  }
}