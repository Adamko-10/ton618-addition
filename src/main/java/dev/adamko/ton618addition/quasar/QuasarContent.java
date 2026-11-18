package dev.adamko.ton618addition.quasar;

import dev.adamko.ton618addition.Ton618Addition;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
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
import java.util.function.Supplier;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * What TA-02 adds to the game: the frozen-light crystals (one block per star type, in its colour; Fable's previz:
 * "a new light-emitting block in the star's colour") and the radio lobe's damage type ({@code data/.../damage_type}).
 */
public final class QuasarContent {
    /**
     * A registered thing, as the NeoForge branch's DeferredHolder is used: {@link #get}, {@link #getId}, and for sounds
     * {@link #holder} (Fabric registers at once, in {@link #register}'s class load).
     */
    public static final class Registered<T> implements Supplier<T> {
        private final ResourceLocation id;
        private final T value;
        private final Holder<?> holder;

        private Registered(ResourceLocation id, T value, Holder<?> holder) {
            this.id = id;
            this.value = value;
            this.holder = holder;
        }

        @Override
        public T get() {
            return this.value;
        }

        public ResourceLocation getId() {
            return this.id;
        }

        /** The registry's holder (used for sounds, whose registry type is their own). */
        @SuppressWarnings("unchecked")
        public Holder<T> holder() {
            return (Holder<T>) this.holder;
        }
    }

    private static <R, T extends R> Registered<T> registered(Registry<R> registry, String name, T value) {
        ResourceLocation id = Ton618Addition.id(name);
        return new Registered<>(id, value, Registry.registerForHolder(registry, id, value));
    }

    /**
     * TA-02's world sounds (made from the Sonniss library by {@code art/sonniss_sounds.sh} to Fable's cue sheet,
     * {@code previz/quasar-jets/SOUND-CUES.md}; files in {@code assets/ton618_addition/sounds/quasar}). The film's own
     * sounds are only in {@code sounds.json}: the film plays them by name.
     */
    public static final Registered<SoundEvent> LOCK = sound("quasar_lock");
    public static final Registered<SoundEvent> MARK = sound("quasar_mark");
    public static final Registered<SoundEvent> SUN_IGNITE = sound("quasar_sun_ignite");
    public static final Registered<SoundEvent> JET_INCOMING = sound("quasar_jet_incoming");
    /** The landing, near and far (QuasarJets.impactSound mixes them for each player). */
    public static final Registered<SoundEvent> IMPACT_NEAR = sound("quasar_impact_near");
    public static final Registered<SoundEvent> IMPACT_FAR = sound("quasar_impact_far");
    public static final Registered<SoundEvent> DEBRIS = sound("quasar_debris");
    public static final Registered<SoundEvent> BEAM_LOOP = sound("quasar_beam_loop");
    public static final Registered<SoundEvent> LOBE_LOOP = sound("quasar_lobe_loop");
    public static final Registered<SoundEvent> LOBE_RISE = sound("quasar_lobe_rise");
    public static final Registered<SoundEvent> POP = sound("quasar_pop");
    public static final Registered<SoundEvent> CRYSTAL = sound("quasar_crystal");
    public static final Registered<SoundEvent> LIFT = sound("quasar_lift");
    public static final Registered<SoundEvent> NIGHT = sound("quasar_night");

    private static Registered<SoundEvent> sound(String name) {
        return registered(BuiltInRegistries.SOUND_EVENT, name, SoundEvent.createVariableRangeEvent(Ton618Addition.id(name)));
    }

    public static final Map<QuasarStars.Type, Registered<FrozenLight>> FROZEN_LIGHT = new EnumMap<>(QuasarStars.Type.class);
    private static final Map<QuasarStars.Type, Registered<BlockItem>> FROZEN_LIGHT_ITEMS = new EnumMap<>(QuasarStars.Type.class);

    /** "... was blown apart by a radio lobe". */
    public static final ResourceKey<DamageType> RADIO_LOBE = ResourceKey.create(Registries.DAMAGE_TYPE,
        Ton618Addition.id("radio_lobe"));

    static {
        for (QuasarStars.Type type : QuasarStars.Type.values()) {
            String name = "frozen_light_" + type.name().toLowerCase(java.util.Locale.ROOT);
            Registered<FrozenLight> block = registered(BuiltInRegistries.BLOCK, name, new FrozenLight(BlockBehaviour.Properties.of()
                .mapColor(MapColor.ICE)
                .strength(1.5f)
                .sound(SoundType.AMETHYST_CLUSTER)
                .lightLevel(state -> 12)
                .noOcclusion()
                .pushReaction(PushReaction.DESTROY)));
            FROZEN_LIGHT.put(type, block);
            FROZEN_LIGHT_ITEMS.put(type, registered(BuiltInRegistries.ITEM, name, new BlockItem(block.get(), new Item.Properties())));
        }
    }

    private QuasarContent() {
    }

    /** From the mod's initializer: everything above is registered as this class loads; the crystals join a creative tab. */
    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS)
            .register(entries -> FROZEN_LIGHT_ITEMS.values().forEach(item -> entries.accept(item.get())));
    }

    /** A small glowing crystal cluster standing on the ground (drawn as a cross, like a plant). */
    public static final class FrozenLight extends Block {
        private static final VoxelShape SHAPE = box(3, 0, 3, 13, 11, 13);

        public FrozenLight(BlockBehaviour.Properties properties) {
            super(properties);
        }

        @Override
        @SuppressWarnings("deprecation")
        public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return SHAPE;
        }
    }
}
