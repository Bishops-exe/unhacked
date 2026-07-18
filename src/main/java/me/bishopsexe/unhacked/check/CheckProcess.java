/*
 * SPDX-FileCopyrightText: Copyright 2026 Bishops_exe and Unhacked authors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */


package me.bishopsexe.unhacked.check;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import me.bishopsexe.unhacked.UnhackedPlugin;
import me.bishopsexe.unhacked.config.ModCheck;
import me.bishopsexe.unhacked.logging.CommandOutput;
import me.bishopsexe.unhacked.utils.messages.MessageUtil;
import me.bishopsexe.unhacked.utils.sign.SignComponent;
import me.bishopsexe.unhacked.utils.sign.SignPacketUtil;
import net.bitbylogic.packetblocks.PacketBlocks;
import net.bitbylogic.packetblocks.block.PacketBlock;
import net.bitbylogic.packetblocks.block.PacketBlockManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CheckProcess {

  static final int LINES_PER_SIGN = 4;
  static final Map<UUID, CheckProcess> playerCheckMap = new ConcurrentHashMap<>();

  // -------------------------------------------------------------------------
  // Per-check state
  // -------------------------------------------------------------------------

  final UnhackedPlugin plugin;
  final List<ModCheck[]> batches;
  @Setter
  BukkitTask timeoutTask;
  @NonNull
  Player target;
  final List<String[]> collectedResponses = Collections.synchronizedList(new ArrayList<>());
  @NonNull
  CommandOutput output;

  // -------------------------------------------------------------------------
  // Static API
  // -------------------------------------------------------------------------

  public static CheckProcess getCheckProcess(@NonNull UUID uuid) {
    return playerCheckMap.get(uuid);
  }

  public static boolean isCheckInProgress(@NonNull UUID uuid) {
    return getCheckProcess(uuid) != null;
  }

  public static void createAndProcessCheck(
      @NonNull UnhackedPlugin plugin,
      @NonNull Player target,
      @NonNull Collection<ModCheck> mods,
      @NonNull CommandOutput output
  ) {
    CheckProcess proc = CheckProcess.createCheck(plugin, target, mods, output);

    if (proc == null) {
      return;
    }

    proc.processCheck();
  }

  public static CheckProcess createCheck(
      @NonNull UnhackedPlugin plugin,
      @NonNull Player target,
      @NonNull Collection<ModCheck> mods,
      @NonNull CommandOutput output
  ) {
    UUID uuid = target.getUniqueId();
    if (playerCheckMap.containsKey(uuid)) {
      output.severe(MessageUtil.alreadyChecking(target.getName()));
      return null;
    }

    List<ModCheck[]> batches = buildBatches(mods);
    if (batches.isEmpty()) {
      return null;
    }

    return new CheckProcess(plugin, batches, target, output);
  }

  public void cancelCheck() {
    UUID uuid = target.getUniqueId();
    CheckProcess check = playerCheckMap.remove(uuid);
    if (check == null) {
      return;
    }
    BukkitTask t = check.getTimeoutTask();
    if (t != null) {
      t.cancel();
    }

  }

  public static void cancelAll() {
    for (CheckProcess checkProcess : playerCheckMap.values()) {
      checkProcess.cancelCheck();
    }
  }

  // -------------------------------------------------------------------------
  // Check loop
  // -------------------------------------------------------------------------


  public void processCheck() {
    output.info(MessageUtil.checkStarted(target.getName()));
    output.fine(MessageUtil.checkBatches(target.getName(), batches.size(),
        batches.stream().mapToInt(b -> b.length).sum()));

    UUID uuid = target.getUniqueId();
    playerCheckMap.put(uuid, this);

    Location signLocation = target.getLocation().clone()
        .add(target.getLocation().getDirection().normalize().multiply(-3));

    PacketBlockManager blockManager = PacketBlocks.getInstance().getBlockManager();
    plugin.getServer().getScheduler().runTask(plugin, () -> {
      for (int i = 0; i < batches.size(); i++) {
        ModCheck[] batch = batches.get(i);
        output.fine(MessageUtil.batchSending(
            target.getName(),
            i,
            batches.size(),
            signLocation.getBlockX(),
            signLocation.getBlockY(),
            signLocation.getBlockZ())
        );

        PacketBlock packetBlock = blockManager.createBlock(
            signLocation,
            Material.OAK_SIGN.createBlockData()
        );
        packetBlock.addAndUpdateViewer(target);

        SignPacketUtil signUtil = new SignPacketUtil(signLocation);

        for (int j = 0; j < batch.length; j++) {
          ModCheck mod = batch[j];

          SignComponent component = SignComponent.fromAdventure(buildModComponent(mod));
          signUtil.front().line(j, component);
          signUtil.back().line(j, component);
        }

        signUtil.send(target);
        signUtil.openFor(target);
        signUtil.closeFor(target);

        blockManager.removeBlock(packetBlock);
      }
      output.fine(MessageUtil.batchesSent(target.getName()));
    });

    BukkitTask timeout = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
      output.warning(MessageUtil.timeout(target.getName(), collectedResponses.size(),
          batches.size()));
      finishCheck();
    }, plugin.getConfigManager().getTimeoutTicks());
    setTimeoutTask(timeout);
  }

  public void handleBatchResponse(@NonNull String[] lines) {
    collectedResponses.add(lines);
    output.fine(MessageUtil.batchReceived(target.getName(), collectedResponses.size(),
        batches.size(), Arrays.toString(lines)));

    if (collectedResponses.size() < batches.size()) {
      return;
    }

    BukkitTask timeout = getTimeoutTask();
    if (timeout != null) {
      timeout.cancel();
    }

    finishCheck();
  }

  // -------------------------------------------------------------------------
  // Batch evaluation
  // -------------------------------------------------------------------------

  private ModCheckResult evaluateResponse(@NonNull ModCheck mod, @NonNull String resp) {
    return switch (mod.mode()) {
      case TRANSLATE, KEYBIND ->
          resp.equalsIgnoreCase(mod.key()) ? ModCheckResult.NOT_DETECTED : ModCheckResult.DETECTED;
      case EXPLOIT_PREVENTER ->
          resp.equalsIgnoreCase(mod.key()) ? ModCheckResult.DETECTED : ModCheckResult.NOT_DETECTED;
    };
  }

  // -------------------------------------------------------------------------
  // Finish
  // -------------------------------------------------------------------------

  private void finishCheck() {
    UUID uuid = target.getUniqueId();
    playerCheckMap.remove(uuid);
    output.fine(MessageUtil.finishStart(target.getName(),
        batches.stream().mapToInt(b -> b.length).sum()));

    int detected = 0;
    int punished = 0;

    for (int i = 0; i < batches.size(); i++) {
      ModCheck[] batch = batches.get(i);
      String[] collectedResponse = i < collectedResponses.size() ? collectedResponses.get(i) : null;

      for (int j = 0; j < batch.length; j++) {
        ModCheckResult result;
        ModCheck mod = batch[j];
        if (collectedResponse == null) {
          result = ModCheckResult.NOT_RECEIVED;
        } else if (collectedResponse[j] != null) {
          result = evaluateResponse(mod, collectedResponse[j].strip());
        } else {
          result = ModCheckResult.FAILED;
        }

        if (result == ModCheckResult.DETECTED) {
          detected++;
        }

        output.info(MessageUtil.modResult(mod.displayName(), result.toString()));

        String action = plugin.getPunishmentUtil().executePunishment(target, mod, result);
        if (!action.isBlank()) {
          punished++;
          output.fine(
              MessageUtil.punishmentIssued(target.getName(), mod.displayName(), action));
          sendToAdmins(MessageUtil.detectionAlert(target.getName(), mod.displayName(), action));
        }
      }
    }

    output.info(MessageUtil.checkFinished(target.getName(), detected, punished));
  }

  private void sendToAdmins(@NonNull String msg) {
    new CommandOutput(
        plugin,
        Bukkit.getOnlinePlayers()
            .stream()
            .filter(p -> p.hasPermission("unhacked.echo-results"))
            .toArray(CommandSender[]::new)
    ).info(msg);

  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private Component buildModComponent(@NonNull ModCheck mod) {
    return switch (mod.mode()) {
      case TRANSLATE, EXPLOIT_PREVENTER -> Component.translatable(mod.key());
      case KEYBIND -> Component.keybind(mod.key());
    };
  }

  private static List<ModCheck[]> buildBatches(@NonNull Iterable<ModCheck> mods) {
    List<ModCheck[]> batches = new ArrayList<>();
    ModCheck[] buffer = new ModCheck[LINES_PER_SIGN];
    int index = 0;

    for (ModCheck mod : mods) {
      buffer[index] = mod;
      index++;

      if (index != LINES_PER_SIGN) {
        continue;
      }

      batches.add(buffer);
      buffer = new ModCheck[LINES_PER_SIGN];
      index = 0;
    }

    if (index > 0) {
      batches.add(Arrays.copyOf(buffer, index));
    }

    return batches;
  }
}