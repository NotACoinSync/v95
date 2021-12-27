package net.server.channel.handlers;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 *
 * @since Nov 3, 2017
 */
public final class MobSkillDelayEndHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        // from bms
        slea.readInt();// objectid
        slea.readShort();// moveAction
        slea.readBoolean();// cheatResult
        slea.readShort();// mp
        slea.readByte();// nSkillCommand
        slea.readByte();// nSLV
    }
}
