package ca.fxco.configurablepistons.renderers;

import ca.fxco.configurablepistons.blocks.pistons.basePiston.BasicPistonBlock;
import ca.fxco.configurablepistons.blocks.pistons.basePiston.BasicPistonBlockEntity;
import ca.fxco.configurablepistons.blocks.pistons.basePiston.BasicPistonHeadBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.PistonType;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

@Environment(EnvType.CLIENT)
public class BasicPistonBlockEntityRenderer<T extends BasicPistonBlockEntity> implements BlockEntityRenderer<T> {
    private final BlockRenderManager manager;

    public BasicPistonBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.manager = ctx.getRenderManager();
    }

    @Override
    public void render(T pistonBE, float f, MatrixStack matrix, VertexConsumerProvider vertexConsumers, int i, int j) {
        World world = pistonBE.getWorld();
        if (world == null) return;
        BlockPos blockPos = pistonBE.getPos().offset(pistonBE.getMovementDirection().getOpposite());
        BlockState blockState = pistonBE.getPushedBlock();
        if (blockState.isAir()) return;
        BlockModelRenderer.enableBrightnessCache();
        matrix.push();
        matrix.translate(pistonBE.getRenderOffsetX(f), pistonBE.getRenderOffsetY(f), pistonBE.getRenderOffsetZ(f));
        if (blockState.getBlock() instanceof BasicPistonHeadBlock && pistonBE.getProgress(f) <= 4.0f) {
            blockState = blockState.with(BasicPistonHeadBlock.SHORT, pistonBE.getProgress(f) <= 0.5f);
            this.renderModel(blockPos, blockState, matrix, vertexConsumers, world, false, j);
        } else if (pistonBE.isSource() && !pistonBE.isExtending()) {
            if (blockState.getBlock() instanceof BasicPistonBlock bpb) {
                PistonType pistonType = bpb.sticky ? PistonType.STICKY : PistonType.DEFAULT;
                BlockState blockState2 = bpb.getHeadBlock().getDefaultState()
                        .with(BasicPistonHeadBlock.TYPE, pistonType)
                        .with(BasicPistonHeadBlock.FACING, blockState.get(BasicPistonBlock.FACING));
                blockState2 = blockState2.with(BasicPistonHeadBlock.SHORT, pistonBE.getProgress(f) >= 0.5f);
                this.renderModel(blockPos, blockState2, matrix, vertexConsumers, world, false, j);
                BlockPos blockPos2 = blockPos.offset(pistonBE.getMovementDirection());
                matrix.pop();
                matrix.push();
                blockState = blockState.with(BasicPistonBlock.EXTENDED, true);
                this.renderModel(blockPos2, blockState, matrix, vertexConsumers, world, true, j);
            }
        } else {
            this.renderModel(blockPos, blockState, matrix, vertexConsumers, world, false, j);
        }
        matrix.pop();
        BlockModelRenderer.disableBrightnessCache();
    }

    protected void renderModel(BlockPos pos, BlockState state, MatrixStack matrix,
                             VertexConsumerProvider vertexConsumers, World world, boolean cull, int overlay) {
        RenderLayer renderLayer = RenderLayers.getMovingBlockLayer(state);
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);
        this.manager.getModelRenderer().render(world, this.manager.getModel(state), state, pos, matrix, vertexConsumer,
                cull, new Random(), state.getRenderingSeed(pos), overlay);
    }

    @Override
    public int getRenderDistance() {
        return 68;
    }
}
