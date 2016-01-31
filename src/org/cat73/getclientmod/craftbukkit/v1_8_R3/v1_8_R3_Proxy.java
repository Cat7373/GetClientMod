package org.cat73.getclientmod.craftbukkit.v1_8_R3;

import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.cat73.getclientmod.craftbukkit.proxy.CraftBukkitProxy;
import org.cat73.getclientmod.network.NetworkManagerHook;
import org.cat73.getclientmod.reference.Reference;
import org.cat73.getclientmod.status.PlayerStatus;
import org.cat73.getclientmod.util.Log;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.v1_8_R3.LoginListener;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.NetworkManager;
import net.minecraft.server.v1_8_R3.ServerConnection;

public class v1_8_R3_Proxy implements CraftBukkitProxy {
    private Field h;
    private Field i;

    public v1_8_R3_Proxy() {
        try {
            this.h = ServerConnection.class.getDeclaredField("h");
            this.i = LoginListener.class.getDeclaredField("i");
            
            this.h.setAccessible(true);
            this.i.setAccessible(true);
        } catch (Exception e) {
            Log.error("%s failed to initialize.", Reference.NAME);
            e.printStackTrace();
        }
    }

    @Override
    public void hookNetworkManager(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        PlayerStatus playerStatus = PlayerStatus.getPlayer(playerName);
        
        NetworkManager networkManager = getLoginNetworkManager(event);
        if(networkManager != null) {
            playerStatus.hookNetworkManager = true;
            networkManager.channel.pipeline().addBefore("packet_handler", "fml:packet_handler", new NetworkManagerHook(networkManager,  playerStatus, playerName));
        } else {
            playerStatus.done = true;
        }
    }
    
    /**
     * 在 PlayerLoginEvent 中获取该玩家的 NetworkManager
     * @param event
     * @return 如果找到则返回该玩家的 NetworkManager, 否则返回 null
     */
    private NetworkManager getLoginNetworkManager(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        ServerConnection serverConnection = MinecraftServer.getServer().getServerConnection();
        try {
            List<NetworkManager> networkManagers = (List<NetworkManager>) this.h.get(serverConnection);
            for(NetworkManager networkManager : networkManagers) {
                if(networkManager.getPacketListener() instanceof LoginListener) {
                    LoginListener loginListener = (LoginListener) networkManager.getPacketListener();
                    GameProfile gameProfile = (GameProfile) i.get(loginListener);
                    if(gameProfile.getName().equals(player.getName())) {
                        return networkManager;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
