package net.server.channel.handlers;

import client.MapleClient;
import client.autoban.AutobanFactory;
import net.AbstractMaplePacketHandler;
import server.ItemInformationProvider;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.ShopDlg.*;
import tools.packets.WvsContext;

public final class SendShopDlgRequest extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        byte type = iPacket.readByte();
        switch (type) {
            case ShopReq.Buy: {
                short POS = iPacket.readShort();
                int ItemID = iPacket.readInt();
                short Count = iPacket.readShort();
                int DiscountPrice = iPacket.readInt();
                if (Count < 1) {
                    AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit a npc shop buying item: " + ItemID + " with quantity " + Count);
                    c.disconnect(true, false);
                    return;
                }
                c.getPlayer().getShop().buy(c, POS, ItemID, Count, DiscountPrice);
                break;
            }
            case ShopReq.Sell: {
                short POS = iPacket.readShort();
                int ItemID = iPacket.readInt();
                short Count = iPacket.readShort();
                if (Count < 1) {
                    AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit a npc shop selling item: " + ItemID + " with quantity " + Count);
                    c.disconnect(true, false);
                    return;
                }
                c.getPlayer().getShop().sell(c, ItemInformationProvider.getInstance().getInventoryType(ItemID), POS, Count);
                break;
            }
            case ShopReq.Recharge: {
                byte POS = (byte) iPacket.readShort();
                c.getPlayer().getShop().recharge(c, POS);
                break;
            }
            case ShopReq.Close: {
                c.getPlayer().setShop(null);
                break;
            }
            default: {
                c.announce(WvsContext.enableActions());
                break;
            }
        }
    }
}
