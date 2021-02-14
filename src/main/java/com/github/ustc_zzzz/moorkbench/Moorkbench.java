package com.github.ustc_zzzz.moorkbench;

import com.google.common.base.Preconditions;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.IContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;
import java.util.UUID;

@Mod("moorkbench")
public final class Moorkbench {
    public static final double OCCURRENCE = 0.25;
    public static final UUID MODIFIER_ID = UUID.fromString("2b883bb0-ec19-4065-aa95-3501cd2e4664");

    @ObjectHolder("moorkbench:moorkbench")
    public static Attribute moorkbench;

    public Moorkbench() {
        Moorkbench.initialize();
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> MoorkbenchClient::initialize);
    }

    private static void initialize() {
        MinecraftForge.EVENT_BUS.addListener(Moorkbench::onSpawn);
        MinecraftForge.EVENT_BUS.addListener(Moorkbench::onInteract);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Moorkbench::onSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Attribute.class, Moorkbench::onRegister);
    }

    private static void onRegister(RegistryEvent.Register<Attribute> event) {
        Attribute attribute = new RangedAttribute("attribute.name.moorkbench", 0.0, 0.0, 1.0).setShouldWatch(true);
        event.getRegistry().register(attribute.setRegistryName("moorkbench:moorkbench"));
    }

    private static void onSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> GlobalEntityTypeAttributes.put(EntityType.MOOSHROOM,
                CowEntity.func_234188_eI_().createMutableAttribute(moorkbench).create()));
    }

    private static void onInteract(PlayerInteractEvent.EntityInteract event) {
        Entity entity = event.getTarget();
        if (entity.getType() == EntityType.MOOSHROOM) {
            MooshroomEntity mooshroom = (MooshroomEntity) entity;
            if (mooshroom.getAttributeValue(moorkbench) + OCCURRENCE >= 1.0) {
                Item item = event.getItemStack().getItem();
                if (event.getWorld().isRemote) {
                    event.setCanceled(true);
                    event.setCancellationResult(ActionResultType.SUCCESS);
                } else if (item == Items.SHEARS && mooshroom.isShearable()) {
                    mooshroom.entityDropItem(new ItemStack(Blocks.CRAFTING_TABLE));
                } else if (!item.isIn(ItemTags.SMALL_FLOWERS) && item != Items.SHEARS && item != Items.BOWL) {
                    event.setCanceled(true);
                    event.setCancellationResult(ActionResultType.CONSUME);
                    IWorldPosCallable pos = IWorldPosCallable.of(event.getWorld(), event.getPos());
                    ITextComponent containerTitle = new TranslationTextComponent("container.crafting");
                    IContainerProvider provider = (id, inv, player) -> new WorkbenchContainer(id, inv, pos) {
                        @Override
                        public boolean canInteractWith(@Nonnull PlayerEntity player) {
                            Vector3d vector3d = Vector3d.copyCentered(event.getPos());
                            return mooshroom.isAlive() && player.getDistanceSq(vector3d) <= 64.0;
                        }
                    };
                    event.getPlayer().openContainer(new SimpleNamedContainerProvider(provider, containerTitle));
                }
            }
        }
    }

    private static void onSpawn(LivingSpawnEvent event) {
        if (event instanceof LivingSpawnEvent.CheckSpawn || event instanceof LivingSpawnEvent.SpecialSpawn) {
            Entity entity = event.getEntity();
            if (entity.getType() == EntityType.MOOSHROOM) {
                MooshroomEntity mooshroom = (MooshroomEntity) entity;
                double amount = entity.world.getRandom().nextDouble();
                AttributeModifier.Operation operation = AttributeModifier.Operation.ADDITION;
                AttributeModifier modifier = new AttributeModifier(MODIFIER_ID, "Moorkbench", amount, operation);
                Preconditions.checkNotNull(mooshroom.getAttribute(moorkbench)).applyPersistentModifier(modifier);
            }
        }
    }
}
