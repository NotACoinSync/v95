package net.server.handlers.Player;

import client.MapleCharacter;
import client.MapleClient;
import client.player.SecondaryStat;
import net.server.channel.handlers.AbstractDealDamageHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.userpool.UserRemote;

public final class TryDoingBodyAttack extends AbstractDealDamageHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        AttackInfo attack = null;
        if (player.getEnergyBar() == 15000 || player.getBuffedValue(SecondaryStat.BodyPressure) != null) {            
            applyAttack(parseDamage(iPacket, player, false, false, true), player, 1);            
        } else {
            return;
        }
        if (player.getBuffEffect(SecondaryStat.Morph) != null) {
            if (player.getBuffEffect(SecondaryStat.Morph).isMorphWithoutAttack()) {
                // How are they attacking when the client won't let them?
                player.getClient().disconnect(false, false);
                return;
            }
        }
        byte[] packet = UserRemote.bodyAttack(player, attack);
        player.getMap().announce(player, packet, false, true);
    }
}
