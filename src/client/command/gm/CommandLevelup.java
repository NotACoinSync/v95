package client.command.gm;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;

/**
 *
 *
 * @since Oct 18, 2017
 */
public class CommandLevelup extends Command {

    public CommandLevelup() {
        super("Levelup", "", "!Levelup", null);
        setGMLevel(PlayerGMRank.GM);
    }

    @Override
    public boolean execute(MapleClient c, String commandLabel, String[] args) {
        c.getPlayer().levelUp(true);
        return false;
    }
}
