package client.command.controller;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import tools.packets.EffectPacket;

public class CommandSpecialEffect extends Command {

    public CommandSpecialEffect() {
        super("SpecialEffect", "", "", "SE");
        setGMLevel(PlayerGMRank.CONTROLLER);
    }

    @Override
    public boolean execute(MapleClient c, String commandLabel, String[] args) {
        if (args.length > 0) {
            c.announce(EffectPacket.encode(Integer.parseInt(args[0])));
        }
        return false;
    }
}
