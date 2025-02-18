package com.ultreon.devices.item;

import com.ultreon.devices.DeviceConfig;
import com.ultreon.devices.Devices;
import com.ultreon.devices.block.entity.NetworkDeviceBlockEntity;
import com.ultreon.devices.block.entity.RouterBlockEntity;
import com.ultreon.devices.core.network.Router;
import com.ultreon.devices.util.KeyboardHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author MrCrayfish
 */
public class EthernetCableItem extends Item {
    public EthernetCableItem() {
        super(new Properties().tab(Devices.TAB_DEVICE).stacksTo(1));
    }

    private static double getDistance(BlockPos source, BlockPos target) {
        return Math.sqrt(source.distToCenterSqr(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        InteractionHand hand = context.getHand();

        if (!level.isClientSide && player != null) {
            ItemStack heldItem = player.getItemInHand(hand);
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof RouterBlockEntity routerBE) {
                if (!heldItem.hasTag()) {
                    sendGameInfoMessage(player, "message.devices.invalid_cable");
                    return InteractionResult.SUCCESS;
                }

                Router router = routerBE.getRouter();

                CompoundTag tag = heldItem.getTag();
                assert tag != null;
                BlockPos devicePos = BlockPos.of(tag.getLong("pos"));

                BlockEntity tileEntity1 = level.getBlockEntity(devicePos);
                if (tileEntity1 instanceof NetworkDeviceBlockEntity networkDeviceBlockEntity) {
                    if (!router.isDeviceRegistered(networkDeviceBlockEntity)) {
                        if (router.addDevice(networkDeviceBlockEntity)) {
                            networkDeviceBlockEntity.connect(router);
                            heldItem.shrink(1);
                            if (getDistance(tileEntity1.getBlockPos(), routerBE.getBlockPos()) > DeviceConfig.SIGNAL_RANGE.get()) {
                                sendGameInfoMessage(player, "message.devices.successful_registered");
                            } else {
                                sendGameInfoMessage(player, "message.devices.successful_connection");
                            }
                        } else {
                            sendGameInfoMessage(player, "message.devices.router_max_devices");
                        }
                    } else {
                        sendGameInfoMessage(player, "message.devices.device_already_connected");
                    }
                } else {
                    if (router.addDevice(tag.getUUID("id"), tag.getString("name"))) {
                        heldItem.shrink(1);
                        sendGameInfoMessage(player, "message.devices.successful_registered");
                    } else {
                        sendGameInfoMessage(player, "message.devices.router_max_devices");
                    }
                }
                return InteractionResult.SUCCESS;
            }

            if (blockEntity instanceof NetworkDeviceBlockEntity networkDeviceBlockEntity) {
                heldItem.setTag(new CompoundTag());
                CompoundTag tag = heldItem.getTag();
                assert tag != null;
                tag.putUUID("id", networkDeviceBlockEntity.getId());
                tag.putString("name", networkDeviceBlockEntity.getCustomName());
                tag.putLong("pos", networkDeviceBlockEntity.getBlockPos().asLong());

                sendGameInfoMessage(player, "message.devices.select_router");
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }

    private void sendGameInfoMessage(Player player, String message) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendMessage(new TranslatableComponent(message), ChatType.GAME_INFO, Util.NIL_UUID);
        }
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if (!level.isClientSide) {
            ItemStack heldItem = player.getItemInHand(usedHand);
            if (player.isCrouching()) {
                heldItem.resetHoverName();
                heldItem.setTag(null);
                return new InteractionResultHolder<>(InteractionResult.SUCCESS, heldItem);
            }
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @org.jetbrains.annotations.Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag isAdvanced) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                tooltip.add(new TextComponent(ChatFormatting.RED.toString() + ChatFormatting.BOLD + "ID: " + ChatFormatting.RESET + tag.getUUID("id")));
                tooltip.add(new TextComponent(ChatFormatting.RED.toString() + ChatFormatting.BOLD + "Device: " + ChatFormatting.RESET + tag.getString("name")));

                BlockPos devicePos = BlockPos.of(tag.getLong("pos"));
                String text = ChatFormatting.RED.toString() + ChatFormatting.BOLD + "X: " + ChatFormatting.RESET + devicePos.getX() + " " +
                        ChatFormatting.RED + ChatFormatting.BOLD + "Y: " + ChatFormatting.RESET + devicePos.getY() + " " +
                        ChatFormatting.RED + ChatFormatting.BOLD + "Z: " + ChatFormatting.RESET + devicePos.getZ();
                tooltip.add(new TextComponent(text));
            }
        } else {
            if (!KeyboardHelper.isShiftDown()) {
                tooltip.add(new TextComponent(ChatFormatting.GRAY + "Use this cable to connect"));
                tooltip.add(new TextComponent(ChatFormatting.GRAY + "a device to a router."));
                tooltip.add(new TextComponent(ChatFormatting.YELLOW + "Hold SHIFT for How-To"));
                return;
            }

            tooltip.add(new TextComponent(ChatFormatting.GRAY + "Start by right clicking a"));
            tooltip.add(new TextComponent(ChatFormatting.GRAY + "device with this cable"));
            tooltip.add(new TextComponent(ChatFormatting.GRAY + "then right click the "));
            tooltip.add(new TextComponent(ChatFormatting.GRAY + "router you want to"));
            tooltip.add(new TextComponent(ChatFormatting.GRAY + "connect this device to."));
        }
        super.appendHoverText(stack, level, tooltip, isAdvanced);
    }

    @Environment(EnvType.CLIENT)
    public boolean hasEffect(ItemStack stack) {
        return stack.hasTag();
    }

    @NotNull
    @Override
    public Component getName(ItemStack stack) {
        if (stack.hasTag()) {
            return super.getDescription().copy().withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD);
        }
        return super.getName(stack);
    }
}
