/*
 * SPDX-FileCopyrightText: Copyright 2026 Bishops_exe and Unhacked authors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package me.bishopsexe.unhacked.utils.sign;

import com.github.retrooper.packetevents.protocol.nbt.NBTByte;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTList;
import com.github.retrooper.packetevents.protocol.nbt.NBTType;
import lombok.NonNull;


public class Side {

  private final SignComponent[] lines = new SignComponent[4];

  public Side line(int index, @NonNull SignComponent component) {
    lines[index] = component;
    return this;
  }

  NBTCompound toNBT() {
    NBTList<NBTCompound> messages = new NBTList<>(NBTType.COMPOUND);
    for (SignComponent component : lines) {
      messages.addTag(component != null ? component.toNBT() : new SignComponent.Text("").toNBT());
    }
    NBTCompound side = new NBTCompound();
    side.setTag("messages", messages);
    side.setTag("has_glowing_text", new NBTByte((byte) 0));
    return side;
  }
}