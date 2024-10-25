package github.xevira.groves.client.renderer;

import github.xevira.groves.block.entity.MoonwellMultiblockMasterBlockEntity;
import github.xevira.groves.block.entity.MoonwellMultiblockSlaveBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class MoonwellFluidLevelBER implements BlockEntityRenderer<MoonwellMultiblockSlaveBlockEntity> {
    private final BlockEntityRendererFactory.Context context;

    public MoonwellFluidLevelBER(BlockEntityRendererFactory.Context context) {
        this.context = context;
    }

    @Override
    public void render(MoonwellMultiblockSlaveBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        SingleFluidStorage fluidTank = entity.getFluidStorage();
        if (fluidTank == null) return;
        if (fluidTank.isResourceBlank() || fluidTank.amount <= 0)
            return;

        matrices.push();
        FluidVariant fluidVariant = fluidTank.getResource();
        long amount = fluidTank.getAmount();
        long capacity = fluidTank.getCapacity();
        float fillPercentage = MathHelper.clamp((float) amount / capacity, 0, 1);

        //int color = FluidVariantRendering.getColor(fluidVariant, entity.getWorld(), entity.getPos());
        Sprite sprite = FluidVariantRendering.getSprites(fluidVariant)[0];
        RenderLayer renderLayer = RenderLayer.getEntityTranslucent(sprite.getAtlasId());
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(renderLayer);

        float minU = sprite.getFrameU(0.0f);
        float maxU = sprite.getFrameU(1.0f);
        float minV = sprite.getFrameV(0.0f);
        float maxV = sprite.getFrameV(1.0f);

        MatrixStack.Entry entry = matrices.peek();

        float y = MathHelper.map(fillPercentage, 0.0f, 1.0f, 0.01f, 0.95f);
        drawQuad(vertexConsumer, entry, 0,0, 1.0f, 1.0f, y, minU, minV, maxU, maxV, 0xFFFFFFFF, light, overlay);

        matrices.pop();
    }

    private static void drawQuad(VertexConsumer vertexConsumer,
                                 MatrixStack.Entry entry,
                                 float x1, float z1,
                                 float x2, float z2,
                                 float y,
                                 float minU, float minV,
                                 float maxU, float maxV,
                                 int color,
                                 int light, int overlay) {
        vertexConsumer.vertex(entry, x2, y, z1)
                .color(color)
                .texture(minU, minV)
                .light(light)
                .overlay(overlay)
                .normal(0.0F, 1.0F, 0.0F);

        vertexConsumer.vertex(entry, x1, y, z1)
                .color(color)
                .texture(minU, maxV)
                .light(light)
                .overlay(overlay)
                .normal(0.0F, 1.0F, 0.0F);

        vertexConsumer.vertex(entry, x1, y, z2)
                .color(color)
                .texture(maxU, maxV)
                .light(light)
                .overlay(overlay)
                .normal(0.0F, 1.0F, 0.0F);

        vertexConsumer.vertex(entry, x2, y, z2)
                .color(color)
                .texture(maxU, minV)
                .light(light)
                .overlay(overlay)
                .normal(0.0F, 1.0F, 0.0F);
    }
}
