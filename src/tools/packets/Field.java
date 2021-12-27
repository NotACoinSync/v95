package tools.packets;

import client.MapleCharacter;
import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;

public class Field {

    /**
     Gets a "block" packet (ie. the cash shop is unavailable, etc) Possible
     values for type:
     1: This portal is closed for now.
     2: You cannot go to that place.
     3: Unable to approach due to the force of the ground.
     4: You cannot teleport to or on this map.
     5: Unable to approach due to the force of the ground.
     6: This map can only be entered by party members.
     7: The Cash Shop is currently not available. Stay tuned...

     @param type The type

     @return The "block" packet.
     */
    public static byte[] TransferFieldReqIgnored(int type) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.BLOCKED_MAP.getValue());
        oPacket.write(type);
        return oPacket.getPacket();
    }

    /**
     Gets a "block" packet (ie. the cash shop is unavailable, etc) Possible
     values for type:
     1: You cannot move that channel. Please try again later.
     2: You cannot go into the cash shop. Please try again later.
     3: The Item-Trading Shop is currently unavailable. Please try again
     later.
     4: You cannot go into the trade shop, due to limitation of user
     count.
     5: You do not meet the minimum level requirement to access the Trade
     Shop.

     @param type The type

     @return The "block" packet.
     */
    public static byte[] TransferChannelReqIgnored(int type) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.BLOCKED_SERVER.getValue());
        oPacket.write(type);
        return oPacket.getPacket();
    }

    /**
     mode: 0 buddychat; 1 partychat; 2 guildchat
     */
    public static byte[] GroupMessage(String sender, String chattext, int mode) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MULTICHAT.getValue());
        oPacket.write(mode);
        oPacket.writeMapleAsciiString(sender);
        oPacket.writeMapleAsciiString(chattext);
        return oPacket.getPacket();
    }

    public static class Whisper {

        /**
         @param target
         @param mapid
         @param MTSmapCSchannel 0: MTS 1: Map 2: CS 3: Different Channel

         @return
         */
        public static byte[] getFindReply(String target, int mapid, int MTSmapCSchannel) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.WHISPER.getValue());
            oPacket.write(9);
            oPacket.writeMapleAsciiString(target);
            oPacket.write(MTSmapCSchannel); // 0: mts 1: map 2: cs
            oPacket.writeInt(mapid); // -1 if mts, cs
            if (MTSmapCSchannel == 1) {
                oPacket.write(new byte[8]);
            }
            return oPacket.getPacket();
        }

        public static byte[] getWhisper(String sender, int channel, String text) {
            return getWhisper(sender, channel, false, text);
        }

        public static byte[] getWhisper(String sender, int channel, boolean bFromAdmin, String text) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.WHISPER.getValue());
            oPacket.write(0x12);
            oPacket.writeMapleAsciiString(sender);
            oPacket.write(channel);
            oPacket.writeBoolean(bFromAdmin);// bFromAdmin. ignores blacklist
            oPacket.writeMapleAsciiString(text);
            return oPacket.getPacket();
        }

        /**
         @param target name of the target character
         @param reply  error code: 0x0 = cannot find char, 0x1 = success

         @return the MaplePacket
         */
        public static byte[] getWhisperReply(String target, byte reply) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.WHISPER.getValue());
            oPacket.write(0x0A); // whisper?
            oPacket.writeMapleAsciiString(target);
            oPacket.write(reply);
            return oPacket.getPacket();
        }
    }

    public static class CoupleMessage {

        public static byte[] sendSpouseChat(MapleCharacter wife, String msg) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SPOUSE_CHAT.getValue());
            oPacket.writeMapleAsciiString(wife.getName());
            oPacket.writeMapleAsciiString(msg);
            return oPacket.getPacket();
        }

        public static byte[] onCoupleMessage(String fiance, String text, boolean spouse) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.SPOUSE_CHAT.getValue());
            oPacket.write(spouse ? 5 : 4); // v2 = CInPacket::Decode1(a1) - 4;
            if (spouse) { // if ( v2 ) {
                oPacket.writeMapleAsciiString(fiance);
            }
            oPacket.write(spouse ? 5 : 1);
            oPacket.writeMapleAsciiString(text);
            return oPacket.getPacket();
        }
    }

    public static byte[] SummonItemInavailable() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SUMMON_ITEM_INAVAILABLE.getValue());
        oPacket.write(0);
        return oPacket.getPacket();
    }

    public static class FieldEffect {

        public static byte[] showBossHP(int oid, int currHP, int maxHP, byte tagColor, byte tagBgColor) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.FIELD_EFFECT.getValue());
            oPacket.write(5);
            oPacket.writeInt(oid);
            oPacket.writeInt(currHP);
            oPacket.writeInt(maxHP);
            oPacket.write(tagColor);
            oPacket.write(tagBgColor);
            return oPacket.getPacket();
        }

        /**
         public enum FieldEffect { Summon(0x0), Tremble(0x1), Object(0x2),
         Object_Disable(0x3), Screen(0x3), Sound(0x4), MobHPTag(0x5),
         ChangeBGM(0x6), BGMVolumeOnly(0x8), BGMVolume(0x9),
         RewordRullet(0x7), TopScreen(0xB), Screen_Delayed(0xC),
         TopScreen_Delayed(0xD), Screen_AutoLetterBox(0xE), FloatingUI(0xF),
         Blind(0x10), GrayScale(0x11), OnOffLayer(0x12), Overlap(0x13),
         Overlap_Detail(0x14), Remove_Overlap_Detail(0x15), ColorChange(0x16),
         StageClear(0x17), TopScreen_WithOrigin(0x18), SpineScreen(0x19),
         OffSpineScreen(0x1A);
         */
        public static byte[] environmentChange(String env, int mode) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.FIELD_EFFECT.getValue());
            oPacket.write(mode);
            oPacket.writeMapleAsciiString(env);
            return oPacket.getPacket();
        }

        public static byte[] mapEffect(String path) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.FIELD_EFFECT.getValue());
            oPacket.write(3);
            oPacket.writeMapleAsciiString(path);
            return oPacket.getPacket();
        }

        public static byte[] mapSound(String path) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.FIELD_EFFECT.getValue());
            oPacket.write(4);
            oPacket.writeMapleAsciiString(path);
            return oPacket.getPacket();
        }

        public static byte[] musicChange(String song) {
            return environmentChange(song, 6);
        }

        public static byte[] showEffect(String effect) {
            return environmentChange(effect, 3);
        }

        public static byte[] playSound(String sound) {
            return environmentChange(sound, 4);
        }

        public static byte[] sendDojoAnimation(byte firstByte, String animation) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.FIELD_EFFECT.getValue());
            oPacket.write(firstByte);
            oPacket.writeMapleAsciiString(animation);
            return oPacket.getPacket();
        }

        /**
         @param type  - (0:Light&Long 1:Heavy&Short)
         @param delay - seconds

         @return
         */
        public static byte[] trembleEffect(int type, int delay) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.FIELD_EFFECT.getValue());
            oPacket.write(1);
            oPacket.write(type);
            oPacket.writeInt(delay);
            return oPacket.getPacket();
        }
    }

    /**
     nState: 2 = off, 1 = on;
     */
    public static byte[] FieldObstacleOnOff(String nText, int nState) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FIELD_OBSTACLE_ONOFF.getValue());
        oPacket.writeMapleAsciiString(nText);
        oPacket.writeInt(nState);
        return oPacket.getPacket();
    }

    public static byte[] FieldObstacleOnOffStatus(String nText, int nState) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FIELD_OBSTACLE_ONOFF_STATUS.getValue());
        oPacket.writeInt(nState);
        if (nState > 0) {
            oPacket.writeMapleAsciiString(nText);
            oPacket.writeInt(nState);
        }
        return oPacket.getPacket();
    }

    public static byte[] FieldObstacleAllReset() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FIELD_OBSTACLE_ALL_RESET.getValue());
        return oPacket.getPacket();
    }

    public static class BlowWeather {

        public static byte[] startMapEffect(String sMsg, int nItemID, boolean nBlowType) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.BLOW_WEATHER.getValue());
            oPacket.write(nBlowType ? 0 : 1);
            oPacket.writeInt(nItemID);
            if (nBlowType) {
                oPacket.writeMapleAsciiString(sMsg);
            }
            return oPacket.getPacket();
        }

        public static byte[] resetMapEffects() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.BLOW_WEATHER.getValue());
            oPacket.write(0);
            oPacket.writeInt(0);
            return oPacket.getPacket();
        }
    }

    public static byte[] PlayJukeBox(int nJukeBoxItemID, String Msg) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.PLAY_JUKEBOX.getValue());
        oPacket.writeInt(nJukeBoxItemID);
        oPacket.writeMapleAsciiString(Msg);
        return oPacket.getPacket();
    }

    public static class AdminResult {

        /**
         Gets GM effect packet (ie. hide, banned, etc.):
         0x04: You have successfully blocked access.
         0x05: The unblocking has been successful.
         0x06: Mode 0: You have successfully removed the name from the ranks -
         Mode 1: You have entered an invalid character name.
         0x10: GM Hide, mode determines whether or not it is on.
         0x1E: Mode 0: Failed to send warning - Mode 1: Sent warning
         0x13: Mode 0: + mapid - Mode 1: + ch (FF = Unable to find merchant)
         */
        public static byte[] getGMEffect(byte type, byte mode) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ADMIN_RESULT.getValue());
            oPacket.write(type);
            oPacket.write(mode);
            return oPacket.getPacket();
        }

        public static byte[] findMerchantResponse(boolean map, int extra) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ADMIN_RESULT.getValue());
            oPacket.write(0x13);
            oPacket.write(map ? 0 : 1); // 00 = mapid, 01 = ch
            if (map) {
                oPacket.writeInt(extra);
            } else {
                oPacket.write(extra); // -1 = unable to find
            }
            oPacket.write(0);
            return oPacket.getPacket();
        }

        public static byte[] disableMinimap() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.ADMIN_RESULT.getValue());
            oPacket.writeShort(0x1C);
            return oPacket.getPacket();
        }
    }

    public static byte[] Quiz(boolean bQuestion, byte nCategory, int pProblem) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.OX_QUIZ.getValue());
        oPacket.writeBoolean(bQuestion);
        oPacket.write(nCategory);
        oPacket.writeShort(pProblem);
        return oPacket.getPacket();
    }

    public static byte[] Desc(int v3) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.GMEVENT_INSTRUCTIONS.getValue());
        oPacket.write(v3); // v3 là string
        return oPacket.getPacket();
    }

    public static byte[] SetQuestClear() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SET_QUEST_CLEAR.getValue());
        return oPacket.getPacket();
    }

    public static byte[] SetQuestTime(int quests, int nQuestID, long ftStart, long ftEnd) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SET_QUEST_TIME.getValue());
        oPacket.write(quests);
        if (quests > 0) {
            do {
                oPacket.writeInt(nQuestID);
                oPacket.writeLong(ftStart);
                oPacket.writeLong(ftEnd);
                --quests;
            } while (quests > 0);
        }
        return oPacket.getPacket();
    }

    public static byte[] WarnMessage(String Msg) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.WARN_MESSAGE.getValue());
        oPacket.writeMapleAsciiString(Msg);
        return oPacket.getPacket();
    }

    public static byte[] SetObjectState(String Msg, int nState) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SET_OBJECT_STATE.getValue());
        oPacket.writeMapleAsciiString(Msg);
        oPacket.writeInt(nState);
        return oPacket.getPacket();
    }

    public static byte[] FootHoldInfo() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FOOTHOLD_INFO.getValue());
        oPacket.writeInt(0);
        return oPacket.getPacket();
    }

    public static byte[] RequestFootHoldInfo() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.REQUEST_FOOTHOLD_INFO.getValue());
        return oPacket.getPacket();
    }

    public static byte[] HontaleTimer(int mode, int v3) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.HONTALE_TIMER.getValue());
        oPacket.write(mode); // mode == 1, mode >= 3
        oPacket.write(v3);
        return oPacket.getPacket();
    }

    public static byte[] ChaosZakumTimer(boolean isDead, int mode) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CHAOS_ZAKUM_TIMER.getValue());
        oPacket.writeBoolean(isDead); // mode == 1, mode >= 3
        oPacket.writeInt(mode); // v3 >= 0
        return oPacket.getPacket();
    }

    public static byte[] HontailTimer(boolean isDead, int mode) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.HONTAIL_TIMER.getValue());
        oPacket.writeBoolean(isDead); // mode == 1, mode >= 3
        oPacket.writeInt(mode); // v3 >= 0
        return oPacket.getPacket();
    }

    public static byte[] ZakumTimer(boolean isDead, int mode) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.ZAKUM_TIMER.getValue());
        oPacket.writeBoolean(isDead); // mode == 1, mode >= 3
        oPacket.writeInt(mode); // v3 >= 0
        return oPacket.getPacket();
    }
}
