/*
 * SPDX-FileCopyrightText: Copyright 2026 Bishops_exe and Unhacked authors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package me.bishopsexe.unhacked.utils.sign;

import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public sealed interface SignComponent permits SignComponent.Text, SignComponent.Keybind, SignComponent.Translate {
  record Text(String text) implements SignComponent {

    public NBTCompound toNBT() {
      NBTCompound c = new NBTCompound();
      c.setTag("text", new NBTString(text));
      return c;
    }
  }

  record Keybind(String key) implements SignComponent {

    public NBTCompound toNBT() {
      NBTCompound c = new NBTCompound();
      c.setTag("keybind", new NBTString(key));
      return c;
    }
  }

  record Translate(String key) implements SignComponent {

    public NBTCompound toNBT() {
      NBTCompound c = new NBTCompound();
      c.setTag("translate", new NBTString(key));
      return c;
    }
  }



  NBTCompound toNBT();

  static SignComponent fromAdventure(Component component) {
    return switch (component) {
      case KeybindComponent k    -> new Keybind(k.keybind());
      case TranslatableComponent t -> new Translate(t.key());
      case TextComponent t       -> new Text(t.content());
      default -> new Text(PlainTextComponentSerializer.plainText().serialize(component));
    };
  }
}