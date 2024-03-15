package paul.fallen.module.modules.render;

import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import paul.fallen.module.Module;

public class AntiRender extends Module {

    public AntiRender(int bind, String name, String displayName, Category category) {
        super(bind, name, displayName, category);
    }

    @SubscribeEvent
    public void onRender(RenderBlockOverlayEvent event) {
        event.setCanceled(true);
    }
}
