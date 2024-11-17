package github.xevira.groves.client.renderer;

import github.xevira.groves.Groves;
import github.xevira.groves.entity.passive.DruidEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.VillagerHeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.render.entity.state.VillagerEntityRenderState;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.util.Identifier;

public class DruidEntityRenderer  extends MobEntityRenderer<DruidEntity, VillagerEntityRenderState, VillagerResemblingModel> {
    private static final Identifier TEXTURE = Groves.id("textures/entity/druid.png");
    public static final EntityModelLayer MAIN_LAYER = new EntityModelLayer(Groves.id("druid"), "main");

    public DruidEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new VillagerResemblingModel(context.getPart(MAIN_LAYER)), 0.5f);
        this.addFeature(new HeadFeatureRenderer<>(this, context.getModelLoader(), context.getItemRenderer()));
        this.addFeature(new VillagerHeldItemFeatureRenderer<>(this, context.getItemRenderer()));
    }
    public Identifier getTexture(VillagerEntityRenderState villagerEntityRenderState) {
        return TEXTURE;
    }

    public VillagerEntityRenderState createRenderState() {
        return new VillagerEntityRenderState();
    }

    public void updateRenderState(DruidEntity druidEntity, VillagerEntityRenderState villagerEntityRenderState, float f) {
        super.updateRenderState(druidEntity, villagerEntityRenderState, f);
        villagerEntityRenderState.headRolling = druidEntity.getHeadRollingTimeLeft() > 0;
    }
}
