/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package paul.fallen.module.modules.world;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import paul.fallen.module.Module;
import paul.fallen.setting.Setting;

public final class AntiWeather extends Module {

    Setting opposite;

    public AntiWeather(int bind, String name, String displayName, Category category) {
        super(bind, name, displayName, category);

        opposite = new Setting("Opposite", this, false);
        addSetting(opposite);
    }

    @Override
    public void onDisable() {
        try {
            if (opposite.bval) {
                assert mc.level != null;
                mc.level.rainLevel = 0;
            }
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        try {
            if (!opposite.bval) {
                mc.level.rainLevel = 0;
            } else {
                mc.level.rainLevel = 1;
            }
        } catch (Exception ignored) {
        }
    }
}