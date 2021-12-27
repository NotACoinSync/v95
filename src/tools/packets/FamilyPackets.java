package tools.packets;

import client.MapleCharacter;
import java.util.Map.Entry;
import net.SendOpcode;
import net.world.family.Family;
import net.world.family.FamilyCharacter;
import tools.data.output.MaplePacketLittleEndianWriter;

public class FamilyPackets {

    public static byte[] sendFamilyInvite(int playerId, String inviter) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FAMILY_JOIN_REQUEST.getValue());// OnFamilyJoinRequest
        oPacket.writeInt(playerId);
        oPacket.writeMapleAsciiString(inviter);
        return oPacket.getPacket();
    }

    public static byte[] sendFamilyJoinResponse(boolean accepted, String added) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FAMILY_JOIN_REQUEST_RESULT.getValue());// OnFamilyJoinRequestResult
        oPacket.write(accepted ? 1 : 0);
        oPacket.writeMapleAsciiString(added);
        return oPacket.getPacket();
    }

    public static byte[] getSeniorMessage(String name) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FAMILY_JOIN_ACCEPTED.getValue());
        oPacket.writeMapleAsciiString(name);
        oPacket.writeInt(0);
        return oPacket.getPacket();
    }

    public static byte[] sendGainRep(int gain, int mode) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FAMILY_FAMOUS_POINT_INC_RESULT.getValue());
        oPacket.writeInt(gain);
        oPacket.writeShort(0);
        return oPacket.getPacket();
    }

    public static byte[] priviliegeList(MapleCharacter player) {// CWvsContext::OnFamilyPrivilegeList
        String[] title = {"Family Reunion", "Summon Family", "My Drop Rate 1.5x (15 min)", "My EXP 1.5x (15 min)", "Family Bonding (30 min)", "My Drop Rate 2x (15 min)", "My EXP 2x (15 min)", "My Drop Rate 2x (30 min)", "My EXP 2x (30 min)", "My Party Drop Rate 2x (30 min)", "My Party EXP 2x (30 min)"};
        String[] description = {"[Target] Me\n[Effect] Teleport directly to the Family member of your choice.", "[Target] 1 Family member\n[Effect] Summon a Family member of choice to the map you're in.", "[Target] Me\n[Time] 15 min.\n[Effect] Monster drop rate will be increased #c1.5x#.\n*  If the Drop Rate event is in progress, this will be nullified.", "[Target] Me\n[Time] 15 min.\n[Effect] EXP earned from hunting will be increased #c1.5x#.\n* If the EXP event is in progress, this will be nullified.", "[Target] At least 6 Family members online that are below me in the Pedigree\n[Time] 30 min.\n[Effect] Monster drop rate and EXP earned will be increased #c2x#. \n* If the EXP event is in progress, this will be nullified.", "[Target] Me\n[Time] 15 min.\n[Effect] Monster drop rate will be increased #c2x#.\n* If the Drop Rate event is in progress, this will be nullified.", "[Target] Me\n[Time] 15 min.\n[Effect] EXP earned from hunting will be increased #c2x#.\n* If the EXP event is in progress, this will be nullified.", "[Target] Me\n[Time] 30 min.\n[Effect] Monster drop rate will be increased #c2x#.\n* If the Drop Rate event is in progress, this will be nullified.", "[Target] Me\n[Time] 30 min.\n[Effect] EXP earned from hunting will be increased #c2x#. \n* If the EXP event is in progress, this will be nullified.", "[Target] My party\n[Time] 30 min.\n[Effect] Monster drop rate will be increased #c2x#.\n* If the Drop Rate event is in progress, this will be nullified.", "[Target] My party\n[Time] 30 min.\n[Effect] EXP earned from hunting will be increased #c2x#.\n* If the EXP event is in progress, this will be nullified."};
        int[] repCost = {3, 5, 7, 8, 10, 12, 15, 20, 25, 40, 50};
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FAMILY_PRIVILEGE_LIST.getValue());
        oPacket.writeInt(11);
        for (int i = 0; i < 11; i++) {
            oPacket.write(i > 4 ? (i % 2) + 1 : i);
            oPacket.writeInt(repCost[i] * 100);
            oPacket.writeInt(1);
            oPacket.writeMapleAsciiString(title[i]);
            oPacket.writeMapleAsciiString(description[i]);
        }
        return oPacket.getPacket();
    }

    public static class FamilyRes {

        public static final int FamilyRes_Success = 0, FamilyRes_Success_Unregister = 1, FamilyRes_Fail_CanNotRegister = 64, FamilyRes_Fail_WrongName = 65, FamilyRes_Fail_SameFamily = 66, FamilyRes_Fail_NotSameFamily = 67, FamilyRes_Fail_DifferentFamily = 68, FamilyRes_Fail_NotSameMap = 69, FamilyRes_Fail_AleadyHasParent = 70, FamilyRes_Fail_OverLevel = 71, FamilyRes_Fail_UnderLevel = 72, FamilyRes_Fail_AlreadyJoining = 73, FamilyRes_Fail_AlreadySummon = 74, FamilyRes_Fail_Summon = 75, FamilyRes_Fail_MaxDepth = 76, FamilyRes_Fail_MinLevel = 77, FamilyRes_Fail_ChildReqWorldTransferUser = 78, FamilyRes_Fail_SelfReqWorldTransferUser = 79, FamilyRes_Fail_NotEnoughMoney = 80, FamilyRes_Fail_NotEnoughParentMoney = 81, FamilyRes_Fail_CanNotTeleport_ByLevellimited = 82;
    }

    /**
     * Family Result Message Possible values for <code>type</code>:<br>
     * 67: You do not belong to the same family.<br>
     * 69: The character you wish to add as\r\na Junior must be in the same
     * map.<br>
     * 70: This character is already a Junior of another character.<br>
     * 71: The Junior you wish to add\r\nmust be at a lower rank.<br>
     * 72: The gap between you and your\r\njunior must be within 20 levels.<br>
     * 73: Another character has requested to add this character.\r\nPlease try
     * again later.<br>
     * 74: Another character has requested a summon.\r\nPlease try again
     * later.<br>
     * 75: The summons has failed. Your current location or state does not allow
     * a summons.<br>
     * 76: The family cannot extend more than 1000 generations from above and
     * below.<br>
     * 77: The Junior you wish to add\r\nmust be over Level 10.<br>
     * 78: You cannot add a Junior \r\nthat has requested to change worlds.<br>
     * 79: You cannot add a Junior \r\nsince you've requested to change
     * worlds.<br>
     * 80: Separation is not possible due to insufficient Mesos.\r\nYou will
     * need %d Mesos to\r\nseparate with a Senior.<br>
     * 81: Separation is not possible due to insufficient Mesos.\r\nYou will
     * need %d Mesos to\r\nseparate with a Junior.<br>
     * 82: The Entitlement does not apply because your level does not match the
     * corresponding area.<br>
     *
     * @param type The type
     * @return Family Result packet
     */
    public static byte[] sendFamilyMessage(int type, int mesos) {// CWvsContext::OnFamilyResult
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FAMILY_RESULT.getValue());
        oPacket.writeInt(type);
        oPacket.writeInt(mesos);
        return oPacket.getPacket();
    }

    public static byte[] getFamilyInfo(Family family, FamilyCharacter fc) {// FamilyInfo::Decode(CWvsContext::OnFamilyInfoResult), move to MapleFamilyEntry?
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FAMILY_INFO_RESULT.getValue());
        oPacket.writeInt(fc.famousPoint); // cur rep left - nFamousPoint
        oPacket.writeInt(fc.totalFamousPoint); // tot rep left - nTotalFamousPoint
        oPacket.writeInt(fc.todaySavePoint); // todays rep - nTodaySavePoint
        oPacket.writeShort(0); // juniors added - wChildCount
        oPacket.writeShort(2); // juniors allowed - wChildLimit
        oPacket.writeShort(0); // wTotalChildCount
        oPacket.writeInt(family.bossID); // id? - dwBossID(Prob leader of whole family.
        oPacket.writeMapleAsciiString(family.familyName);// strFamilyName
        oPacket.writeInt(family.mPrivilegeUse.size());
        for (Entry<Integer, Integer> entry : family.mPrivilegeUse.entrySet()) {
            oPacket.writeInt(entry.getKey());
            oPacket.writeInt(entry.getValue());
        }
        oPacket.writeShort(0);//
        return oPacket.getPacket();
    }

    public static byte[] showPedigree(int chrid, Family family) {// CWvsContext::OnFamilyChartResult, CUIFamilyChart::DecodeLocalChart
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FAMILY_CHART_RESULT.getValue());
        oPacket.writeInt(chrid);
        oPacket.writeInt(family.members.size());
        for (FamilyCharacter fc : family.members.values()) {
            oPacket.writeInt(fc.characterID);
            oPacket.writeInt(fc.parent.characterID);// parent character id(dwCharacterID)
            oPacket.writeShort(fc.job);// nJob
            oPacket.write(fc.level);// nLevel
            oPacket.writeBoolean(fc.online);// bOnline
            oPacket.writeInt(fc.famousPoint);// nFamousPoint
            oPacket.writeInt(fc.totalFamousPoint);// nTotalFamousPoint
            oPacket.writeInt(0);// nTodayParentPoint
            oPacket.writeInt(0);// nTodayGrandParentPoint
            oPacket.writeInt(fc.channelID);// channel id
            oPacket.writeInt(fc.loginMin);// nLoginMin?
            oPacket.writeMapleAsciiString(fc.characterName);// strCharacterName
        }
        oPacket.writeInt(family.mStatistic.size());
        for (Entry<Integer, Integer> entry : family.mStatistic.entrySet()) {
            oPacket.writeInt(entry.getKey());
            oPacket.writeInt(entry.getValue());
        }
        oPacket.writeInt(family.mPrivilege.size());
        for (Entry<Integer, Integer> entry : family.mPrivilege.entrySet()) {
            oPacket.writeInt(entry.getKey());
            oPacket.writeInt(entry.getValue());
        }
        oPacket.writeShort(0);// enables a button or something?
        return oPacket.getPacket();
    }

    /**
     * @param chrName Person who is summoning the packet receiver.
     * @param fieldName Field they are being summoned to.
     */
    public static byte[] familySummonRequest(String chrName, String fieldName) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FAMILY_SUMMON_REQUEST.getValue());
        oPacket.writeMapleAsciiString(chrName);
        oPacket.writeMapleAsciiString(fieldName);
        return oPacket.getPacket();
    }

    public static byte[] setPrivilege(Family f, boolean set) {// Don't use Family instance?
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FAMILY_SET_PRIVILEGE.getValue());
        oPacket.writeBoolean(set);// bType
        oPacket.writeInt(0);// nIndex
        oPacket.writeInt(0);// incExpRate
        oPacket.writeInt(0);// incDropRate
        oPacket.writeLong(0);// tEnd
        return oPacket.getPacket();
    }
}
