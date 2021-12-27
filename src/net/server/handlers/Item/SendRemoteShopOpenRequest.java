package net.server.handlers.Item;

import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanFactory;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import net.server.channel.Channel;
import server.maps.objects.HiredMerchant;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.MiniRoomBaseDlg;
import tools.packets.WvsContext;

public class SendRemoteShopOpenRequest extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        short slot = iPacket.readShort();
        Item toUse = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem(slot);
        if (toUse != null && toUse.getQuantity() > 0) {
            HiredMerchant hm = getMerchant(c);
            if (player.hasMerchant() && hm != null) {
                if (hm.getChannel() == player.getClient().getChannel()) {
                    hm.setOpen(false);
                    hm.removeAllVisitors("");
                    player.setHiredMerchant(hm);
                    player.announce(MiniRoomBaseDlg.EntrustedShop.getHiredMerchant(player, hm, false));
                } else {
                    c.announce(WvsContext.EntrustedShopCheckResult.remoteChannelChange((byte) hm.getChannel()));
                }
                return;
            } else {
                player.dropMessage(1, "You don't have a Merchant open");
            }
            c.announce(WvsContext.enableActions());
        }else {
            AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to use a null item in SendRemoteShopOpenRequest");
        }
    }

    public HiredMerchant getMerchant(MapleClient c) {
        if (c.getPlayer().hasMerchant()) {
            for (Channel cserv : ChannelServer.getInstance().getChannels()) {
                if (cserv.getHiredMerchants().get(c.getPlayer().getId()) != null) {
                    return cserv.getHiredMerchants().get(c.getPlayer().getId());
                }
            }
        }
        return null;
    }
}
