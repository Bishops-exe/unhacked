/*
 * SPDX-FileCopyrightText: Copyright 2026 Bishops_exe and Unhacked authors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package me.bishopsexe.unhacked.utils.sign;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.nbt.NBTByte;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.world.blockentity.BlockEntityTypes;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockEntityData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCloseWindow;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenSignEditor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class SignPacketUtil {

  public void openFor(@NonNull Player player) {
    PacketEvents.getAPI().getPlayerManager().sendPacket(
        player,
        new WrapperPlayServerOpenSignEditor(new Vector3i(x, y, z), true)
    );
  }

  public void closeFor(@NonNull Player player) {
    PacketEvents.getAPI().getPlayerManager().sendPacket(
        player,
        new WrapperPlayServerCloseWindow()
    );
  }

  @Getter
  private final int x, y, z;
  private final Side front = new Side();
  private final Side back = new Side();

  public SignPacketUtil(Location location) {
    this(location.getBlockX(), location.getBlockY(), location.getBlockZ());
  }

  public Side front() {
    return front;
  }

  public Side back() {
    return back;
  }


  public void send(Player player) {
    NBTCompound tag = new NBTCompound();
    tag.setTag("front_text", front.toNBT());
    tag.setTag("back_text", back.toNBT());
    tag.setTag("is_waxed", new NBTByte((byte) 0));

    WrapperPlayServerBlockEntityData packet = new WrapperPlayServerBlockEntityData(
        new Vector3i(x, y, z),
        BlockEntityTypes.SIGN,
        tag
    );

    PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);

  }
}
