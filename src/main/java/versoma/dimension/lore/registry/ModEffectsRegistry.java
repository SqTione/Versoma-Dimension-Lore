package versoma.dimension.lore.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import versoma.dimension.lore.VersomaDimensionLore;
import versoma.dimension.lore.effects.RotEffect;

public class ModEffectsRegistry {
    public static final Holder<MobEffect> ROT = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            Identifier.fromNamespaceAndPath(VersomaDimensionLore.MOD_ID, "rot"),
            new RotEffect()
    );

    public static void initialize() {
    }

    public static void register() {
    }
}