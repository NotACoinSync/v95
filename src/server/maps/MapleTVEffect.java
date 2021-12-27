package server.maps;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import client.MapleCharacter;
import net.channel.ChannelServer;
import server.TimerManager;
import tools.packets.PacketHelper;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CField.MapleTVMan;

public class MapleTVEffect {

    private static boolean ACTIVE;
    private List<String> message = new ArrayList<>(5);
    private MapleCharacter user;
    private int type;
    private MapleCharacter partner;

    public MapleTVEffect(MapleCharacter u, MapleCharacter p, List<String> msg, int t) {
        this.message = msg;
        this.user = u;
        this.type = t;
        this.partner = p;
        broadcastTV(true);
    }

    public static boolean isActive() {
        return ACTIVE;
    }

    private void broadcastTV(boolean activity) {
        try {
            ACTIVE = activity;
            if (ACTIVE) {
                ChannelServer.getInstance().getWorldInterface().broadcastPacket(MapleTVMan.SendMessageResult());
                ChannelServer.getInstance().getWorldInterface().broadcastPacket(MapleTVMan.SetMessage(user, message, type <= 2 ? type : type - 3, partner));
                int delay = 15000;
                if (type == 4) {
                    delay = 30000;
                } else if (type == 5) {
                    delay = 60000;
                }
                TimerManager.getInstance().schedule("broadcastTV", new Runnable() {

                    @Override
                    public void run() {
                        broadcastTV(false);
                    }
                }, delay);
            } else {
                ChannelServer.getInstance().getWorldInterface().broadcastPacket(MapleTVMan.ClearMessage());
            }
        } catch (RemoteException | NullPointerException ex) {
            Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
        }
    }
}
