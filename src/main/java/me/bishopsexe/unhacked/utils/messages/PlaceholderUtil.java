/*
 * SPDX-FileCopyrightText: Copyright 2026 Bishops_exe and Unhacked authors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package me.bishopsexe.unhacked.utils.messages;

import java.util.Map;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import me.bishopsexe.unhacked.UnhackedPlugin;

@UtilityClass
public class PlaceholderUtil {
  public static String parse(@NonNull String messageKey,
      @NonNull Map<String, String> placeholders) {
    UnhackedPlugin plugin = UnhackedPlugin.getInstance();
    return parseRaw(plugin.getConfigManager().getMessage(messageKey), placeholders);
  }

  public static String parseRaw(@NonNull String raw, @NonNull Map<String, String> placeholders) {
    for (Map.Entry<String, String> e : placeholders.entrySet()) {
      raw = raw.replace("{%s}".formatted(e.getKey()), e.getValue());
    }

    return raw;
  }
}