package net.server.channel.handlers;

import java.sql.SQLException;
import java.util.Arrays;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.inventory.ItemFactory;
import constants.FeatureSettings;
import net.AbstractMaplePacketHandler;
import server.maps.objects.MapleMapObjectType;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.WvsContext;

public final class HiredMerchantRequest extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        if (!FeatureSettings.HIRED_MERCHANTS) {
            chr.dropMessage(MessageType.POPUP, FeatureSettings.HIRED_MERCHANTS_DISABLED);
            c.announce(WvsContext.enableActions());
            return;
        }
        if (chr.getMap().getMapObjectsInRange(chr.getPosition(), 23000, Arrays.asList(MapleMapObjectType.HIRED_MERCHANT)).isEmpty() && chr.getMapId() > 910000000 && chr.getMapId() < 910000023) {
            if (!chr.hasMerchant()) {
                try {
                    if (ItemFactory.MERCHANT.loadItems(chr.getId(), false).isEmpty() && chr.getMerchantMeso() == 0) {
                        c.announce(WvsContext.EntrustedShopCheckResult.encode((byte) 7));
                    } else {
                        chr.announce(WvsContext.EntrustedShopCheckResult.encode((byte) 9));
                    }
                } catch (SQLException ex) {
                    Logger.log(LogType.ERROR, LogFile.EXCEPTION, ex);
                }
            } else {
                // chr.dropMessage(1, "You already have a store open.");
                chr.announce(WvsContext.EntrustedShopCheckResult.encode((byte) 0xF));
            }
        } else {
            // chr.dropMessage(1, "You cannot open your hired merchant here.");
            chr.announce(WvsContext.EntrustedShopCheckResult.encode((byte) 0xB));
        }
    }
}
