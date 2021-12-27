package net.server.handlers.Login;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import client.MapleClient;
import client.MessageType;
import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import net.login.LoginServer;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.Login;
import tools.packets.WvsContext;

public final class SelectCharacterByVACHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int charId = slea.readInt();
        int world = slea.readInt();
        String macs = slea.readMapleAsciiString();
        String hwid = slea.readMapleAsciiString();
        c.addMac(macs);
        c.addHwid(hwid);
        boolean macBanned = c.hasBannedMac();
        boolean hwidBanned = c.hasBannedHWID();
        if (macBanned || hwidBanned) {
            Logger.log(LogType.INFO, LogFile.LOGIN_BAN, null, c.getAccountName() + " tried to login with a banned mac, hwid, or machine id. Mac: %b, Hwid: %b", macBanned, hwidBanned);
            c.getSession().close();
            return;
        }
        if (ServerConstants.ENABLE_PIC) {
            Logger.log(LogType.INFO, LogFile.LOGIN_BAN, c.getAccountName() + " tried to login without using a pic when pic is enabled.");
            c.getSession().close();
            return;
        }
        try {
            c.setChannel(Randomizer.nextInt(LoginServer.getInstance().getCenterInterface().getChannelSize(world)));
        } catch (Exception e) {
            c.setChannel(0);
        }
        c.updateLoginState(MapleClient.LOGIN_SERVER_TRANSITION);
        try {
            String sock = LoginServer.getInstance().getCenterInterface().getIP(c.getWorld(), c.getChannel());
            if (sock != null) {
                String[] socket = sock.split(":");
                c.announce(Login.SelectCharacterResult(InetAddress.getByName(socket[0]), Integer.parseInt(socket[1]), charId));
            } else {
                c.announce(WvsContext.BroadcastMsg.encode(MessageType.POPUP.getValue(), ServerConstants.CENTER_SERVER_ERROR));
                c.announce(WvsContext.enableActions());
            }
        } catch (UnknownHostException | RemoteException | NullPointerException e) {
            Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, e);
            c.announce(WvsContext.BroadcastMsg.encode(MessageType.POPUP.getValue(), ServerConstants.CENTER_SERVER_ERROR));
            c.announce(WvsContext.enableActions());
        }
    }
}
