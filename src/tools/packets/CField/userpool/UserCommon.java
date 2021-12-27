package tools.packets.CField.userpool;

import client.MapleCharacter;
import java.awt.Point;
import net.SendOpcode;
import server.MapleMiniGame;
import server.MaplePlayerShop;
import server.maps.objects.miniroom.MiniGame;
import server.maps.objects.miniroom.MiniRoom;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;

public class UserCommon {

    // Gets a general chat packet.
    public static byte[] Chat(int dwCharacterID, String text, boolean gm, boolean bOnlyBalloon) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CHATTEXT.getValue());
        oPacket.writeInt(dwCharacterID);
        oPacket.writeBoolean(gm);
        oPacket.writeMapleAsciiString(text);
        oPacket.writeBoolean(bOnlyBalloon);
        return oPacket.getPacket();
    }
    
    // Gets a general chat packet but bIsFromOutsideOfMap = true
    public static byte[] ChatCWKPQ(int dwCharacterID, String text, String text2, boolean gm, boolean bOnlyBalloon) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CHATTEXT_CWKPQ.getValue());
        oPacket.writeInt(dwCharacterID);
        oPacket.writeBoolean(gm);
        oPacket.writeMapleAsciiString(text);
        oPacket.writeBoolean(bOnlyBalloon);
        oPacket.writeMapleAsciiString(text2);
        return oPacket.getPacket();
    }

    public static byte[] ADBoard(int dwCharacterID, String sMsg, boolean close) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CHALKBOARD.getValue());
        oPacket.writeInt(dwCharacterID);
        oPacket.writeBoolean(close);
        if (close) {
            oPacket.writeMapleAsciiString(sMsg);
        }
        return oPacket.getPacket();
    }

    public static class MiniRoomBalloon {

        public static byte[] encode(MapleCharacter chr, MiniGame mg) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.UPDATE_CHAR_BOX);// CUser::OnMiniRoomBalloon
            oPacket.writeInt(chr.getId());
            addMiniRoomBalloon(oPacket, mg);
            return oPacket.getPacket();
        }

        public static byte[] addCharBox(MapleCharacter c, int type) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
            oPacket.writeInt(c.getId());
            addAnnounceBox(oPacket, c.getPlayerShop(), type);
            return oPacket.getPacket();
        }

        public static byte[] removeCharBox(MapleCharacter c) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter(7);
            oPacket.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
            oPacket.writeInt(c.getId());
            oPacket.write(0);
            return oPacket.getPacket();
        }

        public static byte[] addOmokBox(MapleCharacter c, int ammount, int type) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
            oPacket.writeInt(c.getId());
            addAnnounceBox(oPacket, c.getMiniGame(), 1, 0, ammount, type);
            return oPacket.getPacket();
        }

        public static byte[] removeOmokBox(MapleCharacter c) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter(7);
            oPacket.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
            oPacket.writeInt(c.getId());
            oPacket.write(0);
            return oPacket.getPacket();
        }

        public static byte[] addMatchCardBox(MapleCharacter c, int ammount, int type) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
            oPacket.writeInt(c.getId());
            addAnnounceBox(oPacket, c.getMiniGame(), 2, 0, ammount, type);
            return oPacket.getPacket();
        }

        public static byte[] removeMatchcardBox(MapleCharacter c) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.UPDATE_CHAR_BOX.getValue());
            oPacket.writeInt(c.getId());
            oPacket.write(0);
            return oPacket.getPacket();
        }

        public static void addMiniRoomBalloon(LittleEndianWriter lew, MiniRoom mr) {
            lew.write(mr.getMiniRoomType());// nMiniRoomType
            lew.writeInt(mr.getObjectId());// m_dwMiniRoomSN
            lew.writeMapleAsciiString(mr.getTitle());// m_sMiniRoomTitle
            lew.writeBoolean(mr.getPassword() != null);// m_bPrivate
            lew.write(mr.getGameType());/// m_nGameKind
            lew.write(mr.getCurrentUsers());// m_nCurUsers
            lew.write(mr.getMaxSlots());// m_nMaxUsers
            lew.writeBoolean((mr instanceof MiniGame ? ((MiniGame) mr).hasStarted() : false));
        }

        public static void addAnnounceBox(final MaplePacketLittleEndianWriter oPacket, MaplePlayerShop shop, int availability) {
            oPacket.write(4);
            oPacket.writeInt(shop.getObjectId());
            oPacket.writeMapleAsciiString(shop.getDescription());
            oPacket.write(0);
            oPacket.write(0);
            oPacket.write(1);
            oPacket.write(availability);
            oPacket.write(0);
        }

        public static void addAnnounceBox(final MaplePacketLittleEndianWriter oPacket, MapleMiniGame game, int gametype, int type, int ammount, int joinable) {
            oPacket.write(gametype);
            oPacket.writeInt(game.getObjectId()); // gameid/shopid
            oPacket.writeMapleAsciiString(game.getDescription()); // desc
            oPacket.writeBoolean(game.getPassword() != null);
            oPacket.write(type);
            oPacket.write(ammount);
            oPacket.write(2);
            oPacket.write(joinable);
        }
    }
    
    public static byte[] SetConsumeItemEffect(int dwCharacterID, int nConsumeItemID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SHOW_CONSUME_EFFECT.getValue());
        oPacket.writeInt(dwCharacterID);
        oPacket.writeInt(nConsumeItemID);
        // CUser::SetConsumeItemEffect(CUser chr1, CUser chr2, int nConsumeItemID);
        return oPacket.getPacket();
    }
    
    public static byte[] showItemUpgradeEffect(int dwCharacterID, boolean bSuccess, boolean bCursed, boolean bEnchantSkill, int nEnchantCategory, boolean uWhiteScroll, boolean bRecoverable) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SHOW_SCROLL_EFFECT.getValue());
        oPacket.writeInt(dwCharacterID);
        oPacket.writeBoolean(bSuccess);
        oPacket.writeBoolean(bCursed);
        oPacket.writeBoolean(bEnchantSkill);
        oPacket.writeInt(nEnchantCategory);
        oPacket.writeBoolean(uWhiteScroll);
        oPacket.writeBoolean(bRecoverable);
        // Các hiệu ứng sau khi sử dụng cuộn giấy vào vật phẩm
        return oPacket.getPacket();
    }

    public static byte[] showItemHyperUpgradeEffect(int dwCharacterID, boolean bSuccess, boolean bCursed, boolean bEnchantSkill, int nEnchantCategory) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SHOW_ITEM_HYPER_UPGRADE_EFFECT.getValue());
        oPacket.writeInt(dwCharacterID);
        oPacket.writeBoolean(bSuccess);
        oPacket.writeBoolean(bCursed);
        oPacket.writeBoolean(bEnchantSkill);
        oPacket.writeInt(nEnchantCategory);
        // Các hiệu ứng sau khi sử dụng cuộn giấy vào vật phẩm
        return oPacket.getPacket();
    }

    public static byte[] showItemOptionUpgradeEffect(int dwCharacterID, boolean bSuccess, boolean bCursed, boolean bEnchantSkill, int nEnchantCategory) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SHOW_ITEM_OPTION_UPGRADE_EFFECT.getValue());
        oPacket.writeInt(dwCharacterID);
        oPacket.writeBoolean(bSuccess);
        oPacket.writeBoolean(bCursed);
        oPacket.writeBoolean(bEnchantSkill);
        oPacket.writeInt(nEnchantCategory);
        // Các hiệu ứng sau khi sử dụng cuộn giấy vào vật phẩm
        return oPacket.getPacket();
    }

    public static byte[] showItemReleaseEffect(int dwCharacterID, short nPos) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SHOW_ITEM_RELEASE_EFFECT.getValue());
        oPacket.writeInt(dwCharacterID);
        oPacket.writeShort(nPos);
        // Potential revealed!
        // MagnifierSuccess
        return oPacket.getPacket();
    }

    public static byte[] showItemUnreleaseEffect(int dwCharacterID, boolean bSuccess) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SHOW_ITEM_UNRELEASE_EFFECT.getValue());
        oPacket.writeInt(dwCharacterID);
        oPacket.writeBoolean(bSuccess);
        // Potential successfully reset.\r\nMiracle Cube Fragment obtained!
        // Resetting Potential has failed due to insufficient space in the Use item.
        return oPacket.getPacket();
    }

    public static byte[] HitByUser(int dwCharacterID, int nDelta) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.HIT_BY_USER.getValue());
        oPacket.writeInt(dwCharacterID);
        oPacket.writeInt(nDelta);
        // nếu nDelta > 0 => Thay đổi Emotion nhân vật với ID (dwCharacterID)
        // tAlertRemain = 5000;
        // CUser::MakeIncDecHPEffect(CUser chr, int -nDelta, bGuard = false);
        return oPacket.getPacket();
    }
    
    // Không dùng
    public static byte[] TeslaTriangle(int dwCharacterID, int dwID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.TESLA_TRIANGLE.getValue());
        oPacket.writeInt(dwCharacterID);
        oPacket.writeInt(dwID);
        return oPacket.getPacket();
    }

    public static byte[] followCharacter(int dwCharacterID, int driverID) {
        return followCharacter(dwCharacterID, driverID, null);
    }

    public static byte[] removeFollow(int dwCharacterID, Point transferFieldPos) {
        return followCharacter(dwCharacterID, 0, transferFieldPos);
    }

    private static byte[] followCharacter(int dwCharacterID, int driverID, Point transferFieldPos) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FOLLOW_CHARACTER.getValue());
        oPacket.writeInt(dwCharacterID);
        oPacket.writeInt(driverID);
        oPacket.writeBoolean(transferFieldPos != null);
        if (transferFieldPos != null) {
            oPacket.writeInt(transferFieldPos.x);
            oPacket.writeInt(transferFieldPos.y);
        }
        return oPacket.getPacket();
    }
    
    // CUIPQReward::SelectReward(136) (Handling)
    public static byte[] ShowPQReward(int dwCharacterID, int show) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SHOW_PQ_REWARD.getValue());
        oPacket.writeInt(dwCharacterID);
        for (int i = 1; i <= 6; i++) {
            oPacket.write(show);        
        }
        return oPacket.getPacket();
    }
    
    public static byte[] SetPhase(int dwCharacterID, int nPhase, boolean isRegisterFadeInOutAnimation) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SET_PHASE.getValue());
        oPacket.writeInt(dwCharacterID);
        oPacket.writeInt(nPhase);
        oPacket.writeBoolean(isRegisterFadeInOutAnimation);
        return oPacket.getPacket();
    }
    
    public static byte[] showRecoverUpgradeCountEffect(int dwCharacterID, int nPhase, boolean isRegisterFadeInOutAnimation) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SHOW_RECOVERY_UPGRADE_COUNT_EFFECT.getValue());
        oPacket.writeInt(dwCharacterID);
        return oPacket.getPacket();
    }
}
