package org.cat73.getclientmod;

import org.bukkit.plugin.java.JavaPlugin;
import org.cat73.getclientmod.craftbukkit.proxy.CraftBukkitProxy;
import org.cat73.getclientmod.craftbukkit.proxy.CraftBukkitProxyManager;
import org.cat73.getclientmod.listener.PlayerListener;
import org.cat73.getclientmod.util.Log;

public class GetClientMod extends JavaPlugin {
    public CraftBukkitProxy proxy;

    public void onEnable() {
        Log.init(this.getLogger());

        this.proxy = CraftBukkitProxyManager.getProxy();
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this.proxy), this);
    }
}
