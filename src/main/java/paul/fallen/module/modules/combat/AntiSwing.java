/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package paul.fallen.module.modules.combat;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import paul.fallen.module.Module;

public final class AntiSwing extends Module {

    public AntiSwing(int bind, String name, String displayName, Category category) {
        super(bind, name, displayName, category);
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        try {
            assert mc.player != null;
            mc.player.swingTime = 0;


            PlayerEvent.StartTracking

            Player player = MinecraftClient..player;
            if (player != null) {
                PlayerInteractionManager interactionManager = player.interactionManager;
                // Now you can use interactionManager for player interactions
            } else {
                // Handle case when player is not available
            }




        } catch (Exception ignored) {
        }
    }

}