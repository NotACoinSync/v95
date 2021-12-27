package net.server.channel.handlers;

import client.MapleClient;
import client.SkillFactory;
import client.autoban.AutobanFactory;
import constants.skills.BladeMaster;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;

public class ThrowGrenadeHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int x = slea.readInt();
        int y = slea.readInt();
        int playerY = slea.readInt();
        int charge = slea.readInt();
        int skillid = slea.readInt();// skillid
        int skillLevel = slea.readInt();// skill level
        if (SkillFactory.getSkill(skillid) == null || c.getPlayer().getSkillLevel(skillid) <= 0 || c.getPlayer().getSkillLevel(skillid) != skillLevel) {
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to throw grenade on a skill they don't have");
            return;
        }
        switch (skillid) {
            case BladeMaster.MonsterBomb:
                System.out.println(x + ", " + y + ", " + playerY + ", " + charge);
                break;
            default:
                Logger.log(LogType.INFO, LogFile.GENERAL_INFO, c.getPlayer() + " used ThrowGrenadeHandler with a skill that hasn't been handled: " + skillid);
                return;
        }
    }
}
