package net.server.handlers.Login;

import client.MapleClient;
import constants.ServerConstants;
import java.util.Calendar;
import net.MaplePacketHandler;
import net.server.handlers.AutoRegister;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.Login;
import tools.packets.WvsContext;

public final class LoginPasswordHandler implements MaplePacketHandler {

    @Override
    public boolean validateState(MapleClient c) {
        return !c.isLoggedIn();
    }

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        String pwd = iPacket.readMapleAsciiString();
        String login = iPacket.readMapleAsciiString();
        c.setAccountName(login);
        c.sendServerList(false);
        int loginok = 0;
        if (ServerConstants.AUTO_REGISTER) {
            if (AutoRegister.getAccountExists(login)) {
                loginok = c.login(login, pwd);
            } else {
                if (AutoRegister.createAccount(login, pwd, c.getSession().remoteAddress().toString())) {
                    loginok = c.login(login, pwd);
                } else {
                    c.announce(Login.getLoginFailed(5));
                    return;
                }
            }
        } else {
            loginok = c.login(login, pwd);
        }
        if ((c.hasBannedIP() || c.hasBannedMac())) {
            Logger.log(LogType.INFO, LogFile.LOGIN_BAN, "Someone tried to login to the account " + login + " when the account has a banned ip or mac. New ip: " + c.getSession().remoteAddress().toString());
            c.announce(Login.getLoginFailed(3));
            if (c.getBanReason() != null && c.getBanReason().length() > 0) {
                c.announce(WvsContext.BroadcastMsg.encode(1, c.getBanReason()));
            }
            return;
        }
        Calendar tempban = c.getTempBanCalendar();
        if (tempban != null) {
            if (tempban.getTimeInMillis() > System.currentTimeMillis()) {
                c.announce(Login.getTempBan(tempban.getTimeInMillis(), c.getGReason()));
                if (c.getBanReason() != null && c.getBanReason().length() > 0) {
                    c.announce(WvsContext.BroadcastMsg.encode(1, c.getBanReason()));
                }
                return;
            }
        }
        if (loginok == 3) {
            c.announce(Login.getPermBan(c.getGReason()));// crashes but idc :D
            if (c.getBanReason() != null && c.getBanReason().length() > 0) {
                c.announce(WvsContext.BroadcastMsg.encode(1, c.getBanReason()));
            }
            return;
        } else if (loginok != 0) {
            c.announce(Login.getLoginFailed(loginok));
            return;
        }
        if (c.finishLogin() == 0) {
            login(c);
        } else {
            c.announce(Login.getLoginFailed(7));
        }
    }

    private static void login(MapleClient c) {
        c.announce(Login.CheckPasswordResult(c));// why the fk did I do c.getAccountName()?
    }
}
