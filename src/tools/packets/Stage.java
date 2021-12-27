package tools.packets;

import java.awt.Point;
import client.MapleCharacter;
import client.MapleClient;
import constants.GameConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.SendOpcode;
import server.cashshop.BestItem;
import server.cashshop.CashItemData;
import server.cashshop.CashItemFactory;
import server.cashshop.CategoryDiscount;
import server.cashshop.LimitedGood;
import server.maps.MapleMap;
import tools.HexTool;
import tools.Randomizer;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;

public class Stage {

    private static final byte[] bestItems = HexTool.getByteArrayFromHexString("02 00 00 00 31 00 00 00 0A 00 10 00 12 00 0E 07 E0 3B 8B 0B 60 CE 8A 0B 69 00 6C 00 6C 00 2F 00 35 00 33 00 32 00 30 00 30 00 31 00 31 00 2F 00 73 00 75 00 6D 00 6D 00 6F 00 6E 00 2F 00 61 00 74 00 74 00 61 00 63 00 6B 00 31 00 2F 00 31 0000 00 00 00 00 00 00 00 02 00 1A 00 04 01 08 07 02 00 00 00 32 00 00 00 05 00 1C 00 06 00 08 07 A0 01 2E 00 58 CD 8A 0B");

    /**
     Gets a packet telling the client to change maps or Gets character info
     for a character.

     @param to             The MapleMap to warp to.
     @param spawnPoint     The spawn portal number to spawn at.
     @param spawnPosition  The spawn position to spawn at.
     @param character      The character warping to
     @param bCharacterData

     @return The map change packet.
     */
    public static byte[] SetField(MapleMap to, byte spawnPoint, Point spawnPosition, MapleCharacter character, boolean bCharacterData) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SET_FIELD.getValue());
        oPacket.writeShort(0); // CClientOptMan::DecodeOpt

        oPacket.writeInt(character.getClient().getChannel());
        oPacket.writeInt(0);
        oPacket.write(0); // sNotifierMessage        
        oPacket.writeBoolean(bCharacterData);
        oPacket.writeShort(0);// nNotifierCheck

        if (bCharacterData) {
            character.getCRand().randomize();
            oPacket.writeInt((int) character.getCRand().seed1);
            oPacket.writeInt((int) character.getCRand().seed2);
            oPacket.writeInt((int) character.getCRand().seed3);
            PacketHelper.encodeCharacter(oPacket, character);
            setLogoutGiftConfig(character, oPacket);
        } else {
            oPacket.write(0);// revive
            oPacket.writeInt(to.getId());
            oPacket.write(spawnPoint);
            oPacket.writeInt(character.getHp());
            oPacket.writeBoolean(spawnPosition != null);
            if (spawnPosition != null) {
                oPacket.writeInt(spawnPosition.x);
                oPacket.writeInt(spawnPosition.y);
            }
        }
        oPacket.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        return oPacket.getPacket();
    }

    public static byte[] SetITC(MapleClient c) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SET_ITC.getValue());
        PacketHelper.encodeCharacter(oPacket, c.getPlayer());
        // CITC:LoadData
        oPacket.writeMapleAsciiString(c.getAccountName()); // m_sNexonClubID
        oPacket.writeInt(GameConstants.nRegisterFeeMeso);
        oPacket.writeInt(GameConstants.nCommissionRate);
        oPacket.writeInt(GameConstants.nCommissionBase);
        oPacket.writeInt(GameConstants.nAuctionDurationMin);
        oPacket.writeInt(GameConstants.nAuctionDurationMax);
        oPacket.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        return oPacket.getPacket();
    }

    // TODO: Implements Cashshop
    public static byte[] SetCashShop(MapleClient c) throws Exception {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SET_CASH_SHOP.getValue());
        PacketHelper.encodeCharacter(oPacket, c.getPlayer());

        // start of CCashShop::CCashShop(v5, iPacket); and CCashShop::LoadData(v2, iPacket);                
        boolean bCashShopAuthorized = true;
        oPacket.writeBoolean(bCashShopAuthorized);
        if (bCashShopAuthorized) {
            oPacket.writeMapleAsciiString(c.getAccountName()); // m_sNexonClubID
        }
        // CWvsContext::SetSaleInfo
        oPacket.writeInt(0); // NotSaleCount
        oPacket.writeShort(CashItemFactory.getModifiedCommodity().size());
        for (CashItemData cmd : CashItemFactory.getModifiedCommodity()) {
            cmd.encode(oPacket);// CS_COMMODITY::DecodeModifiedData
        }
        oPacket.write(CashItemFactory.categoryDiscount.size());
        for (CategoryDiscount cd : CashItemFactory.categoryDiscount) {
            cd.encode(oPacket);
        }
        // aBest(buffer of 1080)
        for (int commodityGender = 0; commodityGender <= 1; commodityGender++) {
            int index = 0;
            int[] aBest = {50200004, 50200069, 50200117, 50100008, 50000047};
            List<BestItem> bestItems = new ArrayList<>(CashItemFactory.bestItems.values());
            Collections.sort(bestItems);
            for (BestItem bItem : bestItems) {
                if ((bItem.nCommodityGender == 2 || bItem.nCommodityGender == commodityGender)) {
                    aBest[index++] = bItem.sn;
                }
                if (index == aBest.length) {
                    break;
                }
            }
            for (int commodityCategory = 0; commodityCategory < 9; commodityCategory++) {
                for (int SN : aBest) {
                    oPacket.writeInt(commodityCategory);
                    oPacket.writeInt(commodityGender);
                    oPacket.writeInt(SN);
                }
            }
            bestItems.clear();
        }
        // CCashShop::DecodeStock, CStockInfo::EncodeStock
        List<Integer> stockSN = new ArrayList<>();
        for (LimitedGood lg : CashItemFactory.limitedGoods) {
            for (int sn : lg.nSN) {
                if (sn != 0) {
                    stockSN.add(sn);
                }
            }
        }
        oPacket.writeShort(stockSN.size());// decodeBuffer 8 * amount
        for (int sn : stockSN) {
            oPacket.writeInt(sn);// nSN
            LimitedGood lg = CashItemFactory.getGoodFromSN(sn);
            oPacket.writeInt(lg.getStockState(CashItemFactory.getItem(sn)));// nStockState,
        }
        // CCashShop::DecodeLimitGoods, CLimitSell::EncodeLimitGoods
        oPacket.writeShort(CashItemFactory.limitedGoods.size());// decodeBuffer 104 * amount
        for (LimitedGood lg : CashItemFactory.limitedGoods) {
            lg.encode(oPacket);
        }
        // CCashShop::DecodeZeroGoods
        oPacket.writeShort(0);// this shit broken af decodeBuffer 68 * amount.
        for (int i = 0; i < 0; i++) {
            oPacket.writeInt(0);// nStartSN
            oPacket.writeInt(0);// nEndSN
            oPacket.writeInt(50);// nGoodsCount
            oPacket.writeInt(10102346);// nEventSN
            oPacket.writeInt(40);// nExpireDays
            oPacket.writeInt(0);// dwConditionFlag
            oPacket.writeInt(0);// nDateStart
            oPacket.writeInt(31);// nDateEnd
            oPacket.writeInt(0);// nHourStart
            oPacket.writeInt(24);// nHourEnd
            List<Boolean> abWeek = new ArrayList<>(Collections.nCopies(7, true));
            for (boolean week : abWeek) {
                oPacket.writeInt(week ? 1 : 0);// abWeek
            }
        }
        // end of CCashShop::LoadData
        oPacket.write(0);// bEventOn - apparently never used
        oPacket.writeInt(75);// nHighestCharacterLevelInThisAccount
        return oPacket.getPacket();
    }

    private static void setLogoutGiftConfig(MapleCharacter character, LittleEndianWriter lew) {
        lew.writeInt(character.getPredictQuit()); // bPredictQuit
        for (int i = 0; i < 3; i++) { // LogoutGiftCommoditySN
            lew.writeInt(Randomizer.rand(5202000, 5202002)); // CommodityID
        }
    }
}
