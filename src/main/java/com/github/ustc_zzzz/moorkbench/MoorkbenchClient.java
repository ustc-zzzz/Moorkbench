package com.github.ustc_zzzz.moorkbench;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.MinecraftForge;

public final class MoorkbenchClient {
    public static void initialize() {
        MinecraftForge.EVENT_BUS.addListener(MoorkbenchClient::onRenderPost);
    }

    public static void onRenderPost(RenderLivingEvent.Post<?, ?> event) {
        Entity entity = event.getEntity();
        if (entity.getType() == EntityType.MOOSHROOM && !event.getRenderer().getEntityModel().isSitting) {
            MooshroomEntity mooshroom = (MooshroomEntity) entity;
            if (mooshroom.getAttributeValue(Moorkbench.moorkbench) + Moorkbench.OCCURRENCE >= 1.0) {
                BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
                MatrixStack matrixStack = event.getMatrixStack();
                matrixStack.push();
                matrixStack.rotate(Vector3f.YP.rotationDegrees(180.0F - MathHelper.interpolateAngle(
                        event.getPartialRenderTick(), mooshroom.prevRenderYawOffset, mooshroom.renderYawOffset)));
                matrixStack.scale(0.8F, 0.8F, 0.8F);
                matrixStack.translate(-0.5, 1.5, -0.5);
                dispatcher.renderBlock(Blocks.CRAFTING_TABLE.getDefaultState(), matrixStack,
                        event.getBuffers(), event.getLight(), OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
                matrixStack.pop();
            }
        }
    }
}
