package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleCharacter.FameStatus;
import client.MapleClient;
import client.MapleStat;
import client.autoban.AutobanFactory;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public final class GiveFameHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter target = (MapleCharacter) c.getPlayer().getMap().getMapObject(slea.readInt());
        int mode = slea.readByte();
        int famechange = 2 * mode - 1;
        MapleCharacter player = c.getPlayer();
        if (target == null || target.getId() == player.getId() || player.getLevel() < 15) {
            return;
        } else if (famechange != 1 && famechange != -1) {
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit fame. famechange" + famechange);
            c.disconnect(false, false);
            return;
        }
        FameStatus status = player.canGiveFame(target);
        if (status == FameStatus.OK || player.isGM()) {
            if (Math.abs(target.getFame() + famechange) < 30001) {
                target.addFame(famechange);
                target.updateSingleStat(MapleStat.FAME, target.getFame());
            }
            if (!player.isGM()) {
                player.hasGivenFame(target);
            }
            c.announce(WvsContext.GivePopularityResult.Send(mode, target.getName(), target.getFame()));
            target.getClient().announce(WvsContext.GivePopularityResult.Receive(mode, player.getName()));
        } else {
            c.announce(WvsContext.GivePopularityResult.encode((byte) (status == FameStatus.NOT_TODAY ? 3 : 4)));
        }
    }
}
