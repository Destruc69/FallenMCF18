package paul.fallen.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.WorldGenAttemptRenderer;
import net.minecraft.resources.ResourceLocation;
import paul.fallen.ClientSupport;

public class UIUtils {

    // Method to draw text on the screen
    public static void drawTextOnScreen(String text, int x, int y, int color) {
        Font fontRenderer = Minecraft.getInstance().font;
        fontRenderer.drawShadow(new PoseStack(), text, x, y, color);
    }

    // Method to draw a filled rectangle on the screen
    public static void drawRect(int x, int y, int width, int height, int color) {
        GuiComponent.fill(new PoseStack(), x, y, x + width, y + height, color);
    }

    public static void drawRect(double x, double y, double width, double height, int color) {
        fill(x, y, x + width, y + height, color);
    }

    // Method to draw a filled gradient rectangle on the screen
    public static void drawGradientRect(double x, double y, double width, double height, int startColor, int endColor) {
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // Interpolate between startColor and endColor diagonally
        for (double yOffset = 0; yOffset < height; yOffset++) {
            float ratioY = (float) yOffset / (float) height;
            for (double xOffset = 0; xOffset < width; xOffset++) {
                float ratioX = (float) xOffset / (float) width;
                float ratio = (ratioX + ratioY) / 2.0f;
                int color = interpolateColor(startColor, endColor, ratio);
                float alpha = (float) (color >> 24 & 255) / 255.0F;
                float red = (float) (color >> 16 & 255) / 255.0F;
                float green = (float) (color >> 8 & 255) / 255.0F;
                float blue = (float) (color & 255) / 255.0F;

                bufferBuilder.vertex(x + xOffset, y + yOffset + 1, 0.0F).color(red, green, blue, alpha).endVertex();
                bufferBuilder.vertex(x + xOffset + 1, y + yOffset + 1, 0.0F).color(red, green, blue, alpha).endVertex();
                bufferBuilder.vertex(x + xOffset + 1, y + yOffset, 0.0F).color(red, green, blue, alpha).endVertex();
                bufferBuilder.vertex(x + xOffset, y + yOffset, 0.0F).color(red, green, blue, alpha).endVertex();
            }
        }

        bufferBuilder.end();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        bufferSource.endBatch();

        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private static int interpolateColor(int startColor, int endColor, float ratio) {
        Vector4f startVec = unpackColor(startColor);
        Vector4f endVec = unpackColor(endColor);

        float red = lerp(startVec.x(), endVec.x(), ratio);
        float green = lerp(startVec.y(), endVec.y(), ratio);
        float blue = lerp(startVec.z(), endVec.z(), ratio);
        float alpha = lerp(startVec.w(), endVec.w(), ratio);

        return packColor(red, green, blue, alpha);
    }

    private static Vector4f unpackColor(int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        return new Vector4f(red, green, blue, alpha);
    }

    private static int packColor(float red, float green, float blue, float alpha) {
        int r = (int) (red * 255);
        int g = (int) (green * 255);
        int b = (int) (blue * 255);
        int a = (int) (alpha * 255);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static float lerp(float a, float b, float ratio) {
        return a + ratio * (b - a);
    }

    private static void fillGradient(double minX, double minY, double maxX, double maxY, int color1, int color2) {
        if (minX < maxX) {
            double i = minX;
            minX = maxX;
            maxX = i;
        }

        if (minY < maxY) {
            double j = minY;
            minY = maxY;
            maxY = j;
        }

        float f3 = (float) (color1 >> 24 & 255) / 255.0F;
        float f = (float) (color1 >> 16 & 255) / 255.0F;
        float f1 = (float) (color1 >> 8 & 255) / 255.0F;
        float f2 = (float) (color1 & 255) / 255.0F;

        float f4 = (float) (color2 >> 24 & 255) / 255.0F;
        float f5 = (float) (color2 >> 16 & 255) / 255.0F;
        float f6 = (float) (color2 >> 8 & 255) / 255.0F;
        float f7 = (float) (color2 & 255) / 255.0F;

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // Draw vertices with interpolated colors
        for (double y = minY; y < maxY; y++) {
            float lerpFactor = (float) ((y - minY) / (maxY - minY));
            float r = f * (1 - lerpFactor) + f5 * lerpFactor;
            float g = f1 * (1 - lerpFactor) + f6 * lerpFactor;
            float b = f2 * (1 - lerpFactor) + f7 * lerpFactor;
            float a = f3 * (1 - lerpFactor) + f4 * lerpFactor;

            bufferbuilder.vertex(minX, y, 0).color(r, g, b, a).endVertex();
            bufferbuilder.vertex(maxX, y, 0).color(r, g, b, a).endVertex();
            bufferbuilder.vertex(maxX, y + 1, 0).color(r, g, b, a).endVertex();
            bufferbuilder.vertex(minX, y + 1, 0).color(r, g, b, a).endVertex();
        }

        bufferbuilder.end();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        bufferSource.endBatch();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private static void fill(double minX, double minY, double maxX, double maxY, int color) {
        if (minX < maxX) {
            double i = minX;
            minX = maxX;
            maxX = i;
        }

        if (minY < maxY) {
            double j = minY;
            minY = maxY;
            maxY = j;
        }

        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.vertex(minX, maxY, 0.0F).color(red, green, blue, alpha).endVertex();
        bufferbuilder.vertex(maxX, maxY, 0.0F).color(red, green, blue, alpha).endVertex();
        bufferbuilder.vertex(maxX, minY, 0.0F).color(red, green, blue, alpha).endVertex();
        bufferbuilder.vertex(minX, minY, 0.0F).color(red, green, blue, alpha).endVertex();
        bufferbuilder.end();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        bufferSource.endBatch();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}