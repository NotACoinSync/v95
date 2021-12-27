package client;

import client.inventory.Item;
import server.MapleInventoryManipulator;
import tools.packets.PacketHelper;
import tools.Randomizer;
import tools.packets.CField.RPSGameDlg;

public class RockPaperScissors {

    private int round = 0;
    private boolean ableAnswer = true;
    private boolean win = false;

    public RockPaperScissors(final MapleClient c, final byte mode) {
        c.announce(RPSGameDlg.encode(mode == 0 ? RPSGameDlg.Start : RPSGameDlg.Retry));
        if (mode == 0) {
            c.getPlayer().gainMeso(-1000, true, true, true);
        }
    }

    public final boolean answer(final MapleClient c, final int answer) {
        if (ableAnswer && !win && answer >= 0 && answer <= 2) {
            final int response = Randomizer.nextInt(3);
            if (response == answer) {
                c.announce(RPSGameDlg.Select((byte) response, (byte) round));
                // dont do anything. they can still answer once a draw
            } else if ((answer == 0 && response == 2) || (answer == 1 && response == 0) || (answer == 2 && response == 1)) { // they win
                c.announce(RPSGameDlg.Select((byte) response, (byte) (round + 1)));
                ableAnswer = false;
                win = true;
            } else { // they lose
                c.announce(RPSGameDlg.Select((byte) response, (byte) -1));
                ableAnswer = false;
            }
            return true;
        }
        reward(c);
        return false;
    }

    public final boolean timeOut(final MapleClient c) {
        if (ableAnswer && !win) {
            ableAnswer = false;
            c.announce(RPSGameDlg.encode(RPSGameDlg.ShowResult));
            return true;
        }
        reward(c);
        return false;
    }

    public final boolean nextRound(final MapleClient c) {
        if (win) {
            round++;
            if (round < 10) {
                win = false;
                ableAnswer = true;
                c.announce(RPSGameDlg.encode(RPSGameDlg.Retry));
                return true;
            }
        }
        reward(c);
        return false;
    }

    public final void reward(final MapleClient c) {
        if (win) {
            MapleInventoryManipulator.addFromDrop(c, new Item(4031332 + round, (short) 1), true);
        } else if (round == 0) {
            c.getPlayer().gainMeso(500, true, true, true);
        }
        c.getPlayer().setRPS(null);
    }

    public final void dispose(final MapleClient c) {
        reward(c);
        c.announce(RPSGameDlg.encode(RPSGameDlg.Destroy));
    }
}
