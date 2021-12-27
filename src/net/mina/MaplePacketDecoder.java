package net.mina;

import java.util.List;

import client.MapleClient;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;
import tools.MapleAESOFB;

public class MaplePacketDecoder extends ByteToMessageDecoder {

    private static final AttributeKey<DecoderState> DECODER_STATE_KEY = AttributeKey.valueOf(MaplePacketDecoder.class.getName() + ".STATE");

    private static class DecoderState {

        public int packetlength = -1;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!ctx.channel().hasAttr(MapleClient.CLIENT_KEY)) {
            return;
        }
        final MapleClient client = (MapleClient) ctx.channel().attr(MapleClient.CLIENT_KEY).get();
        if (client == null) {
            return;
        }
        DecoderState decoderState = (DecoderState) ctx.channel().attr(DECODER_STATE_KEY).get();
        if (decoderState == null) {
            decoderState = new DecoderState();
            ctx.channel().attr(DECODER_STATE_KEY).set(decoderState);
        }
        if (in.readableBytes() >= 4 && decoderState.packetlength == -1) {
            int packetHeader = in.readInt();
            if (!client.getReceiveCrypto().checkPacket(packetHeader)) {
                ctx.channel().close();
                return;
            }
            decoderState.packetlength = MapleAESOFB.getPacketLength(packetHeader);
        } else if (in.readableBytes() < 4 && decoderState.packetlength == -1) {
            return;
        }
        if (in.readableBytes() >= decoderState.packetlength) {
            byte decryptedPacket[] = new byte[decoderState.packetlength];
            in.readBytes(decryptedPacket, 0, decoderState.packetlength);
            decoderState.packetlength = -1;
            client.getReceiveCrypto().crypt(decryptedPacket);
            MapleCustomEncryption.decryptData(decryptedPacket);
            out.add(decryptedPacket);
        }
        return;
    }
}
