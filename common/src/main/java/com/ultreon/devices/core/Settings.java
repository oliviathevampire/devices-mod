package com.ultreon.devices.core;

import com.ultreon.devices.programs.system.object.ColorScheme;
import net.minecraft.nbt.CompoundTag;

/**
 * @author MrCrayfish
 */
public class Settings {
    private static boolean showAllApps = true;

    private ColorScheme colorScheme = new ColorScheme();

    private boolean useColorAsWallpaper = false;

    private String taskBarPlacement;

    public static void setShowAllApps(boolean showAllApps) {
        Settings.showAllApps = showAllApps;
    }

    public static boolean isShowAllApps() {
        return Settings.showAllApps;
    }

    public ColorScheme getColorScheme() {
        return colorScheme;
    }

    public boolean shouldUseColorAsWallpaper() {
        return useColorAsWallpaper;
    }

    public void useColorAsWallpaper() {
        this.useColorAsWallpaper = true;
    }

    public String getTaskBarPlacement() {
        return taskBarPlacement;
    }

    public void setTaskBarPlacement(String taskBarPlacement) {
        this.taskBarPlacement = taskBarPlacement;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("showAllApps", showAllApps);
        tag.put("colorScheme", colorScheme.toTag());
        tag.putBoolean("useColorAsWallpaper", useColorAsWallpaper);
        tag.putString("taskBarPlacement", taskBarPlacement);
        return tag;
    }

    public static Settings fromTag(CompoundTag tag) {
        //showAllApps = tag.getBoolean("showAllApps");

        Settings settings = new Settings();
        settings.colorScheme = ColorScheme.fromTag(tag.getCompound("colorScheme"));
        settings.useColorAsWallpaper = tag.getBoolean("useColorAsWallpaper");
        settings.taskBarPlacement = tag.getString("taskBarPlacement");
        return settings;
    }
}
