package com.mrcrayfish.device.api.app.interfaces;

import net.minecraft.ChatFormatting;

/**
 * @author MrCrayfish
 */
public interface IHighlight {
    ChatFormatting[] getKeywordFormatting(String text);
}
