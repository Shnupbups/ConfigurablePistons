package ca.fxco.configurablepistons.blocks.slipperyBlocks;

import ca.fxco.configurablepistons.base.ModProperties;
import ca.fxco.configurablepistons.base.ModTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

import java.util.Random;

public class BaseSlipperyBlock extends Block {

    public static final int SLIPPERY_DELAY = 6;
    public static final int MAX_DISTANCE = 12;

    public static final IntProperty SLIPPERY_DISTANCE = ModProperties.SLIPPERY_DISTANCE;

    /**
     * A block so slippery that it just keeps falling apart unless it's connected to other blocks
     */

    public BaseSlipperyBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(SLIPPERY_DISTANCE, 0));
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(SLIPPERY_DISTANCE, calculateDistance(ctx.getWorld(), ctx.getBlockPos()));
    }

    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!world.isClient) world.createAndScheduleBlockTick(pos, this, SLIPPERY_DELAY);
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
                                                WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (!world.isClient()) world.createAndScheduleBlockTick(pos, this, SLIPPERY_DELAY);
        return state;
    }

    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int i = calculateDistance(world, pos);
        BlockState blockState = state.with(SLIPPERY_DISTANCE, i);
        if (blockState.get(SLIPPERY_DISTANCE) == MAX_DISTANCE) {
            FallingBlockEntity.spawnFromBlock(world, pos, blockState);
        } else if (state != blockState) {
            world.setBlockState(pos, blockState, Block.NOTIFY_ALL);
        }
    }

    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return calculateDistance(world, pos) < MAX_DISTANCE;
    }

    public static int calculateDistance(BlockView world, BlockPos pos) {
        return calculateDistance(world, pos, 0);
    }
    public static int calculateDistance(BlockView world, BlockPos pos, int iteration) {
        BlockPos.Mutable mutable = pos.mutableCopy().move(Direction.DOWN);
        BlockState blockState = world.getBlockState(mutable);
        int currentDistance = MAX_DISTANCE;
        if (blockState.isIn(ModTags.SLIPPERY_BLOCKS)) {
            currentDistance = blockState.get(SLIPPERY_DISTANCE);
        } else if (blockState.isIn(ModTags.SLIPPERY_TRANSPARENT_BLOCKS)) {
            return iteration < MAX_DISTANCE ? calculateDistance(world, mutable, ++iteration) : MAX_DISTANCE;
        } else if (!blockState.isIn(ModTags.SLIPPERY_IGNORE_BLOCKS) &&
                blockState.isSideSolidFullSquare(world, mutable, Direction.UP)) {
            return 0;
        }
        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockState blockState2 = world.getBlockState(mutable.set(pos, direction));
            if (blockState2.isIn(ModTags.SLIPPERY_BLOCKS)) {
                currentDistance = Math.min(currentDistance, blockState2.get(SLIPPERY_DISTANCE) + 1);
                if (currentDistance == 1) break;
            }
        }
        return currentDistance;
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(SLIPPERY_DISTANCE);
    }
}
