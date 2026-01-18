package me.pajic.plugin;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class ServerPausePlugin extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public ServerPausePlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        // pause worlds when server starts
        getEventRegistry().register(BootEvent.class, event -> {
            Universe.get().getWorlds().values().forEach(world -> {
                LOGGER.atInfo().log("Paused world %s", world.getName());
                world.setPaused(true);
            });
            LOGGER.atInfo().log("Server started, paused all worlds.");
        });
        // pause world when last player disconnects
        getEventRegistry().register(PlayerDisconnectEvent.class, event -> {
            Ref<EntityStore> storeRef = event.getPlayerRef().getReference();
            if (storeRef != null && storeRef.isValid()) {
                World world = storeRef.getStore().getExternalData().getWorld();
                if (world.getPlayerCount() == 1) {
                    world.setPaused(true);
                    LOGGER.atInfo().log("No players online, paused world.");
                }
            }
        });
        // unpause world when player connects and world is paused
        getEventRegistry().register(PlayerConnectEvent.class, event -> {
            World world = event.getWorld();
            if (world != null && world.isPaused()) {
                world.setPaused(false);
                LOGGER.atInfo().log("Player joined, resumed world.");
            }
        });
    }
}