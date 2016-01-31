package org.cat73.getclientmod.listener;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.cat73.getclientmod.craftbukkit.proxy.CraftBukkitProxy;
import org.cat73.getclientmod.status.PlayerStatus;
import org.cat73.getclientmod.util.Log;

public class PlayerListener implements Listener {
    private final CraftBukkitProxy proxy;
    
    public PlayerListener(CraftBukkitProxy proxy) {
        this.proxy = proxy;
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerLogin (PlayerLoginEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        PlayerStatus playerStatus = PlayerStatus.addPlayer(playerName);
        
        String hostname = event.getHostname();
        boolean useFML = hostname.contains("\0FML\0");
        Log.debug(hostname);
        boolean hostnameCheck = !hostname.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{1,5}$");
        
        playerStatus.useFML = useFML;
        playerStatus.hostnameCheck = hostnameCheck;
        
        proxy.hookNetworkManager(event);
    }

    public static void onDone (String playerName) {
        PlayerStatus playerStatus = PlayerStatus.getPlayer(playerName);

        playerStatus.done = true;

        Log.debug("Player %s status:\n"
                + "useFML: %s\n"
                + "hostnameCheck: %s\n"
                + "hookNetworkManager: %s\n"
                + "register: %s\n"
                + "sendGetModsPacket: %s\n"
                + "clientName: %s\n"
                + "Mods:\n%s",
            playerName,
            playerStatus.useFML,
            playerStatus.hostnameCheck,
            playerStatus.hookNetworkManager,
            playerStatus.register,
            playerStatus.sendGetModsPacket,
            playerStatus.clientName,
            formatMods(playerStatus.mods)
        );
    }
    
    private static String formatMods(HashMap<String, String> mods) {
        String result = "";
        for(String name : mods.keySet()) {
            result += name + ": " + mods.get(name) + "\n";
        }
        return result;
    }
}
