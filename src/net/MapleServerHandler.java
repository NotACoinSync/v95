package net;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import client.MapleClient;
import constants.ServerConstants;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.login.LoginServer;
import server.shark.SharkPacket;
import tools.MapleAESOFB;
import tools.MapleLogger;
import tools.data.input.ByteArrayByteStream;
import tools.data.input.GenericSeekableLittleEndianAccessor;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.ClientSocket;

public class MapleServerHandler extends ChannelInboundHandlerAdapter {

    private PacketProcessor processor;
    private int world = -1, channel = -1;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    public MapleServerHandler() {
        this.processor = PacketProcessor.getProcessor(-1, -1);
    }

    public MapleServerHandler(int world, int channel) {
        this.processor = PacketProcessor.getProcessor(world, channel);
        this.world = world;
        this.channel = channel;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        MapleClient mc = (MapleClient) ctx.channel().attr(MapleClient.CLIENT_KEY).get();
        if ((cause instanceof IOException || cause instanceof ClassCastException) && mc != null) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, cause, "Exception caught by account: " + mc.getAccountName());
            return;
        }
        if (mc != null && mc.getPlayer() != null) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, cause, "Exception caught by: " + mc.getPlayer());
        } else if (mc != null) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, cause, "Exception caught by: " + mc.getAccountName());
        }
        super.exceptionCaught(ctx, cause);// ?
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (channel > -1 && world > -1) {// TODO:?
            /*if(!Server.getInstance().isOnline()){
				session.close(true);
				return;
			}
			if(Server.getInstance().getChannel(world, channel) == null){
				session.close(true);
				return;
			}*/
        } else {
            LoginServer.getInstance().addClientChannel(ctx.channel());
            Logger.log(LogType.INFO, LogFile.SESSIONS, "IoSession with " + ctx.channel().remoteAddress() + " opened on " + sdf.format(Calendar.getInstance().getTime()));
        }
        byte key[] = {0x13, 0x00, 0x00, 0x00, // 19
            0x08, 0x00, 0x00, 0x00, // 8
            0x06, 0x00, 0x00, 0x00, (byte) // 6
            0xB4, 0x00, 0x00, 0x00, // 180
            0x1B, 0x00, 0x00, 0x00, // 27
            0x0F, 0x00, 0x00, 0x00, // 15
            0x33, 0x00, 0x00, 0x00, // 51
            0x52, 0x00, 0x00, 0x00};// 82
        byte ivRecv[] = {70, 114, 122, 82};
        byte ivSend[] = {82, 48, 120, 115};
        ivRecv[3] = (byte) (Math.random() * 255);
        ivSend[3] = (byte) (Math.random() * 255);
        MapleAESOFB sendCypher = new MapleAESOFB(key, ivSend, (short) (0xFFFF - ServerConstants.VERSION));
        MapleAESOFB recvCypher = new MapleAESOFB(key, ivRecv, (short) ServerConstants.VERSION);
        MapleClient client = new MapleClient(sendCypher, recvCypher, ctx.channel());
        client.setWorld(world);
        client.setChannel(channel);
        ctx.channel().writeAndFlush(ClientSocket.getHello(ivSend, ivRecv));
        ctx.channel().attr(MapleClient.CLIENT_KEY).set(client);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (channel > -1 && world > -1) {
            // TODO:?
        } else {
            LoginServer.getInstance().removeClientChannel(ctx.channel());
        }
        MapleClient client = ctx.channel().attr(MapleClient.CLIENT_KEY).get();
        if (client != null) {
            try {
                boolean inCashShop = false;
                if (!client.isPlayerNull()) {
                    inCashShop = client.getPlayer().getCashShop().isOpened();
                }
                client.disconnect(false, inCashShop);
            } catch (Throwable t) {
                Logger.log(LogType.ERROR, LogFile.ACCOUNT_STUCK, t);
            } finally {
                ctx.close();
                ctx.channel().attr(MapleClient.CLIENT_KEY).set(null);
                // client.empty();
            }
        }
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        byte[] content = (byte[]) message;
        SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream(content));
        short packetId = slea.readShort();
        MapleClient client = (MapleClient) ctx.channel().attr(MapleClient.CLIENT_KEY).get();
        if (ServerConstants.LOG_SHARK) {
            final SharkPacket sp = new SharkPacket((byte[]) message, true);
            client.sl.log(sp);
        }
        if (ServerConstants.UPDATE_VERSION && !MapleLogger.isRecvBlocked(MapleLogger.getRecvOpcodeFromValue(packetId))) {
            System.out.println("[Recv] (" + packetId + ") " + RecvOpcode.getOpcodeByOp(packetId));
        }
        final MaplePacketHandler packetHandler = processor.getHandler(packetId);
        MapleLogger.logRecv(packetId, message);
        MapleLogger.logRecv(client, packetId, message);
        if (packetHandler != null && packetHandler.validateState(client)) {
            packetHandler.handlePacket(slea, client);
        } else if (packetHandler == null) {
            Logger.log(LogType.INFO, LogFile.MISSING_HANDLER, null, "Failed Handler - Client: %s, Player: %s, packetid: %d, toString: %s", client.getAccountName(), (client.getPlayerNullable() != null ? client.getPlayerNullable().getName() : ""), packetId, slea.toString());
        }
        content = null;
        slea = null;
        super.channelRead(ctx, message);
    }
        
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        MapleClient client = (MapleClient) ctx.channel().attr(MapleClient.CLIENT_KEY).get();
        if (client != null) {
            client.sendPing();
        }
        super.userEventTriggered(ctx, evt);
    }
}
