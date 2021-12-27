package net.server.handlers.DamageMeter;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public final class SaveDamageMeterInfoHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        iPacket.readShort(); // 7697u
        String MaxAverageDamage = iPacket.readMapleAsciiString();
        iPacket.readShort(); // 7698u
        String AverageDamage = iPacket.readMapleAsciiString();
        if (c.getPlayer() == null) {
            return;
        }
        if (AddDamageInfo.getActivate()) {
            int MADamage = Integer.parseInt(MaxAverageDamage);
            int ADamage = Integer.parseInt(AverageDamage);
            int TotalDamage = AddDamageInfo.getTotalDamage();
            if (MADamage >= ADamage && TotalDamage <= MADamage) {
                AddDamageInfo.setMaxAverageDamage(MADamage);
                AddDamageInfo.setAverageDamage(ADamage);
            } else {
                c.getPlayer().message("Maximum Average Damage cannot lower than Average Damage.");
            }
        }
    }
}
