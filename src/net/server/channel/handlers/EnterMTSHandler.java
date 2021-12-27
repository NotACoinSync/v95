package net.server.channel.handlers;

import client.player.SecondaryStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.autoban.AutobanFactory;
import constants.FeatureSettings;
import java.rmi.RemoteException;
import java.sql.SQLException;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import server.maps.FieldLimit;
import tools.packets.PacketHelper;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.ITC;
import tools.packets.WvsContext;
import tools.packets.Stage;
import tools.packets.Field;

public final class EnterMTSHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        try {
        if (!FeatureSettings.MTS) {
            /*if (!FieldLimit.CANNOTVIPROCK.check(c.getPlayer().getMap().getFieldLimit())) {
                c.getPlayer().saveLocation("FREE_MARKET");
                c.getPlayer().changeMap(910000000);
            } else {
                c.getPlayer().dropMessage(1, "You can not enter the free market from this location.");
            }*/
                c.getPlayer().dropMessage(MessageType.POPUP, FeatureSettings.MTS_DISABLED);
                return;
            }
            if (!chr.isAlive() || chr.getHiredMerchant() != null || chr.getTrade() != null) {
                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to open MTS with certain UIs open");
                c.announce(WvsContext.enableActions());
                return;
            }
            if (chr.getLevel() < 10) {
                c.announce(Field.TransferChannelReqIgnored(5));
                c.announce(WvsContext.enableActions());
                return;
            }
            if (chr.getCashShop().isOpened()) {
                return;
            }
            if (chr.getEventInstance() != null) {
                return;
            }
            if (FieldLimit.CHANGECHANNEL.check(chr.getMap().getMapData().getFieldType())) {
                return;
            }
            ChannelServer.getInstance().getWorldInterface().addBuffsToStorage(chr.getId(), chr.getAllBuffs());
            chr.cancelAllBuffs();
            chr.cancelExpirationTask();
            if (chr.getBuffedValue(SecondaryStat.PUPPET) != null) {
                chr.cancelEffectFromSecondaryStat(SecondaryStat.PUPPET);
            }
            if (chr.getBuffedValue(SecondaryStat.ComboCounter) != null) {
                chr.cancelEffectFromSecondaryStat(SecondaryStat.ComboCounter);
            }
            c.announce(Stage.SetITC(c));
            chr.getCashShop().open(2);
            chr.getMap().removePlayer(chr);
            c.getChannelServer().removePlayer(chr);
            ChannelServer.getInstance().addMTSPlayer(chr);
            c.getPlayer().changeTab(1);// meh
            c.getPlayer().changeType(0);
            c.getPlayer().changePage(0);
            c.getPlayer().setSearch(null);
            c.announce(PacketHelper.enableCSUse());
            c.announce(ITC.NormalItemResult.GetNotifyCancelWishResult(0, 0));
            c.announce(ITC.QueryCashResult(c.getPlayer()));
            c.announce(MTSHandler.getMTS(c.getPlayer(), 1, 0, 0));
            c.announce(ITC.NormalItemResult.GetUserPurchaseItem_Done(MTSHandler.getTransfer(chr.getId())));
            c.announce(ITC.NormalItemResult.GetUserSaleItem_Done(MTSHandler.getNotYetSold(chr.getId())));
        } catch (RemoteException | SQLException e) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
        }
    }
}
