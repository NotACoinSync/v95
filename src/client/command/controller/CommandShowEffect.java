package client.command.controller;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import tools.packets.Field;

public class CommandShowEffect extends Command {

    public CommandShowEffect() {
        super("ShowEffect", "", "!ShowEffect <effect>", null);
        setGMLevel(PlayerGMRank.CONTROLLER);
    }

    @Override
    public boolean execute(MapleClient c, String commandLabel, String[] args) {
        if (args.length != 0) {
            c.announce(Field.FieldEffect.showEffect(args[0]));
        }
        return false;
    }
}
