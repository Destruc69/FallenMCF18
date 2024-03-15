package paul.fallen.module.modules.movement;

import net.minecraft.item.BowItem;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CConfirmTeleportPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import paul.fallen.module.Module;
import paul.fallen.packetevent.PacketEvent;
import paul.fallen.setting.Setting;
import paul.fallen.utils.client.MathUtils;
import paul.fallen.utils.entity.EntityUtils;
import paul.fallen.utils.entity.InventoryUtils;

import java.util.ArrayList;
import java.util.Arrays;

public final class Flight extends Module {

    private final Setting mode;
    private final Setting upSpeed;
    private final Setting baseSpeed;
    private final Setting downSpeed;
    private final Setting ncpStrength;

    private double flyHeight;

    private boolean a = true;
    private double count = 0;

    public Flight(int bind, String name, String displayName, Category category) {
        super(bind, name, displayName, category);

        mode = new Setting("mode", "Mode", this, "vanilla", new ArrayList<>(Arrays.asList("vanilla", "ncp", "ghostly", "fallen", "bow")));
        upSpeed = new Setting("up-speed", "Up-Speed", this, 1.0F, (float) 0.005, 10.0F);
        baseSpeed = new Setting("base-speed", "Base-Speed", this, 1.0F, (float) 0.005, 10.0F);
        downSpeed = new Setting("down-speed", "Down-Speed", this, 1.0F, (float) 0.005, 10.0F);
        ncpStrength = new Setting("ncp-strength", "NCP-Strength", this, 1, 1, 20);

        addSetting(mode);
        addSetting(upSpeed);
        addSetting(baseSpeed);
        addSetting(downSpeed);
        addSetting(ncpStrength);
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        try {
            if (mode.sval == "ncp") {
                assert mc.player != null;
                if (mc.player.ticksExisted > 20) {
                    if (!mc.player.isOnGround()) {
                        if (mc.gameSettings.keyBindJump.isKeyDown()) {
                            mc.player.setVelocity(0, +upSpeed.dval, 0);
                        } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                            mc.player.setVelocity(0, -downSpeed.dval, 0);
                        } else {
                            EntityUtils.setMotionY(0);
                        }

                        if (!mc.gameSettings.keyBindForward.isKeyDown() && !mc.gameSettings.keyBindRight.isKeyDown() && !mc.gameSettings.keyBindBack.isKeyDown() && !mc.gameSettings.keyBindLeft.isKeyDown() && !mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()) {
                            mc.player.setVelocity(0, 0, 0);
                        }

                        if (mc.player.ticksExisted % 2 == 0) {
                            mc.player.fallDistance = 50000 + Math.round(Math.random() * 50000);
                        } else {
                            mc.player.fallDistance = 50000 - Math.round(Math.random() * 50000);
                        }

                        if (mc.player.getMotion().x > 0.26 || mc.player.getMotion().x < -0.26 ||
                                mc.player.getMotion().z > 0.26 || mc.player.getMotion().z < -0.26) {
                            if (mc.player.getMotion().x > 0) {
                                EntityUtils.setMotionX(mc.player.getMotion().x - 0.05);
                            } else if (mc.player.getMotion().x < 0) {
                                EntityUtils.setMotionX(mc.player.getMotion().x + 0.05);
                            }
                            if (mc.player.getMotion().z > 0) {
                                EntityUtils.setMotionZ(mc.player.getMotion().z - 0.05);
                            } else if (mc.player.getMotion().z < 0) {
                                EntityUtils.setMotionZ(mc.player.getMotion().z + 0.05);
                            }
                        }

                        for (int a = 0; a < ncpStrength.dval; a++) {
                            mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX() + mc.player.getMotion().x, mc.player.getPosY() + mc.player.getMotion().y, mc.player.getPosZ() + mc.player.getMotion().z, mc.player.rotationYaw, mc.player.rotationPitch, false));
                            if (mc.player.ticksExisted % 2 == 0) {
                                mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX() + mc.player.getMotion().x, mc.player.getMotion().y + mc.player.getMotion().y + 50000 + Math.round(Math.random() * 50000), mc.player.getPosZ() + mc.player.getMotion().z, mc.player.rotationYaw, mc.player.rotationPitch, false));
                            } else {
                                mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX() + mc.player.getMotion().x, mc.player.getPosY() + mc.player.getMotion().y - 50000 - Math.round(Math.random() * 50000), mc.player.getPosZ() + mc.player.getMotion().z, mc.player.rotationYaw, mc.player.rotationPitch, false));
                            }
                        }
                    } else {
                        mc.player.jump();
                    }
                }
            } else if (mode.sval == "ghostly") {
                assert mc.player != null;
                mc.player.setOnGround(true);
                mc.player.isAirBorne = false;
                mc.player.fallDistance = 0;
                mc.player.collidedHorizontally = true;
                mc.player.collidedVertically = false;
                if (!mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()) {
                    EntityUtils.setMotionY(0);
                    MathUtils.setSpeed(baseSpeed.dval);
                    if (a) {
                        mc.player.setPosition(mc.player.lastTickPosX, mc.player.lastTickPosY + 0.025, mc.player.lastTickPosZ);
                        a = false;
                    } else {
                        mc.player.setPosition(mc.player.lastTickPosX, mc.player.lastTickPosY - 0.025, mc.player.lastTickPosZ);
                        a = true;
                    }
                } else if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    EntityUtils.setMotionY(upSpeed.dval);
                } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    EntityUtils.setMotionY(-downSpeed.dval);
                }
            } else if (mode.sval == "vanilla") {
                assert mc.player != null;
                mc.player.setMotion(0, 0, 0);
                MathUtils.setSpeed(baseSpeed.dval);

                if (mc.gameSettings.keyBindJump.isKeyDown())
                    EntityUtils.setMotionY(upSpeed.dval);
                if (mc.gameSettings.keyBindSneak.isKeyDown())
                    EntityUtils.setMotionY(-downSpeed.dval);
            } else if (mode.sval == "fallen") {
                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    EntityUtils.setMotionY(upSpeed.dval * 0.6);
                } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                    EntityUtils.setMotionY(-downSpeed.dval * 0.6);
                } else {
                    EntityUtils.setMotionY(0);
                }
                updateFlyHeight();
                mc.player.connection.sendPacket(new CPlayerPacket(true));
                if (((this.flyHeight <= 290.0D) && (mc.player.ticksExisted % 10 == 0))
                        || ((this.flyHeight > 290.0D) && (mc.player.ticksExisted % 2 == 0))) {
                    goToGround();
                }
            } else if (mode.sval == "bow") {
                InventoryUtils.setSlot(InventoryUtils.getSlot(Items.BOW));

                if (mc.player.getHeldItemMainhand().getItem() != null &&
                        mc.player.getHeldItemMainhand().getItem() instanceof BowItem) {
                    CPlayerDiggingPacket C07 = new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN);
                    CPlayerTryUseItemOnBlockPacket C08 = new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, new BlockRayTraceResult(new Vector3d(0, 0, 0), Direction.DOWN, BlockPos.ZERO, false));
                    float yaw = mc.player.rotationYaw;
                    float pitch = -90;
                    if (mc.player.moveForward != 0 || mc.player.moveStrafing != 0) {
                        pitch = -80;
                    }
                    if (mc.player.moveForward < 0) {
                        yaw -= 180f;
                    }
                    if (mc.player.getMotion().y < -0.1) {
                        pitch = 90f;
                    }
                    mc.player.rotationPitch = pitch;
                    mc.player.rotationYaw = yaw;
                    if (mc.player.isOnGround() && mc.player.collidedVertically) {
                        mc.player.jump();
                    } else {
                        //if (mc.player.getMotion().y < 0) {
                        //    mc.timer.timerSpeed = 0.1f;
                        //} else {
                        //    if (mc.timer.timerSpeed == 0.1f) {
                        //        mc.timer.timerSpeed = 1f;
                        //    }
                        //}
                    }
                    count++;
                    if (count >= 4) {
                        mc.player.connection.sendPacket(C07);
                        count = 0;
                    } else if (count == 1) {
                        mc.player.connection.sendPacket(C08);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public void onPackets(PacketEvent event) {
        assert mc.player != null;
        if (mc.player.ticksExisted > 20) {
            if (!mc.player.isOnGround()) {
                if (mode.sval == "ncp") {
                    if (event.getPacket() instanceof SPlayerPositionLookPacket) {
                        SPlayerPositionLookPacket sPacketPlayerPosLook = (SPlayerPositionLookPacket) event.getPacket();
                        for (int x = 0; x < ncpStrength.dval; x++) {
                            mc.player.connection.sendPacket(new CConfirmTeleportPacket(sPacketPlayerPosLook.getTeleportId()));
                            mc.player.connection.sendPacket(new CPlayerPacket.PositionRotationPacket(sPacketPlayerPosLook.getX(), sPacketPlayerPosLook.getY(), sPacketPlayerPosLook.getZ(), sPacketPlayerPosLook.getYaw(), sPacketPlayerPosLook.getPitch(), false));
                        }
                        mc.player.setPosition(sPacketPlayerPosLook.getX(), sPacketPlayerPosLook.getY(), sPacketPlayerPosLook.getZ());
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    public void updateFlyHeight() {
        double h = 1.0D;
        AxisAlignedBB box = mc.player.getBoundingBox().expand(0.0625D, 0.0625D, 0.0625D);
        for (this.flyHeight = 0.0D; this.flyHeight < mc.player.getPosY(); this.flyHeight += h) {
            AxisAlignedBB nextBox = box.offset(0.0D, -this.flyHeight, 0.0D);
            if (!mc.world.hasNoCollisions(nextBox)) {
                if (h < 0.0625D) {
                    break;
                }
                this.flyHeight -= h;
                h /= 2.0D;
            }
        }
    }

    public void goToGround() {
        if (this.flyHeight > 300.0D) {
            return;
        }
        double minY = mc.player.getPosY() - this.flyHeight;
        if (minY <= 0.0D) {
            return;
        }
        for (double y = mc.player.getPosY(); y > minY; ) {
            y -= 8.0D;
            if (y < minY) {
                y = minY;
            }
            CPlayerPacket.PositionPacket packet = new CPlayerPacket.PositionPacket(
                    mc.player.getPosX(), y, mc.player.getPosZ(), true);
            mc.player.connection.sendPacket(packet);
        }
        for (double y = minY; y < mc.player.getPosY(); ) {
            y += 8.0D;
            if (y > mc.player.getPosY()) {
                y = mc.player.getPosY();
            }
            CPlayerPacket.PositionPacket packet = new CPlayerPacket.PositionPacket(
                    mc.player.getPosX(), y, mc.player.getPosZ(), true);
            mc.player.connection.sendPacket(packet);
        }
    }
}