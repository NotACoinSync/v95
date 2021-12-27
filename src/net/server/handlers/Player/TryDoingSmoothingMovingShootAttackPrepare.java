package net.server.handlers.Player;

import client.MapleCharacter;
import client.MapleClient;
import client.player.SecondaryStat;
import net.server.channel.handlers.AbstractDealDamageHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public class TryDoingSmoothingMovingShootAttackPrepare extends AbstractDealDamageHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        AttackInfo ret = new AttackInfo();
        ret.skillID = iPacket.readInt();
        ret.display = iPacket.readShort();
        ret.AttackSpeed = iPacket.readByte();

        if (player.getBuffEffect(SecondaryStat.Morph) != null) {
            if (player.getBuffEffect(SecondaryStat.Morph).isMorphWithoutAttack()) {
                // How are they attacking when the client won't let them?
                player.getClient().disconnect(false, false);
                return;
            }
        }
    }
}
