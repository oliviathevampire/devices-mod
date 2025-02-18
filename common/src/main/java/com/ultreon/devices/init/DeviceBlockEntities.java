package com.ultreon.devices.init;

import com.google.gson.JsonArray;
import com.ultreon.devices.Devices;
import com.ultreon.devices.block.OfficeChairBlock;
import com.ultreon.devices.block.entity.*;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

@SuppressWarnings("ConstantConditions")
public class DeviceBlockEntities {
    private static final Registrar<BlockEntityType<?>> REGISTER = Devices.REGISTRIES.get().get(Registry.BLOCK_ENTITY_TYPE_REGISTRY);

    public static final RegistrySupplier<BlockEntityType<PaperBlockEntity>> PAPER = REGISTER.register(Devices.id("paper"), () -> BlockEntityType.Builder.of(PaperBlockEntity::new, DeviceBlocks.PAPER.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<LaptopBlockEntity>> LAPTOP = REGISTER.register(Devices.id("laptop"), () -> BlockEntityType.Builder.of(LaptopBlockEntity::new, DeviceBlocks.getAllLaptops().toArray(new Block[]{})).build(null));
    public static final RegistrySupplier<BlockEntityType<PrinterBlockEntity>> PRINTER = REGISTER.register(Devices.id("printer"), () -> BlockEntityType.Builder.of(PrinterBlockEntity::new, DeviceBlocks.getAllPrinters().toArray(new Block[]{})).build(null));
    public static final RegistrySupplier<BlockEntityType<RouterBlockEntity>> ROUTER = REGISTER.register(Devices.id("router"), () -> BlockEntityType.Builder.of(RouterBlockEntity::new, DeviceBlocks.getAllRouters().toArray(new Block[]{})).build(null));
    public static final RegistrySupplier<BlockEntityType<OfficeChairBlockEntity>> SEAT = REGISTER.register(Devices.id("seat"), () -> BlockEntityType.Builder.of(OfficeChairBlockEntity::new, DeviceBlocks.getAllOfficeChairs().toArray(new Block[]{})).build(null));

    public static void register() {
   //    Marker
    }
}
