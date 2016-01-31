package org.cat73.getclientmod.craftbukkit.proxy;

import org.cat73.getclientmod.craftbukkit.v1_8_R3.v1_8_R3_Proxy;

public class ProxyManager {
    public static Proxy proxy;
    public static void init() {
        ProxyManager.proxy = getProxy();
        if(ProxyManager.proxy != null) {
            ProxyManager.proxy.init();
        }
    }

    private static Proxy getProxy() {
        try {
            org.cat73.getclientmod.craftbukkit.v1_8_R3.ImportTest.test();
            return new v1_8_R3_Proxy();
        } catch(Exception e) {
        }
        
        return null;
    }
}
