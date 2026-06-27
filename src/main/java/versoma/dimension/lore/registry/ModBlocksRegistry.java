package versoma.dimension.lore.registry;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import versoma.dimension.lore.VersomaDimensionLore;
import versoma.dimension.lore.mold.MoldBlock;

import java.util.function.Function;

public class ModBlocksRegistry {
    public static void initialize() {
        CreativeModeTabEvents
                .modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register((creativeTab) ->
                        creativeTab.accept(ModBlocksRegistry.ANCIENT_TOTEM.asItem()));
    }

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> blockFactory, BlockBehaviour.Properties properties, boolean shouldRegisterItem) {
       ResourceKey<Block> blockKey = keyOfBlock(name);
       Block block = blockFactory.apply(properties.setId(blockKey));

       if(shouldRegisterItem) {
           ResourceKey<Item> blockItemKey = keyOfItem(name);

           BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(blockItemKey).useBlockDescriptionPrefix());
           Registry.register(BuiltInRegistries.ITEM, blockItemKey, blockItem);
       }

       return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
   }

   private static ResourceKey<Block> keyOfBlock(String name) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(VersomaDimensionLore.MOD_ID, name));
   }

    private static ResourceKey<Item> keyOfItem(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(VersomaDimensionLore.MOD_ID, name));
    }

    public static final Block ANCIENT_TOTEM = register(
            "ancient_totem",
            Block::new,
            BlockBehaviour.Properties.of().sound(SoundType.WOOD),
            true
    );

    public static final Block MOLD = register(
            "mold",
            MoldBlock::new,
            BlockBehaviour.Properties.of()
                    .sound(SoundType.SCULK_VEIN)
                    .noCollision()
                    .replaceable(),
            true
    );
}
