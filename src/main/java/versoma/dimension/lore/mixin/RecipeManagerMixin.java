package versoma.dimension.lore.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.core.registries.Registries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

    @Shadow
    private RecipeMap recipes;

    private static final ResourceKey<Recipe<?>> ENDER_EYE_RECIPE = ResourceKey.create(
            Registries.RECIPE,
            Identifier.withDefaultNamespace("ender_eye")
    );

    @Inject(
            method = "apply(Lnet/minecraft/world/item/crafting/RecipeMap;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("TAIL")
    )
    private void removeEnderEyeRecipe(RecipeMap recipes, ResourceManager manager, ProfilerFiller profiler, CallbackInfo ci) {
        List<RecipeHolder<?>> filtered = this.recipes.values().stream()
                .filter(r -> !r.id().equals(ENDER_EYE_RECIPE))
                .toList();

        this.recipes = RecipeMap.create(filtered);
    }
}