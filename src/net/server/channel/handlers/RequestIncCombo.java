package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import constants.GameConstants;
import constants.skills.Aran;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public class RequestIncCombo extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        final MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        int skillLevel = player.getSkillLevel(SkillFactory.getSkill(Aran.COMBO_ABILITY));
        if (GameConstants.is_aran_job(player.getJob().getId()) && (skillLevel > 0 || player.getJob().getId() == 2000)) {
            final long currentTime = System.currentTimeMillis();
            short combo = player.getCombo();
            if ((currentTime - player.getLastCombo()) > 3000 && combo > 0) {
                combo = 0;
            }
            combo++;
            switch (combo) {
                case 10:
                case 20:
                case 30:
                case 40:
                case 50:
                case 60:
                case 70:
                case 80:
                case 90:
                case 100:
                    if (player.getJob().getId() != 2000 && (combo / 10) > skillLevel) {
                        break;
                    }
                    SkillFactory.getSkill(Aran.COMBO_ABILITY).getEffect(combo / 10).applyComboBuff(player, combo);
                    break;
            }
            player.setCombo(combo);
            player.setLastCombo(currentTime);
        }
    }
}
