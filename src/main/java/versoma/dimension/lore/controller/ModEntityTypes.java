package versoma.dimension.lore.controller;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;

public class ModEntityTypes {
    public static final EntityType<ControllerCreakingEntity> CONTROLLER_CREAKING = register(
            "controller_creaking",
            EntityType.Builder.<ControllerCreakingEntity>of(ControllerCreakingEntity::new, MobCategory.MONSTER)
                    .sized(0.9f, 1.8f)
                    .noSummon() // пока без яйца призыва в обычном виде
    );

    private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(
                Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath("versoma-dimension-lore", name)
        );
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(
                CONTROLLER_CREAKING,
                ControllerCreakingEntity.createAttributes()
        );

        ResourceKey<Item> eggKey = ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath("versoma-dimension-lore", "controller_creaking_spawn_egg")
        );

        Registry.register(
                BuiltInRegistries.ITEM,
                eggKey,
                new SpawnEggItem(
                        new Item.Properties()
                                .setId(eggKey)
                                .component(
                                        DataComponents.ENTITY_DATA,
                                        TypedEntityData.of(CONTROLLER_CREAKING, new CompoundTag())
                                )
                )
        );
    }
}