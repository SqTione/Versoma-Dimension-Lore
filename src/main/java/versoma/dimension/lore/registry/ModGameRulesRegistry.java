package versoma.dimension.lore.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;

public class ModGameRulesRegistry {
    public static final GameRule<Boolean> PALE_GARDEN_CALL_ACTIVE = GameRuleBuilder
            .forBoolean(false)
            .category(GameRuleCategory.SPAWNING)
            .buildAndRegister(Identifier.fromNamespaceAndPath("versoma-dimension-lore", "pale_garden_call_active"));

    public static void register() {
    }
}