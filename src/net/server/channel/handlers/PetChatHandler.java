package net.server.channel.handlers;

import client.MapleClient;
import client.autoban.AutobanFactory;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.WvsContext;
import tools.packets.PetPacket;

public final class PetChatHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int petId = slea.readInt();
        /*int unk = */
        slea.readInt();
        byte n = slea.readByte();
        int nAction = slea.readByte();
        byte pet = c.getPlayer().getPetIndex(petId);
        if ((pet < 0 || pet > 3)) {
            return;
        }
        String text = slea.readMapleAsciiString();
        if (text.length() > Byte.MAX_VALUE) {
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with pets with text length of " + text.length());
            c.disconnect(true, false);
            return;
        }
        c.getPlayer().getMap().announce(c.getPlayer(), PetPacket.petChat(c.getPlayer().getId(), pet, n, nAction, text), true);
        Logger.log(LogType.INFO, LogFile.PET_CHAT, c.getPlayer().getName() + ".txt", text + "\r\nPetId: " + petId + " act: " + nAction);
        c.announce(WvsContext.enableActions());
    }
}
