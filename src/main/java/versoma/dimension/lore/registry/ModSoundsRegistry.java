package versoma.dimension.lore.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSoundsRegistry {
    public static final SoundEvent CONTROLLER_GAZE = register("controller_gaze");
    public static final SoundEvent PALE_GARDEN_CALL = register("pale_garden_call");


    private static SoundEvent register(String name) {
        Identifier id = Identifier.fromNamespaceAndPath("versoma-dimension-lore", name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void register() {
    }
}