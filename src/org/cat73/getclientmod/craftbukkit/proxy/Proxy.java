package org.cat73.getclientmod.craftbukkit.proxy;

import org.bukkit.event.player.PlayerLoginEvent;

public interface Proxy {
    void init();
    void hookNetworkManager(PlayerLoginEvent event);
}
