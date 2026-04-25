package io.github.motwnb.packetsniffer.mixin;

import io.github.motwnb.packetsniffer.PacketLogger;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {

    @Inject(
            method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V",
            at = @At("HEAD")
    )
    private void onSend(Packet<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
        PacketLogger.log(packet, "C2S");
    }

    @Inject(
            method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
            at = @At("HEAD")
    )
    private void onReceive(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        PacketLogger.log(packet, "S2C");
    }
}
