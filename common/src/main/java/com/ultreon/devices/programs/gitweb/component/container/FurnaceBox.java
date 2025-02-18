package com.ultreon.devices.programs.gitweb.component.container;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ultreon.devices.core.Laptop;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;

/**
 * @author MrCrayfish
 */
public class FurnaceBox extends ContainerBox {
    public static final int HEIGHT = 68;

    private int progressTimer;
    private int fuelTimer;
    private final int fuelTime;

    public FurnaceBox(ItemStack input, ItemStack fuel, ItemStack result) {
        super(0, 0, 0, 68, HEIGHT, new ItemStack(Blocks.FURNACE), "Furnace");
        slots.add(new Slot(26, 8, input));
        slots.add(new Slot(26, 44, fuel));
        slots.add(new Slot(85, 26, result));
        this.fuelTime = getBurnTime(fuel, RecipeType.SMELTING);
    }

    @ExpectPlatform
    private static int getBurnTime(ItemStack stack, RecipeType<?> type) {
        throw new AssertionError();
    }

    @Override
    protected void handleTick() {
        if (++progressTimer == 200) {
            progressTimer = 0;
        }
        if (--fuelTimer <= 0) {
            fuelTimer = fuelTime;
        }
    }

    @Override
    protected void render(PoseStack pose, Laptop laptop, Minecraft mc, int x, int y, int mouseX, int mouseY, boolean windowActive, float partialTicks) {
        super.render(pose, laptop, mc, x, y, mouseX, mouseY, windowActive, partialTicks);

        RenderSystem.setShaderTexture(0, CONTAINER_BOXES_TEXTURE);

        int burnProgress = this.getBurnLeftScaled(13);
        this.blit(pose, x + 26, y + 52 - burnProgress, 128, 238 - burnProgress, 14, burnProgress + 1);

        int cookProgress = this.getCookProgressScaled(24);
        this.blit(pose, x + 49, y + 37, 128, 239, cookProgress + 1, 16);
    }

    private int getCookProgressScaled(int pixels) {
        return this.progressTimer * pixels / 200;
    }

    private int getBurnLeftScaled(int pixels) {
        int i = this.fuelTime;
        if (i == 0) {
            i = 200;
        }
        return this.fuelTimer * pixels / i + 1;
    }
}
