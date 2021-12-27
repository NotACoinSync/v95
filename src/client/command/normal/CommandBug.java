package client.command.normal;

import client.MapleCharacter;
import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import net.channel.ChannelServer;
import tools.StringUtil;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.WvsContext;

public class CommandBug extends Command {

    public CommandBug() {
        super("Bug", "", "", null);
        setGMLevel(PlayerGMRank.NORMAL);
    }

    @Override
    public boolean execute(MapleClient c, String commandLabel, String[] args) {
        if (args.length < 2) {
            c.getPlayer().dropMessage(5, "Message too short and not sent. Please do @bug <bug>");
            return false;
        }
        String message = StringUtil.joinStringFrom(args, 0);
        try {
            ChannelServer.getInstance().getWorldInterface().broadcastGMPacket(WvsContext.SetWeekEventMessage("[BUG]:" + MapleCharacter.makeMapleReadable(c.getPlayer().getName()) + ": " + message));
        } catch (Exception ex) {
            Logger.log(LogType.ERROR, LogFile.REMOTE_EXCEPTION, ex);
        }
        Logger.log(LogType.INFO, LogFile.BUG_REPORT, MapleCharacter.makeMapleReadable(c.getPlayer().getName()) + " (Map: " + c.getPlayer().getMapId() + ", Ch: " + c.getPlayer().getClient().getChannel() + "): " + message);
        c.getPlayer().dropMessage(5, "Your bug '" + message + "' was submitted successfully to our developers. Thank you!");
        return false;
    }
}
