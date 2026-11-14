package dev.adamko.ton618addition;

import dev.adamko.ton618addition.link.Ton618Link;
import net.fabricmc.api.DedicatedServerModInitializer;

/** On a dedicated server: join TON 618 once every mod's main entrypoint has run (see {@link Ton618Addition}). */
public final class Ton618AdditionServer implements DedicatedServerModInitializer {
    @Override
    public void onInitializeServer() {
        Ton618Link.install();
    }
}
