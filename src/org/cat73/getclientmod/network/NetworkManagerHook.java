package org.cat73.getclientmod.network;

import java.net.SocketAddress;

import org.cat73.getclientmod.listener.PlayerListener;
import org.cat73.getclientmod.status.PlayerStatus;

import com.google.common.base.Charsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.server.v1_8_R3.NetworkManager;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;
import net.minecraft.server.v1_8_R3.PacketLoginOutSuccess;
import net.minecraft.server.v1_8_R3.PacketPlayInCustomPayload;
import net.minecraft.server.v1_8_R3.PacketPlayInKeepAlive;
import net.minecraft.server.v1_8_R3.PacketPlayOutCustomPayload;

public class NetworkManagerHook extends SimpleChannelInboundHandler<Object> implements ChannelOutboundHandler {
    private final NetworkManager networkManager;
    private final PlayerStatus playerStatus;
    private final String playerName;
    private boolean clientHello = false;
    
    public NetworkManagerHook(NetworkManager networkManager, PlayerStatus playerStatus, String playerName) {
        this.networkManager = networkManager;
        this.playerStatus = playerStatus;
        this.playerName = playerName;
    }

    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.bind(localAddress, promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.close(promise);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        ctx.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.deregister(promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.disconnect(promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        ctx.read();
    }
    
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ctx.write(msg, promise);

        if(msg instanceof PacketLoginOutSuccess) {
            PacketDataSerializer data = new PacketDataSerializer(Unpooled.buffer());
            data.writeBytes(new byte[] { 0, 2, 0, 0, 0, 0});
            this.networkManager.handle(new PacketPlayOutCustomPayload("FML|HS", data));

            this.playerStatus.sendGetModsPacket = true;
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        boolean handled = false;
        
        if(msg instanceof PacketPlayInCustomPayload) {
            handled = handleServerSideCustomPacket((PacketPlayInCustomPayload) msg);
        } else if(msg instanceof PacketPlayInKeepAlive) {
            onDone();
        }

        if (!handled) {
            ctx.fireChannelRead(msg);
        }
    }
    
    private void onDone() {
        this.networkManager.channel.pipeline().remove("fml:packet_handler");
        PlayerListener.onDone(this.playerName);
    }

    private boolean handleServerSideCustomPacket(PacketPlayInCustomPayload msg) {
        String channelName = msg.a();
        
        if ("FML|HS".equals(channelName)) {
            if(!this.clientHello) {
                this.clientHello = true;
            } else {
                PacketDataSerializer data = msg.b();
                data.readByte();
                int modCount = data.readByte();
                for (int i = 0; i < modCount; i++) {
                    this.playerStatus.mods.put(readUTF8String(data), readUTF8String(data));
                }
            }
            return true;
        } else if("REGISTER".equals(channelName)) {
            this.playerStatus.register = true;
            return true;
        } else if("MC|Brand".equals(channelName)) {
            PacketDataSerializer data = msg.b();
            this.playerStatus.clientName = readUTF8String(data);
        }

        return false;
    }
    
    private static String readUTF8String(ByteBuf from) {
        int len = from.readByte();
        String str = from.toString(from.readerIndex(), len, Charsets.UTF_8);
        from.readerIndex(from.readerIndex() + len);
        return str;
    }
}
