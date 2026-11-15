package dev.adamko.ton618addition.quasar;

import com.mojang.serialization.MapCodec;
import dev.adamko.ton618addition.Ton618Addition;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * What TA-02 adds to the game: the frozen-light crystals (one block per star type, in its colour; Fable's previz:
 * "a new light-emitting block in the star's colour") and the radio lobe's damage type ({@code data/.../damage_type}).
 */
public final class QuasarContent {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Ton618Addition.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Ton618Addition.MOD_ID);

    public static final Map<QuasarStars.Type, DeferredBlock<FrozenLight>> FROZEN_LIGHT = new EnumMap<>(QuasarStars.Type.class);
    private static final Map<QuasarStars.Type, DeferredItem<BlockItem>> FROZEN_LIGHT_ITEMS = new EnumMap<>(QuasarStars.Type.class);

    /** "... was blown apart by a radio lobe". */
    public static final ResourceKey<DamageType> RADIO_LOBE = ResourceKey.create(Registries.DAMAGE_TYPE,
        Ton618Addition.id("radio_lobe"));

    static {
        for (QuasarStars.Type type : QuasarStars.Type.values()) {
            String name = "frozen_light_" + type.name().toLowerCase(java.util.Locale.ROOT);
            DeferredBlock<FrozenLight> block = BLOCKS.registerBlock(name, FrozenLight::new, BlockBehaviour.Properties.of()
                .mapColor(MapColor.ICE)
                .strength(1.5f)
                .sound(SoundType.AMETHYST_CLUSTER)
                .lightLevel(state -> 12)
                .noOcclusion()
                .pushReaction(PushReaction.DESTROY));
            FROZEN_LIGHT.put(type, block);
            FROZEN_LIGHT_ITEMS.put(type, ITEMS.registerSimpleBlockItem(block));
        }
    }

    private QuasarContent() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, event -> {
            if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
                FROZEN_LIGHT_ITEMS.values().forEach(event::accept);
            }
        });
    }

    /** A small glowing crystal cluster standing on the ground (drawn as a cross, like a plant). */
    public static final class FrozenLight extends Block {
        private static final MapCodec<FrozenLight> CODEC = simpleCodec(FrozenLight::new);
        private static final VoxelShape SHAPE = box(3, 0, 3, 13, 11, 13);

        public FrozenLight(BlockBehaviour.Properties properties) {
            super(properties);
        }

        @Override
        protected MapCodec<? extends Block> codec() {
            return CODEC;
        }

        @Override
        protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return SHAPE;
        }
    }
}
