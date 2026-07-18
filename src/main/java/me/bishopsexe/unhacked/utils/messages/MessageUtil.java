/*
 * SPDX-FileCopyrightText: Copyright 2026 Bishops_exe and Unhacked authors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package me.bishopsexe.unhacked.utils.messages;

import java.util.Map;
import lombok.experimental.UtilityClass;
import lombok.NonNull;

@UtilityClass
public class MessageUtil {

  public static @NonNull String playerNotFound(@NonNull String player) {
    return PlaceholderUtil.parse("player-not-found", Map.of("player", player));
  }

  public static @NonNull String alreadyChecking(@NonNull String player) {
    return PlaceholderUtil.parse("already-checking", Map.of("player", player));
  }

  public static @NonNull String checkStarted(@NonNull String player) {
    return PlaceholderUtil.parse("check-started", Map.of("player", player));
  }

  public static @NonNull String reloadDone() {
    return PlaceholderUtil.parse("reload-done", Map.of());
  }

  public static @NonNull String invalidMod(@NonNull String mod) {
    return PlaceholderUtil.parse("invalid-mod", Map.of("mod", mod));
  }

  public static @NonNull String detectionAlert(@NonNull String player, @NonNull String mod,
      @NonNull String action) {
    return PlaceholderUtil.parse("detection-alert",
        Map.of("player", player, "mod", mod, "action", action));
  }

  public static @NonNull String checkBatches(@NonNull String player, int batches, int total) {
    return PlaceholderUtil.parse("check-batches",
        Map.of("player", player, "batches", String.valueOf(batches), "total",
            String.valueOf(total)));
  }

  public static @NonNull String batchSending(@NonNull String player, int batch, int total,
      int x, int y, int z) {
    return PlaceholderUtil.parse("batch-sending",
        Map.of("player", player, "batch", String.valueOf(batch), "total", String.valueOf(total),
            "x", String.valueOf(x), "y", String.valueOf(y), "z", String.valueOf(z)));
  }

  public static @NonNull String batchesSent(@NonNull String player) {
    return PlaceholderUtil.parse("batches-sent", Map.of("player", player));
  }

  public static @NonNull String timeout(@NonNull String player, int received, int total) {
    return PlaceholderUtil.parse("timeout",
        Map.of("player", player, "received", String.valueOf(received), "total",
            String.valueOf(total)));
  }

  public static @NonNull String batchReceived(@NonNull String player, int received, int total,
      @NonNull String lines) {
    return PlaceholderUtil.parse("batch-received",
        Map.of("player", player, "received", String.valueOf(received), "total",
            String.valueOf(total), "lines", lines));
  }

  public static @NonNull String finishStart(@NonNull String player, int total) {
    return PlaceholderUtil.parse("finish-start",
        Map.of("player", player, "total", String.valueOf(total)));
  }

  public static @NonNull String modResult(@NonNull String mod, @NonNull String result) {
    return PlaceholderUtil.parse("mod-result", Map.of("mod", mod, "result", result));
  }

  public static @NonNull String punishmentIssued(@NonNull String player, @NonNull String mod,
      @NonNull String action) {
    return PlaceholderUtil.parse("punishment-issued",
        Map.of("player", player, "mod", mod, "action", action));
  }

  public static @NonNull String checkFinished(@NonNull String player, int detected,
      int punished) {
    return PlaceholderUtil.parse("check-finished",
        Map.of("player", player, "detected", String.valueOf(detected), "punished",
            String.valueOf(punished)));
  }

  public static @NonNull String loglevelCurrent(@NonNull String level, @NonNull String levels) {
    return PlaceholderUtil.parse("loglevel-current", Map.of("level", level, "levels", levels));
  }

  public static @NonNull String loglevelSet(@NonNull String level) {
    return PlaceholderUtil.parse("loglevel-set", Map.of("level", level));
  }

  public static @NonNull String loglevelInvalid(@NonNull String level, @NonNull String levels) {
    return PlaceholderUtil.parse("loglevel-invalid", Map.of("level", level, "levels", levels));
  }
}
