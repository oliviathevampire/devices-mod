package com.ultreon.devices.api.app;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

/**
 * @author MrCrayfish
 */
public enum Alphabet implements IIcon {
    EXCLAMATION_MARK, QUOTATION_MARK, NUMBER_SIGN, DOLLAR_SIGN, PERCENT_SIGN, AMPERSAND, APOSTROPHE, LEFT_PARENTHESIS, RIGHT_PARENTHESIS, ASTERISK, PLUS_SIGN, COMMA, HYPHEN_MINUS, FULL_STOP, SLASH, ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, COLON, SEMI_COLON, LESS_THAN, EQUALS, MORE_THAN, QUESTION_MARK, COMMERCIAL_AT, UPPERCASE_A, UPPERCASE_B, UPPERCASE_C, UPPERCASE_D, UPPERCASE_E, UPPERCASE_F, UPPERCASE_G, UPPERCASE_H, UPPERCASE_I, UPPERCASE_J, UPPERCASE_K, UPPERCASE_L, UPPERCASE_M, UPPERCASE_N, UPPERCASE_O, UPPERCASE_P, UPPERCASE_Q, UPPERCASE_R, UPPERCASE_S, UPPERCASE_T, UPPERCASE_U, UPPERCASE_V, UPPERCASE_W, UPPERCASE_X, UPPERCASE_Y, UPPERCASE_Z, LEFT_SQUARE_BRACKET, SLASH_REVERSE, RIGHT_SQUARE_BRACKET, CARET, UNDERSCORE, GRAVE_ACCENT, LEFT_CURLY_BRACKET, VERTICAL_LINE, RIGHT_CURLY_BRACKET, TILDE;

    private static final ResourceLocation ALPHABET_ASSET = new ResourceLocation("devices:textures/gui/alphabet.png");

    private static final int ICON_SIZE = 10;
    private static final int GRID_SIZE = 20;

    @Override
    public ResourceLocation getIconAsset() {
        return ALPHABET_ASSET;
    }

    @Override
    public int getIconSize() {
        return ICON_SIZE;
    }

    @Override
    public int getGridWidth() {
        return GRID_SIZE;
    }

    @Override
    public int getGridHeight() {
        return GRID_SIZE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSourceHeight() {
        return ICON_SIZE * GRID_SIZE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSourceWidth() {
        return ICON_SIZE * GRID_SIZE;
    }

    @Override
    public int getU() {
        return (ordinal() % GRID_SIZE) * ICON_SIZE;
    }

    @Override
    public int getV() {
        return (ordinal() / GRID_SIZE) * ICON_SIZE;
    }

    @Override
    public int getOrdinal() {
        return ordinal();
    }

    public void draw(PoseStack pose, Minecraft mc, int x, int y, int color) {
        Color temp = new Color(color);
        float[] hsb = Color.RGBtoHSB(temp.getRed(), temp.getGreen(), temp.getBlue(), null);
        Color iconColor = new Color(Color.HSBtoRGB(hsb[0], hsb[1], 1f));
        RenderSystem.setShaderColor(iconColor.getRed() / 255f, iconColor.getGreen() / 255f, iconColor.getBlue() / 255f, 1f);
        this.draw(pose, mc, x, y);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
}
