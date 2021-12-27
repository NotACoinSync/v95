package net.server.channel.handlers;

import client.MapleClient;
import client.MessageType;
import client.autoban.AutobanFactory;
import constants.FeatureSettings;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public final class ChangeChannelHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int nTargetChannel = slea.readByte();
        c.getPlayer().getAutobanManager().setTimestamp(6, slea.readInt(), 2);
        if (c.getChannel() == nTargetChannel) {
            AutobanFactory.GENERAL.alert(c.getPlayer(), "CCing to same channel.");
            c.disconnect(false, false);
            return;
        } else if (c.getPlayer().getCashShop().isOpened() || c.getPlayer().getMiniGame() != null || c.getPlayer().getPlayerShop() != null) {
            return;
        }
        if (!FeatureSettings.CC) {
            c.getPlayer().dropMessage(MessageType.POPUP, FeatureSettings.CC_DISABLED);
            c.announce(WvsContext.enableActions());
            return;
        }
        c.announce(WvsContext.TransferChannel(nTargetChannel, "You will be going to transfer to channel " + nTargetChannel));
        c.changeChannel(nTargetChannel);}
}
