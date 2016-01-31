package org.cat73.getclientmod.craftbukkit.proxy;

import org.bukkit.event.player.PlayerLoginEvent;

public interface CraftBukkitProxy {
    void hookNetworkManager(PlayerLoginEvent event);
}
