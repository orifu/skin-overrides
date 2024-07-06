package net.orifu.skin_overrides.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.Skin;
import net.orifu.xplat.gui.GuiGraphics;

public class PlayerSkinRenderer {
    public static final int HEAD_SIZE = 8;
    public static final int HEAD_U = 8;
    public static final int HEAD_V = 8;
    public static final int HEAD_LAYER_U = 40;
    public static final int HEAD_LAYER_V = 8;
    public static final int HEAD_X = 4;
    public static final int HEAD_Y = 0;

    public static final int TORSO_WIDTH = 8;
    public static final int TORSO_HEIGHT = 12;
    public static final int TORSO_U = 20;
    public static final int TORSO_V = 20;
    public static final int TORSO_LAYER_U = 20;
    public static final int TORSO_LAYER_V = 36;
    public static final int TORSO_X = 4;
    public static final int TORSO_Y = HEAD_SIZE;

    public static final int ARM_WIDE_WIDTH = 4;
    public static final int ARM_SLIM_WIDTH = 3;
    public static final int ARM_HEIGHT = 12;

    public static final int ARM_RIGHT_U = 44;
    public static final int ARM_RIGHT_V = 20;
    public static final int ARM_RIGHT_LAYER_U = 44;
    public static final int ARM_RIGHT_LAYER_V = 36;
    public static final int ARM_RIGHT_X = 0;
    public static final int ARM_RIGHT_Y = HEAD_SIZE;

    public static final int ARM_LEFT_U = 36;
    public static final int ARM_LEFT_V = 52;
    public static final int ARM_LEFT_LAYER_U = 52;
    public static final int ARM_LEFT_LAYER_V = 52;
    public static final int ARM_LEFT_X = 4 + TORSO_WIDTH;
    public static final int ARM_LEFT_Y = HEAD_SIZE;

    public static final int LEG_WIDTH = 4;
    public static final int LEG_HEIGHT = 12;

    public static final int LEG_RIGHT_U = 4;
    public static final int LEG_RIGHT_V = 20;
    public static final int LEG_RIGHT_LAYER_U = 4;
    public static final int LEG_RIGHT_LAYER_V = 36;
    public static final int LEG_RIGHT_X = 4;
    public static final int LEG_RIGHT_Y = HEAD_SIZE + TORSO_HEIGHT;

    public static final int LEG_LEFT_U = 20;
    public static final int LEG_LEFT_V = 52;
    public static final int LEG_LEFT_LAYER_U = 4;
    public static final int LEG_LEFT_LAYER_V = 52;
    public static final int LEG_LEFT_X = 4 + LEG_WIDTH;
    public static final int LEG_LEFT_Y = HEAD_SIZE + TORSO_HEIGHT;

    public static final int WIDTH = TORSO_WIDTH + ARM_WIDE_WIDTH * 2;
    public static final int HEIGHT = HEAD_SIZE + TORSO_HEIGHT + LEG_HEIGHT;

    public static final int LAYER_DOWNSCALE = 2;

    public static void draw(GuiGraphics graphics, Skin skin, int x, int y, int scale) {
        draw(graphics, skin.texture(), skin.model(), x, y, scale);
    }

    public static void draw(GuiGraphics graphics, Identifier texture, Skin.Model model, int x, int y, int scale) {
        int armWidth = model.equals(Skin.Model.WIDE) ? ARM_WIDE_WIDTH : ARM_SLIM_WIDTH;
        int rightArmOffset = model.equals(Skin.Model.WIDE) ? 0 : 1;

        drawLeftArm(graphics, texture, x, y, scale, armWidth, false);
        drawRightArm(graphics, texture, x, y, scale, armWidth, rightArmOffset, false);
        drawLeftLeg(graphics, texture, x, y, scale, false);
        drawRightLeg(graphics, texture, x, y, scale, false);
        drawTorso(graphics, texture, x, y, scale, false);

        RenderSystem.enableBlend();
        drawLeftArm(graphics, texture, x, y, scale, armWidth, true);
        drawRightArm(graphics, texture, x, y, scale, armWidth, rightArmOffset, true);
        drawLeftLeg(graphics, texture, x, y, scale, true);
        drawRightLeg(graphics, texture, x, y, scale, true);
        drawTorso(graphics, texture, x, y, scale, true);

        drawHead(graphics, texture, x, y, scale, false);
        drawHead(graphics, texture, x, y, scale, true);
        RenderSystem.disableBlend();
    }

    public static void drawFace(GuiGraphics graphics, Skin skin, int x, int y, int scale) {
        drawFace(graphics, skin.texture(), x, y, scale);
    }

