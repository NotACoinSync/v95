package net.server.channel.handlers;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.AdminShopDlg;
import tools.packets.WvsContext;

public class SendAdminShopRequest extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte mode = slea.readByte();
        switch (mode) {
            case AdminShopDlg.Request.OpenShop: {
                int NpcTemplateID = slea.readInt();                
                c.announce(WvsContext.enableActions());
                break;
            }
            case AdminShopDlg.Request.Trade: {
                int NpcTemplateID = slea.readInt();
                int Count = slea.readShort();
                int POS = slea.readShort();
                c.announce(WvsContext.enableActions());
                break;
            }
            case AdminShopDlg.Request.Close: {
                c.announce(WvsContext.enableActions());
                break;
            }
            default: {
                c.announce(WvsContext.enableActions());
                break;
            }
        }
    }
}
