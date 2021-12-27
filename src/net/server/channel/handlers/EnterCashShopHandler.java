package net.server.channel.handlers;

import client.player.SecondaryStat;
import client.MapleCharacter;
import client.MapleClient;
import client.autoban.AutobanFactory;
import net.AbstractMaplePacketHandler;
import net.channel.ChannelServer;
import server.maps.FieldLimit;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.logger.Logger;
import tools.logger.Logger.LogFile;
import tools.logger.Logger.LogType;
import tools.packets.CashShopPacket;
import tools.packets.WvsContext;
import tools.packets.Stage;

/**
 * @author Flav
 */
public class EnterCashShopHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        try {
            MapleCharacter character = c.getPlayer();
            if (!character.isAlive() || character.getHiredMerchant() != null || character.getTrade() != null) {
                AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), "Tried to open cashshop with certain UIs open");
                c.announce(WvsContext.enableActions());
                return;
            }
            if (character.getCashShop().isOpened()) {
                return;
            }
            if (character.getEventInstance() != null) {
                return;
            }
            if (FieldLimit.CHANGECHANNEL.check(character.getMap().getMapData().getFieldType())) {
                return;
            }
            ChannelServer.getInstance().getWorldInterface().addBuffsToStorage(character.getId(), character.getAllBuffs());
            character.cancelAllBuffs();
            character.cancelExpirationTask();
            if (character.getBuffedValue(SecondaryStat.PUPPET) != null) {
                character.cancelEffectFromSecondaryStat(SecondaryStat.PUPPET);
            }
            if (character.getBuffedValue(SecondaryStat.ComboCounter) != null) {
                character.cancelEffectFromSecondaryStat(SecondaryStat.ComboCounter);
            }
            c.announce(Stage.SetCashShop(c));
            character.getCashShop().open(1);
            character.getMap().removePlayer(character);
            c.getChannelServer().removePlayer(character);
            ChannelServer.getInstance().addMTSPlayer(character);
            c.announce(CashShopPacket.CashItemResult.showCashInventory(c));
            c.announce(CashShopPacket.CashItemResult.showGifts(character.getCashShop().loadGifts()));
            c.announce(CashShopPacket.CashItemResult.showWishList(character, false));
            c.announce(CashShopPacket.QueryCashResult(character));
        } catch (Exception e) {
            Logger.log(LogType.ERROR, LogFile.EXCEPTION, e);
        }
    }
}
