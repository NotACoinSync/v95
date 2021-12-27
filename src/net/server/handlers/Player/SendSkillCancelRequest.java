package net.server.handlers.Player;

import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import constants.skills.*;
import constants.skills.resistance.*;
import net.AbstractMaplePacketHandler;
import net.MaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.userpool.UserRemote;

public final class SendSkillCancelRequest extends AbstractMaplePacketHandler implements MaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        int SkillID = slea.readInt();
        switch (SkillID) {
            case FPArchMage.BIG_BANG:
            case ILArchMage.BIG_BANG:
            case Bishop.BIG_BANG:
            case Bowmaster.HURRICANE:
            case Marksman.PIERCING_ARROW:
            case Corsair.RAPID_FIRE:
            case WindArcher.HURRICANE:
            case Evan.FIRE_BREATH:
            case Evan.ICE_BREATH:
            case Mechanic.SatelliteSafety:
            case Mechanic.PerfectArmor:
                c.getPlayer().getMap().announce(c.getPlayer(), UserRemote.skillCancel(c.getPlayer().getId(), SkillID), false);
                break;
            default:
                c.getPlayer().cancelEffect(SkillFactory.getSkill(SkillID).getEffect(1), false, -1);
                break;
        }
    }
}
