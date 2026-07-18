/*
 * SPDX-FileCopyrightText: Copyright 2026 Bishops_exe and Unhacked authors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package me.bishopsexe.unhacked.events;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUpdateSign;
import java.util.UUID;
import lombok.NonNull;
import me.bishopsexe.unhacked.check.CheckProcess;

public class SignEvent extends PacketListenerAbstract {

  public SignEvent() {
    super(PacketListenerPriority.HIGHEST);
  }

  @Override
  public void onPacketReceive(@NonNull PacketReceiveEvent event) {
    if (event.getPacketType() != PacketType.Play.Client.UPDATE_SIGN) {
      return;
    }

    UUID uuid = event.getUser().getUUID();
    CheckProcess check = CheckProcess.getCheckProcess(uuid);
    if (check == null) {
      return;
    }

    event.setCancelled(true);

    String[] raw = new WrapperPlayClientUpdateSign(event).getTextLines();
    check.handleBatchResponse(raw);
  }
}
