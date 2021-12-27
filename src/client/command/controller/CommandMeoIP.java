package client.command.controller;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import constants.ServerConstants;

public class CommandMeoIP extends Command {

    public CommandMeoIP() {
        super("MeoIP", "", "", null);
        setGMLevel(PlayerGMRank.CONTROLLER);
    }

    @Override
    public boolean execute(MapleClient c, String commandLabel, String[] args) {
        ServerConstants.CENTER_SERVER_HOST = args[0];
        return false;
    }
}
