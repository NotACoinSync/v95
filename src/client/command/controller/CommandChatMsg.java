package client.command.controller;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import tools.StringUtil;
import tools.packets.CField.userpool.UserLocal;

public class CommandChatMsg extends Command {

    public CommandChatMsg() {
        super("ChatMsg", "", "", null);
        setGMLevel(PlayerGMRank.CONTROLLER);
    }

    @Override
    public boolean execute(MapleClient c, String commandLabel, String[] args) {
        if (args.length > 1) {
            c.announce(UserLocal.ChatMsg(Integer.parseInt(args[0]), StringUtil.joinStringFrom(args, 1)));
        }
        return false;
    }
}
