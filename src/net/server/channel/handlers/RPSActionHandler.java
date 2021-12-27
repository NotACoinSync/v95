package net.server.channel.handlers;

import client.MapleClient;
import client.RockPaperScissors;
import net.AbstractMaplePacketHandler;
import tools.packets.PacketHelper;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.RPSGameDlg;

public final class RPSActionHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (slea.available() == 0 || !c.getPlayer().getMap().containsNPC(9000019)) {
            if (c.getPlayer().getRPS() != null) {
                c.getPlayer().getRPS().dispose(c);
            }
            return;
        }
        final byte mode = slea.readByte();
        switch (mode) {
            case 0: // Start
            case 5: // Retry
                if (c.getPlayer().getRPS() != null) {
                    c.getPlayer().getRPS().reward(c);
                }
                if (c.getPlayer().getMeso() >= 1000) {
                    c.getPlayer().setRPS(new RockPaperScissors(c, mode));
                } else {
                    c.announce(RPSGameDlg.encode(RPSGameDlg.NotEnoughMeso));
                }
                break;
            case 1: // Answer
                if (c.getPlayer().getRPS() == null || !c.getPlayer().getRPS().answer(c, slea.readByte())) {
                    c.announce(RPSGameDlg.encode(RPSGameDlg.Destroy));
                }
                break;
            case 2: // Time Over
                if (c.getPlayer().getRPS() == null || !c.getPlayer().getRPS().timeOut(c)) {
                    c.announce(RPSGameDlg.encode(RPSGameDlg.Destroy));
                }
                break;
            case 3: // Continue
                if (c.getPlayer().getRPS() == null || !c.getPlayer().getRPS().nextRound(c)) {
                    c.announce(RPSGameDlg.encode(RPSGameDlg.Destroy));
                }
                break;
            case 4: // Exit
                if (c.getPlayer().getRPS() != null) {
                    c.getPlayer().getRPS().dispose(c);
                } else {
                    c.announce(RPSGameDlg.encode(RPSGameDlg.Destroy));
                }
                break;
        }
    }
}
