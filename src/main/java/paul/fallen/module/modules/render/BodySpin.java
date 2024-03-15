package paul.fallen.module.modules.render;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import paul.fallen.module.Module;
import paul.fallen.setting.Setting;

import java.util.ArrayList;
import java.util.Arrays;

public class BodySpin extends Module {

    private Setting mode;

    public BodySpin(int bind, String name, String displayName, Category category) {
        super(bind, name, displayName, category);

        mode = new Setting("Mode", this, "spin", new ArrayList<>(Arrays.asList("spin", "backwards")));
        addSetting(mode);
    }

    private float yaw = 0;

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        try {
            if (mode.sval == "spin") {
                mc.player.renderYawOffset = yaw;
                if (yaw + 1 <= 360) {
                    yaw++;
                } else {
                    yaw = 0;
                }
            } else if (mode.sval == "backwards") {
                mc.player.renderYawOffset = mc.player.rotationYaw + 180;
            }
        } catch (Exception ignored) {
        }
    }
}
