package client.command.controller;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import tools.StringUtil;
import tools.packets.CField.userpool.UserLocal;

public class CommandNoticeMsg extends Command {

    public CommandNoticeMsg() {
        super("NoticeMsg", "", "", null);
        setGMLevel(PlayerGMRank.CONTROLLER);
    }

    @Override
    public boolean execute(MapleClient c, String commandLabel, String[] args) {
        if (args.length > 1) {
            c.announce(UserLocal.NoticeMsg(StringUtil.joinStringFrom(args, 0)));
        }
        return false;
    }
}
