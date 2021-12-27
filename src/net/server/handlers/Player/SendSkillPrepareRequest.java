package net.server.handlers.Player;

import client.MapleClient;
import constants.skills.*;
import constants.skills.resistance.*;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.userpool.UserRemote;
import tools.packets.WvsContext;

public final class SendSkillPrepareRequest extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int skillID = slea.readInt();
        int skillLevel = slea.readByte();
        short flags = slea.readShort();
        int actionSpeed = slea.readByte();
        if (skillID == 33101005) {
            int SwallowMobID = slea.readInt(); // TODO: Jaguar nuốt quái
        }
        switch (skillID) {
            case FPMage.EXPLOSION:

            case FPArchMage.BIG_BANG:
            case ILArchMage.BIG_BANG:
            case Bishop.BIG_BANG:
            case Bowmaster.HURRICANE:
            case Marksman.PIERCING_ARROW:
            case Gunslinger.GRENADE:
            case Corsair.RAPID_FIRE:
            case Brawler.CORKSCREW_BLOW:
            case BladeMaster.MAPLE_WARRIOR:
            case BladeMaster.FINAL_CUT:
            case BladeMaster.MonsterBomb:
            case WindArcher.HURRICANE:
            case NightWalker.POISON_BOMB:
            case ThunderBreaker.CORKSCREW_BLOW:
            case ChiefBandit.CHAKRA:
            case Paladin.MonsterMagnet:
            case DarkKnight.MonsterMagnet:
            case Hero.MonsterMagnet:
            case Evan.FIRE_BREATH:
            case Evan.ICE_BREATH:
            case Mechanic.EnhancedFlameLauncher:
            case Mechanic.FlameLauncher:
            case WildHunter.WildArrowBlast:
            case WildHunter.CalloftheWild:
                c.getPlayer().getMap().announce(c.getPlayer(), UserRemote.skillPrepare(c.getPlayer().getId(), skillID, skillLevel, flags, actionSpeed), false);
                break;
            default:
                c.announce(WvsContext.enableActions());
                break;
        }
    }
}
