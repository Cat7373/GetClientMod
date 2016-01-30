package org.cat73.getclientmod;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.cat73.getclientmod.listener.PlayerListener;
import org.cat73.getclientmod.util.Log;

public class GetClientMod extends JavaPlugin implements Listener {
    private static GetClientMod instance;

    public void onEnable() {
        GetClientMod.instance = this;
        Log.init(this.getLogger());

        this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }

    public static GetClientMod instance() {
        return GetClientMod.instance;
    }
}
