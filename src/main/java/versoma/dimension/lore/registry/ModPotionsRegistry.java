package versoma.dimension.lore.registry;

import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import versoma.dimension.lore.VersomaDimensionLore;

public class ModPotionsRegistry {

    public static final Holder<Potion> ROT_POTION = register("rot", new Potion("rot",
            new MobEffectInstance(ModEffectsRegistry.ROT, 300, 0)));

    public static final Holder<Potion> LONG_ROT_POTION = register("long_rot", new Potion("rot",
            new MobEffectInstance(ModEffectsRegistry.ROT, 800, 0)));

    private static Holder<Potion> register(String name, Potion potion) {
        return Registry.registerForHolder(
                BuiltInRegistries.POTION,
                Identifier.fromNamespaceAndPath(VersomaDimensionLore.MOD_ID, name),
                potion
        );
    }

    public static void register() {
        FabricPotionBrewingBuilder.BUILD.register(builder -> {
            builder.addMix(Potions.AWKWARD, Items.ROTTEN_FLESH, ROT_POTION);

            builder.addMix(ROT_POTION, Items.REDSTONE, LONG_ROT_POTION);
        });
    }
}