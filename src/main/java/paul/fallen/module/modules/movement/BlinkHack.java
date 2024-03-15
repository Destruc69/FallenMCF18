/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package paul.fallen.module.modules.movement;

import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import paul.fallen.module.Module;
import paul.fallen.packetevent.PacketEvent;
import paul.fallen.setting.Setting;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;

public final class BlinkHack extends Module {
    private final Setting limit;

    private final Deque<CPlayerPacket> packets = new LinkedList<>();

    public BlinkHack(int bind, String name, String displayName, Category category) {
        super(bind, name, displayName, category);
        limit = new Setting("Limit", "Limit", this, 100, 20, 500);
        addSetting(limit);
    }

    @Override
    public void onDisable() {
        packets.forEach(p -> Objects.requireNonNull(mc.getConnection()).sendPacket(p));
        packets.clear();
    }

    @SubscribeEvent
    public void onUpdate(TickEvent.ClientTickEvent event) {
        if (limit.dval == 0)
            return;

        if (packets.size() >= (int) limit.dval) {
            setState(false);
        }
    }

    @SubscribeEvent
    public void onPacketOutput(PacketEvent event) {
        if (!(event.getPacket() instanceof CPlayerPacket))
            return;

        event.setCanceled(true);

        CPlayerPacket packet = (CPlayerPacket) event.getPacket();
        CPlayerPacket prevPacket = packets.peekLast();

        if (prevPacket != null && packet.isOnGround() == prevPacket.isOnGround()
                && packet.getYaw(-1) == prevPacket.getYaw(-1)
                && packet.getPitch(-1) == prevPacket.getPitch(-1)
                && packet.getX(-1) == prevPacket.getX(-1)
                && packet.getY(-1) == prevPacket.getY(-1)
                && packet.getZ(-1) == prevPacket.getZ(-1))
            return;

        packets.addLast(packet);
    }
}
