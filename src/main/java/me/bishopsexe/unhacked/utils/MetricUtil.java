package me.bishopsexe.unhacked.utils;

import dev.faststats.Metrics;
import dev.faststats.bukkit.BukkitContext;
import lombok.RequiredArgsConstructor;
import me.bishopsexe.unhacked.UnhackedPlugin;

@RequiredArgsConstructor
public class MetricUtil {
  public String token = "d1a83a0b497b7fe72cd6c9f2670b641c";
  private static BukkitContext context = null;
  private final UnhackedPlugin plugin;

  private BukkitContext createContext() {
    return new BukkitContext.Factory(plugin, token)
            .metrics(Metrics.Factory::create)
            .create();
  }

  public void enable() {
    if (context != null) {
      throw new RuntimeException("Is already running!");
    }

    context = createContext();

    context.ready();
  }

  public void disable() {
    if (context == null) {
      throw new RuntimeException("Is not running!");
    }

    context.shutdown();

    context = null;
  }
}
