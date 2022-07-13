package com.ultreon.devices.core.client.debug;

import com.ultreon.devices.block.entity.LaptopBlockEntity;
import com.ultreon.devices.core.Laptop;
import com.ultreon.devices.init.DeviceBlocks;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.hooks.client.screen.ScreenAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Adds a button to the title screen to test system applications that don't require the system
 */
public class ClientAppDebug {
    public static void register() {
        ClientGuiEvent.INIT_POST.register(((screen, access) -> {
            if (!(screen instanceof TitleScreen)) return;
            var rowHeight = 24;
            var y = screen.height / 4 + 48;

            var a = new Button(screen.width / 2 - 100, y + rowHeight * -1, 200, 20, new TextComponent("DV TEST"), (button) -> {
                Minecraft.getInstance().setScreen(new Laptop(new LaptopBlockEntity(new BlockPos(0, 0, 0), DeviceBlocks.WHITE_LAPTOP.get().defaultBlockState())));
            }, Button.NO_TOOLTIP);
            access.addRenderableWidget(a);
        }));
    }
}