    public static void drawFace(GuiGraphics graphics, Identifier texture, int x, int y, int scale) {
        int inflate = Math.max(scale / LAYER_DOWNSCALE, 1);
        drawComponent(graphics, texture, x, y, scale, -inflate, false,
                0, 0, HEAD_SIZE, HEAD_SIZE, HEAD_U, HEAD_V, HEAD_LAYER_U, HEAD_LAYER_V);
        RenderSystem.enableBlend();
        drawComponent(graphics, texture, x, y, scale, 0, true,
                0, 0, HEAD_SIZE, HEAD_SIZE, HEAD_U, HEAD_V, HEAD_LAYER_U, HEAD_LAYER_V);
        RenderSystem.disableBlend();
    }

    private static void drawComponent(GuiGraphics graphics, Identifier texture,
            int x, int y, int scale, int o, boolean isLayer,
            int compX, int compY, int compW, int compH,
            int u, int v, int uLayer, int vLayer) {
        graphics.drawTexture(texture,
                x + compX * scale - o, y + compY * scale - o,
                compW * scale + o * 2, compH * scale + o * 2,
                isLayer ? uLayer : u,
                isLayer ? vLayer : v,
                compW, compH, 64, 64);
    }

    private static void drawComponent(GuiGraphics graphics, Identifier texture,
            int x, int y, int scale, boolean isLayer,
            int compX, int compY, int compW, int compH,
            int u, int v, int uLayer, int vLayer) {
        int inflate = isLayer ? Math.max(scale / LAYER_DOWNSCALE, 1) : 0;
        drawComponent(graphics, texture, x, y, scale, inflate, isLayer,
                compX, compY, compW, compH, u, v, uLayer, vLayer);
    }

    private static void drawHead(GuiGraphics graphics, Identifier texture, int x, int y, int scale, boolean isLayer) {
        drawComponent(graphics, texture, x, y, scale, isLayer,
                HEAD_X, HEAD_Y,
                HEAD_SIZE, HEAD_SIZE,
                HEAD_U, HEAD_V,
                HEAD_LAYER_U, HEAD_LAYER_V);
    }

    private static void drawTorso(GuiGraphics graphics, Identifier texture, int x, int y, int scale, boolean isLayer) {
        drawComponent(graphics, texture, x, y, scale, isLayer,
                TORSO_X, TORSO_Y,
                TORSO_WIDTH, TORSO_HEIGHT,
                TORSO_U, TORSO_V,
                TORSO_LAYER_U, TORSO_LAYER_V);
    }

    private static void drawLeftArm(GuiGraphics graphics, Identifier texture, int x, int y, int scale, int armWidth,
            boolean isLayer) {
        drawComponent(graphics, texture, x, y, scale, isLayer,
                ARM_LEFT_X, ARM_LEFT_Y,
                armWidth, ARM_HEIGHT,
                ARM_LEFT_U, ARM_LEFT_V,
                ARM_LEFT_LAYER_U, ARM_LEFT_LAYER_V);
    }

    private static void drawRightArm(GuiGraphics graphics, Identifier texture, int x, int y, int scale, int armWidth,
            int rightArmOffset, boolean isLayer) {
        drawComponent(graphics, texture, x, y, scale, isLayer,
                ARM_RIGHT_X + rightArmOffset, ARM_RIGHT_Y,
                armWidth, ARM_HEIGHT,
                ARM_RIGHT_U, ARM_RIGHT_V,
                ARM_RIGHT_LAYER_U, ARM_RIGHT_LAYER_V);
    }

    private static void drawLeftLeg(GuiGraphics graphics, Identifier texture, int x, int y, int scale,
            boolean isLayer) {
        drawComponent(graphics, texture, x, y, scale, isLayer,
                LEG_LEFT_X, LEG_LEFT_Y,
                LEG_WIDTH, LEG_HEIGHT,
                LEG_LEFT_U, LEG_LEFT_V,
                LEG_LEFT_LAYER_U, LEG_LEFT_LAYER_V);
    }

    private static void drawRightLeg(GuiGraphics graphics, Identifier texture, int x, int y, int scale,
            boolean isLayer) {
        drawComponent(graphics, texture, x, y, scale, isLayer,
                LEG_RIGHT_X, LEG_RIGHT_Y,
                LEG_WIDTH, LEG_HEIGHT,
                LEG_RIGHT_U, LEG_RIGHT_V,
                LEG_RIGHT_LAYER_U, LEG_RIGHT_LAYER_V);
    }
}
