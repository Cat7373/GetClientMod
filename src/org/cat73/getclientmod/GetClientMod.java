package org.cat73.getclientmod;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.cat73.getclientmod.craftbukkit.proxy.ProxyManager;
import org.cat73.getclientmod.listener.PlayerListener;
import org.cat73.getclientmod.util.Log;

public class GetClientMod extends JavaPlugin implements Listener {
    public void onEnable() {
        Log.init(this.getLogger());

        ProxyManager.init();
        this.getServer().getPluginManager().registerEvents(new PlayerListener(ProxyManager.proxy), this);
    }
}
