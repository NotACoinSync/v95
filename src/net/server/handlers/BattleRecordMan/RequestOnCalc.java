package net.server.handlers.BattleRecordMan;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.BattleRecordMan;
import tools.packets.WvsContext;

public class RequestOnCalc extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        boolean On = slea.readBoolean();
        if (On) {
            c.announce(BattleRecordMan.serverOnCalcRequestResult(On));
        } else {
            c.announce(WvsContext.enableActions());
        }
    }
}
