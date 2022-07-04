package com.mrcrayfish.device.init;

import com.mrcrayfish.device.Reference;
import com.mrcrayfish.device.block.entity.LaptopBlockEntity;
import com.mrcrayfish.device.block.entity.PaperBlockEntity;
import com.mrcrayfish.device.block.entity.PrinterBlockEntity;
import com.mrcrayfish.device.block.entity.RouterBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("ConstantConditions")
public class DeviceBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, Reference.MOD_ID);

    public static final RegistryObject<BlockEntityType<PaperBlockEntity>> PAPER = REGISTER.register("paper", () -> BlockEntityType.Builder.of(PaperBlockEntity::new, DeviceBlocks.PAPER.get()).build(null));
    public static final RegistryObject<BlockEntityType<LaptopBlockEntity>> LAPTOP = REGISTER.register("laptop", () -> BlockEntityType.Builder.of(LaptopBlockEntity::new, DeviceBlocks.getLaptops().toArray(new Block[]{})).build(null));
    public static final RegistryObject<BlockEntityType<PrinterBlockEntity>> PRINTER = REGISTER.register("printer", () -> BlockEntityType.Builder.of(PrinterBlockEntity::new, DeviceBlocks.getPrinters().toArray(new Block[]{})).build(null));
    public static final RegistryObject<BlockEntityType<RouterBlockEntity>> ROUTER = REGISTER.register("router", () -> BlockEntityType.Builder.of(RouterBlockEntity::new, DeviceBlocks.getRouters().toArray(new Block[]{})).build(null));

    public static void register(IEventBus bus) {
        REGISTER.register(bus);
    }
}
