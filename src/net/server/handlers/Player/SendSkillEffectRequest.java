package net.server.handlers.Player;

import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanManager;
import constants.skills.resistance.Mechanic;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.EffectPacket;
import tools.packets.WvsContext;

public class SendSkillEffectRequest extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        AutobanManager abm = player.getAutobanManager();
        int timestamp = slea.readInt();// TODO: Readd
        abm.setTimestamp(0, timestamp, 3);
        int skillID = slea.readInt();
        int skillLevel = slea.readByte();
        boolean SendLocal = slea.readByte() != 0;
        if (c.getPlayer().getMount().isActive()) {
            c.getPlayer().message("Vui lòng tắt thú cưỡi để thực hiện kỹ năng này!");
            c.announce(WvsContext.enableActions());
            return;
        }
        switch (skillID) {
            case Mechanic.EnhancedFlameLauncher:
                c.announce(EffectPacket.Local.SkillUse.ShowEffectFlameThrowerEnhanced(player.getLevel(), skillLevel));
                if (!SendLocal) {
                    c.getPlayer().getMap().announce(c.getPlayer(), EffectPacket.Remote.SkillUse.ShowEffectFlameThrowerEnhanced(c.getPlayer().getId(), player.getLevel(), skillLevel), false);
                }
                break;
            case Mechanic.FlameLauncher:
                c.announce(EffectPacket.Local.SkillUse.ShowEffectFlameThrower(player.getLevel(), skillLevel));
                if (!SendLocal) {
                    c.getPlayer().getMap().announce(c.getPlayer(), EffectPacket.Remote.SkillUse.ShowEffectFlameThrower(c.getPlayer().getId(), player.getLevel(), skillLevel), false);
                }
                break;
            case 35120005:
                c.announce(EffectPacket.Local.SkillUse.ShowEffectSiege_MissileTank(player.getLevel(), skillLevel));
                if (!SendLocal) {
                    c.getPlayer().getMap().announce(c.getPlayer(), EffectPacket.Remote.SkillUse.ShowEffectSiege_MissileTank(c.getPlayer().getId(), player.getLevel(), skillLevel), false);
                }
                break;
            case 35100004:
                c.announce(EffectPacket.Local.SkillUse.ShowEffectRocketBooster(player.getLevel(), skillLevel));
                if (!SendLocal) {
                    c.getPlayer().getMap().announce(c.getPlayer(), EffectPacket.Remote.SkillUse.ShowEffectRocketBooster(c.getPlayer().getId(), player.getLevel(), skillLevel), false);
                }
                break;
            case 35110004:
                c.announce(EffectPacket.Local.SkillUse.ShowEffectSiege(player.getLevel(), skillLevel));
                if (!SendLocal) {
                    c.getPlayer().getMap().announce(c.getPlayer(), EffectPacket.Remote.SkillUse.ShowEffectSiege(c.getPlayer().getId(), player.getLevel(), skillLevel), false);
                }
                break;
            case 35120013:
                c.announce(EffectPacket.Local.SkillUse.ShowEffectSiegeStance(player.getLevel(), skillLevel));
                if (!SendLocal) {
                    c.getPlayer().getMap().announce(c.getPlayer(), EffectPacket.Remote.SkillUse.ShowEffectSiege3(c.getPlayer().getId(), player.getLevel(), skillLevel), false);
                }
                break;
            default:
                c.getPlayer().message("Kỹ năng này chưa được hoàn thiện. Vui lòng báo cho GM về kỹ năng số: " + skillID);
                c.announce(WvsContext.enableActions());
                break;
        }
    }

}
