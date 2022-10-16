package com.ultreon.devices.api.app;

import net.minecraft.resources.ResourceLocation;

/**
 * Created by Timor Morrien
 */
public enum Food implements IIcon {
    TOMATO,
    APPLE_RED,
    APPLE_GREEN,
    ORANGE,
    LEMON,
    BANANA,
    MELON,
    CHERRIES,

    DONUT,
    PIZZA,
    ICE_CREAM,
    STRAWBERRY,
    GRAPES;

    private static final ResourceLocation ICON_ASSET = new ResourceLocation("devices:textures/gui/icon_packs/food.png");

    private static final int ICON_SIZE = 10;
    private static final int GRID_SIZE = 20;

    @Override
    public ResourceLocation getIconAsset() {
        return ICON_ASSET;
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

    @Override
    public int getSourceWidth() {
        return ICON_SIZE * GRID_SIZE;
    }

    @Override
    public int getSourceHeight() {
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

}
