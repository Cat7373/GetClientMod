package org.cat73.getclientmod.listener;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.cat73.getclientmod.network.NetworkManagerHook;
import org.cat73.getclientmod.reference.Reference;
import org.cat73.getclientmod.status.PlayerStatus;
import org.cat73.getclientmod.util.Log;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.v1_8_R3.LoginListener;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.NetworkManager;
import net.minecraft.server.v1_8_R3.ServerConnection;

public class PlayerListener implements Listener {
    private Field h;
    private Field i;
    
    public PlayerListener() {
        // 初始化获取 NetworkManager 需要用的反射的变量 如果出错则本插件失效
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
    
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerLogin (PlayerLoginEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        PlayerStatus playerStatus = PlayerStatus.addPlayer(playerName);
        
        String hostname = event.getHostname();
        boolean useFML = hostname.contains("\0FML\0");
        boolean hostnameCheck = !hostname.matches("^\\\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}:\\d{1,5}$");
        
        playerStatus.useFML = useFML;
        playerStatus.hostnameCheck = hostnameCheck;
        
        NetworkManager networkManager = getLoginNetworkManager(event);
        if(networkManager != null) {
            playerStatus.hookNetworkManager = true;
            networkManager.channel.pipeline().addBefore("packet_handler", "fml:packet_handler", new NetworkManagerHook(networkManager,  playerStatus));
        }
    }
    
    // 不能用这个停止 要在NetWorkManagerHook里检测到PlayerLoginOutSuccess包之后的下一个包被发送的时候停止
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerJoin (PlayerJoinEvent event) {
        Player player = event.getPlayer();
        final String playerName = player.getName();
        final PlayerStatus playerStatus = PlayerStatus.getPlayer(playerName);
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                playerStatus.done = true;
                
                Log.debug("Player %s status: {useFML: %s, hostnameCheck: %s, hookNetworkManager: %s, register: %s, sendGetModsPacket: %s}\nMods:\n%s",
                    playerName,
                    playerStatus.useFML,
                    playerStatus.hostnameCheck,
                    playerStatus.hookNetworkManager,
                    playerStatus.register,
                    playerStatus.sendGetModsPacket,
                    formatMods(playerStatus.mods)
                );
            }
        }.start();
    }
    
    private Object formatMods(HashMap<String, String> mods) {
        String result = "";
        for(String name : mods.keySet()) {
            result += name + ": " + mods.get(name) + "\n";
        }
        return result;
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
