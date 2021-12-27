package net.server.handlers.Player;

import client.MapleCharacter;
import client.MapleClient;
import client.MapleStat;
import client.autoban.AutobanFactory;
import client.autoban.AutobanManager;
import constants.GameConstants;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext;

public class AutoAssignRequest extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        AutobanManager abm = player.getAutobanManager();
        int timestamp = slea.readInt();// TODO: Readd
        abm.setTimestamp(0, timestamp, 3);
        slea.skip(4);
        if (player.getRemainingAp() < 1) {
            return;
        }
        int total = 0;
        int extras = 0;
        if (slea.available() < 16) {
            AutobanFactory.PACKET_EDIT.alert(player, "Didn't send full packet for Auto Assign.");
            c.disconnect(false, false);
            return;
        }
        for (int i = 0; i < 2; i++) {
            int type = slea.readInt();
            int tempVal = slea.readInt();
            if (tempVal < 0 || tempVal > c.getPlayer().getRemainingAp()) {
                return;
            }
            total += tempVal;
            extras += gainStatByType(player, MapleStat.getBy5ByteEncoding(type), tempVal);
        }
        int remainingAp = (player.getRemainingAp() - total) + extras;
        player.setRemainingAp(remainingAp);
        player.updateSingleStat(MapleStat.AVAILABLEAP, remainingAp);
        c.announce(WvsContext.enableActions());
    }

    private int gainStatByType(MapleCharacter player, MapleStat type, int gain) {
        int newValue = 0;
        switch (type) {
            case STR:
                newValue = player.getStr() + gain;
                if (newValue > GameConstants.maxAbilityStat) {
                    player.setStr(GameConstants.maxAbilityStat);
                } else {
                    player.setStr(newValue);
                }   break;
            case INT:
                newValue = player.getInt() + gain;
                if (newValue > GameConstants.maxAbilityStat) {
                    player.setInt(GameConstants.maxAbilityStat);
                } else {
                    player.setInt(newValue);
                }   break;
            case LUK:
                newValue = player.getLuk() + gain;
                if (newValue > GameConstants.maxAbilityStat) {
                    player.setLuk(GameConstants.maxAbilityStat);
                } else {
                    player.setLuk(newValue);
                }   break;
            case DEX:
                newValue = player.getDex() + gain;
                if (newValue > GameConstants.maxAbilityStat) {
                    player.setDex(GameConstants.maxAbilityStat);
                } else {
                    player.setDex(newValue);
                }   break;
            default:
                break;
        }
        if (newValue > GameConstants.maxAbilityStat) {
            player.updateSingleStat(type, GameConstants.maxAbilityStat);
            return newValue - GameConstants.maxAbilityStat;
        }
        player.updateSingleStat(type, newValue);
        return 0;
    }
}
