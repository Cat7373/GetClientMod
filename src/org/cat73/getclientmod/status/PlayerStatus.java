package org.cat73.getclientmod.status;

import java.util.HashMap;

public class PlayerStatus {
    private final static HashMap<String, PlayerStatus> playerStatus = new HashMap<String, PlayerStatus>();

    /** 是否为 Forge 客户端 */
    public boolean useFML;
    /** 检查握手包里的 hostname 是否被修改过(比如 Forge 会附加 \\0FML\\0) */
    public boolean hostnameCheck;
    /** 是否已经 hook 过 NetworkManager(获取 NetworkManager 失败会一直为false) */
    public boolean hookNetworkManager = false;
    
    // hookNetworkManager 为 true 以下几个值才有意义
    /** 是否收到过 REGISTER 包(Forge 客户端的标记之一) */
    public boolean register;
    /** 是否已经发送过获取 Mod 列表的请求包(如果不是 Forge 客户端则可能一直是 false) */
    public boolean sendGetModsPacket = false;
    /** 该客户端的 Mod 列表 */
    public HashMap<String, String> mods = new HashMap<String, String>();
    
    /** 是否已经完成所有检查 */
    public boolean done = false;
    
    public static PlayerStatus addPlayer(String playerName) {
        PlayerStatus playerStatus = new PlayerStatus();
        PlayerStatus.playerStatus.put(playerName, playerStatus);
        return playerStatus;
    }
    
    public static PlayerStatus removePlayer(String playerName) {
        return playerStatus.remove(playerName);
    }
    
    public static PlayerStatus getPlayer(String playerName) {
        return playerStatus.get(playerName);
    }
}
