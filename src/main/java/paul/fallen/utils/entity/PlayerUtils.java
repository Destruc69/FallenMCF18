package paul.fallen.utils.entity;

import com.mojang.math.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import paul.fallen.ClientSupport;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

public class PlayerUtils implements ClientSupport {

    public static void setMoveSpeed(double speed) {
        double forward = getForward();
        double strafe = getStrafe();
        float yaw = mc.player.rotA;
        if (forward != 0) {
            if (strafe > 0) {
                yaw += ((forward > 0) ? -45 : 45);
            } else if (strafe < 0) {
                yaw += ((forward > 0) ? 45 : -45);
            }
            strafe = 0;
            if (forward > 0) {
                forward = 1;
            } else {
                forward = -1;
            }
        }
        mc.player.setDeltaMovement(forward * speed * Math.cos(Math.toRadians((yaw + 90.0F))) + strafe * speed * Math.sin(Math.toRadians((yaw + 90.0F))), mc.player.getDeltaMovement().y, forward * speed * Math.sin(Math.toRadians((yaw + 90.0F))) - strafe * speed * Math.cos(Math.toRadians((yaw + 90.0F))));
    }

    public static double[] getMotions(double speed) {
        double forward = getForward();
        double strafe = getStrafe();
        float yaw = mc.player.rotA;
        if (forward != 0) {
            if (strafe > 0) {
                yaw += ((forward > 0) ? -45 : 45);
            } else if (strafe < 0) {
                yaw += ((forward > 0) ? 45 : -45);
            }
            strafe = 0;
            if (forward > 0) {
                forward = 1;
            } else {
                forward = -1;
            }
        }
        return new double[]{forward * speed * Math.cos(Math.toRadians((yaw + 90.0F))) + strafe * speed * Math.sin(Math.toRadians((yaw + 90.0F))), forward * speed * Math.sin(Math.toRadians((yaw + 90.0F))) - strafe * speed * Math.cos(Math.toRadians((yaw + 90.0F)))};
    }

    public static int getForward() {
        if (mc.options.keyUp.isDown()) {
            return 1;
        } else if (mc.options.keyDown.isDown()) {
            return -1;
        } else {
            return 0;
        }
    }

    public static int getStrafe() {
        if (mc.options.keyRight.isDown()) {
            return 1;
        } else if (mc.options.keyLeft.isDown()) {
            return -1;
        } else {
            return 0;
        }
    }

    public static float getDirection() {
        float yaw = mc.player.rotA;
        float forward = getForward();
        float strafe = getStrafe();
        yaw += (forward < 0.0F ? 180 : 0);
        if (strafe < 0.0F) {
            yaw += (forward == 0.0F ? 90 : forward < 0.0F ? -45 : 45);
        }
        if (strafe > 0.0F) {
            yaw -= (forward == 0.0F ? 90 : forward < 0.0F ? -45 : 45);
        }
        return yaw * 0.017453292F;
    }

    public static float getSpeed() {
        return (float) Math.sqrt(mc.player.getDeltaMovement().x * mc.player.getDeltaMovement().x + mc.player.getDeltaMovement().z * mc.player.getDeltaMovement().z);
    }

    public static void setSpeed(double speed) {
        mc.player.setDeltaMovement(-Math.sin(getDirection()) * speed, mc.player.getDeltaMovement().y, Math.cos(getDirection()) * speed);
    }

    public static void setTickSpeed(float speed) {
        try {
            // Get Minecraft instance
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();

            // Get the 'timer' field from the Minecraft class
            Field timerField = mc.getClass().getDeclaredField("timer");

            // Set the accessibility of the field to true to be able to access it
            timerField.setAccessible(true);

            // Get the value of the 'timer' field
            Object timer = timerField.get(mc);

            // Get the 'tickLength' field from the Timer class
            Field tickLengthField = timer.getClass().getDeclaredField("tickLength");

            // Set the accessibility of the field to true to be able to access it
            tickLengthField.setAccessible(true);

            // Set the custom tick speed
            tickLengthField.set(timer, 50 / speed);












            // Get the 'timer' field from the Minecraft class
            Field timerFieldA = mc.getClass().getDeclaredField("field_71428_T");

            // Set the accessibility of the field to true to be able to access it
            timerFieldA.setAccessible(true);

            // Get the value of the 'timer' field
            Object timerA = timerField.get(mc);

            // Get the 'tickLength' field from the Timer class
            Field tickLengthFieldA = timerA.getClass().getDeclaredField("field_194149_e");

            // Set the accessibility of the field to true to be able to access it
            tickLengthFieldA.setAccessible(true);

            // Set the custom tick speed
            tickLengthFieldA.set(timer, 50 / speed);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
