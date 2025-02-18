package com.ultreon.devices.block;

import com.ultreon.devices.DeviceType;
import com.ultreon.devices.block.entity.DeviceBlockEntity;
import com.ultreon.devices.block.entity.OfficeChairBlockEntity;
import com.ultreon.devices.entity.SeatEntity;
import com.ultreon.devices.init.DeviceBlockEntities;
import com.ultreon.devices.init.DeviceEntities;
import com.ultreon.devices.util.SeatUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class OfficeChairBlock extends DeviceBlock.Colored
{
    public static final EnumProperty<Type> TYPE = EnumProperty.create("type", Type.class);

    private static final VoxelShape EMPTY_BOX = Shapes.box(0, 0, 0, 0, 0, 0);
    private static final VoxelShape SELECTION_BOX = Shapes.box(0.0625f, 0, 0.0625f, 0.9375f, /*1.6875f*/0.625f, 0.9375f);
    private static final VoxelShape SEAT_BOUNDING_BOX = Shapes.box(0.0625f, 0, 0.0625f, 0.9375f, 0.625f, 0.9375f);

    public OfficeChairBlock(DyeColor color)
    {
        super(BlockBehaviour.Properties.of(Material.STONE, color.getMaterialColor()), color, DeviceType.SEAT);
        //this.setUnlocalizedName("office_chair");
        //this.setRegistryName("office_chair");
        //this.setCreativeTab(MrCrayfishDeviceMod.TAB_DEVICE);
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(TYPE, Type.LEGS));
    }

    @Override
    public String getDescriptionId() {
        return Util.makeDescriptionId("block", new ResourceLocation("devices", "office_chair"));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader reader, BlockPos pos)
    {
        return false;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return SELECTION_BOX;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SEAT_BOUNDING_BOX;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if(Minecraft.getInstance().player != null && Minecraft.getInstance().player.getVehicle() instanceof SeatEntity)
        {
            return EMPTY_BOX;
        }
        return SELECTION_BOX;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        //System.out.println(DeviceEntities.SEAT.get().create(level).toString());
        System.out.println("OKOKJRTKFD");
        if(!level.isClientSide)
        {
            SeatUtil.createSeatAndSit(level, pos, player, -1);
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new OfficeChairBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> pBuilder)
    {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(TYPE);
    }

    public enum Type implements StringRepresentable
    {
        LEGS, SEAT, FULL;

        @Override
        public String getSerializedName()
        {
            return name().toLowerCase();
        }
    }
}