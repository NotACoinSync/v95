package tools.packets.CField;

import client.inventory.Item;
import java.util.List;
import net.SendOpcode;
import scripting.npc.NPCConversationManager;
import scripting.npc.ScriptMessageType;
import tools.data.output.MaplePacketLittleEndianWriter;

public class ScriptMan {

    public static byte[] Say(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sText, boolean bPrev, boolean bNext) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NPC_TALK.getValue());
        oPacket.write(nSpeakerTypeID);
        oPacket.writeInt(nSpeakerTemplateID);
        oPacket.write(ScriptMessageType.Say.getMsgType());
        oPacket.write(bParam);
        if ((bParam & 0x4) > 0)// idek xd, its a 2nd template for something
        {
            oPacket.writeInt(nSpeakerTemplateID);
        }
        oPacket.writeMapleAsciiString(sText);
        oPacket.writeBoolean(bPrev);
        oPacket.writeBoolean(bNext);
        return oPacket.getPacket();
    }

    public static byte[] SayImage(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, List<String> asPath) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NPC_TALK.getValue());
        oPacket.write(nSpeakerTypeID);
        oPacket.writeInt(nSpeakerTemplateID);
        oPacket.write(ScriptMessageType.SayImage.getMsgType());
        oPacket.write(bParam);
        oPacket.write(asPath.size());
        for (String sPath : asPath) {
            oPacket.writeMapleAsciiString(sPath);// CUtilDlgEx::AddImageList(v8, sPath);
        }
        return oPacket.getPacket();
    }

    public static byte[] SayImage(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sPath) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NPC_TALK.getValue());
        oPacket.write(nSpeakerTypeID);
        oPacket.writeInt(nSpeakerTemplateID);
        oPacket.write(ScriptMessageType.SayImage.getMsgType());
        oPacket.write(bParam);
        oPacket.write(1);
        oPacket.writeMapleAsciiString(sPath);
        return oPacket.getPacket();
    }

    public static byte[] AskYesNo(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sText) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NPC_TALK.getValue());
        oPacket.write(nSpeakerTypeID);
        oPacket.writeInt(nSpeakerTemplateID);
        oPacket.write(ScriptMessageType.AskYesNo.getMsgType());
        oPacket.write(bParam);// (bParam & 0x6)
        oPacket.writeMapleAsciiString(sText);
        return oPacket.getPacket();
    }

    public static byte[] AskAccept(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sText) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NPC_TALK.getValue());
        oPacket.write(nSpeakerTypeID);
        oPacket.writeInt(nSpeakerTemplateID);
        oPacket.write(ScriptMessageType.AskAccept.getMsgType());
        oPacket.write(bParam);
        oPacket.writeMapleAsciiString(sText);
        return oPacket.getPacket();
    }

    public static byte[] AskText(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sMsg, String sMsgDefault, int nLenMin, int nLenMax) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NPC_TALK.getValue());
        oPacket.write(nSpeakerTypeID);
        oPacket.writeInt(nSpeakerTemplateID);
        oPacket.write(ScriptMessageType.AskText.getMsgType());
        oPacket.write(bParam);// (bParam & 0x6)
        oPacket.writeMapleAsciiString(sMsg);
        oPacket.writeMapleAsciiString(sMsgDefault);
        oPacket.writeShort(nLenMin);
        oPacket.writeShort(nLenMax);
        return oPacket.getPacket();
    }

    public static byte[] AskBoxText(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sMsg, String sMsgDefault, int nCol, int nLine) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NPC_TALK.getValue());
        oPacket.write(nSpeakerTypeID);
        oPacket.writeInt(nSpeakerTemplateID);
        oPacket.write(ScriptMessageType.AskBoxText.getMsgType());
        oPacket.write(bParam);// (bParam & 0x6)
        oPacket.writeMapleAsciiString(sMsg);
        oPacket.writeMapleAsciiString(sMsgDefault);
        oPacket.writeShort(nCol);
        oPacket.writeShort(nLine);
        return oPacket.getPacket();
    }

    public static byte[] AskNumber(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sMsg, int nDef, int nMin, int nMax) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NPC_TALK.getValue());
        oPacket.write(nSpeakerTypeID);
        oPacket.writeInt(nSpeakerTemplateID);
        oPacket.write(ScriptMessageType.AskNumber.getMsgType());
        oPacket.write(bParam);// (bParam & 0x6)
        oPacket.writeMapleAsciiString(sMsg);
        oPacket.writeInt(nDef);
        oPacket.writeInt(nMin);
        oPacket.writeInt(nMax);
        return oPacket.getPacket();
    }

    public static byte[] AskMenu(int nSpeakerTypeID, int nSpeakerTemplateID, byte bParam, String sMsg) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NPC_TALK.getValue());
        oPacket.write(nSpeakerTypeID);
        oPacket.writeInt(nSpeakerTemplateID);
        oPacket.write(ScriptMessageType.AskMenu.getMsgType());
        oPacket.write(bParam);// (bParam & 0x6)
        oPacket.writeMapleAsciiString(sMsg);
        return oPacket.getPacket();
    }

    public static byte[] AskAvatar(int nSpeakerTypeID, int nSpeakerTemplateID, String sMsg, int[] anCanadite) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NPC_TALK.getValue());
        oPacket.write(nSpeakerTypeID);
        oPacket.writeInt(nSpeakerTemplateID);
        oPacket.write(ScriptMessageType.AskAvatar.getMsgType());
        oPacket.write(0);
        oPacket.writeMapleAsciiString(sMsg);
        oPacket.write(anCanadite.length);
        for (int nCanadite : anCanadite) {
            oPacket.writeInt(nCanadite);// hair id's and stuff lol
        }
        return oPacket.getPacket();
    }

    public static byte[] AskMembershopAvatar(int nSpeakerTypeID, int nSpeakerTemplateID, String sMsg, int[] aCanadite) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NPC_TALK.getValue());
        oPacket.write(nSpeakerTypeID);
        oPacket.writeInt(nSpeakerTemplateID);
        oPacket.write(ScriptMessageType.AskMemberShopAvatar.getMsgType());
        oPacket.write(0);
        oPacket.writeMapleAsciiString(sMsg);
        oPacket.write(aCanadite.length);
        for (int nCanadite : aCanadite) {
            oPacket.writeInt(nCanadite);// hair id's and stuff lol
        }
        return oPacket.getPacket();
    }

    public static byte[] AskPet(byte nSpeakerTypeID, int nSpeakerTemplateID, String sMsg, List<Item> apPet) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NPC_TALK.getValue());
        oPacket.write(nSpeakerTypeID);
        oPacket.writeInt(nSpeakerTemplateID);
        oPacket.write(ScriptMessageType.AskPet.getMsgType());
        oPacket.write(0);
        oPacket.writeMapleAsciiString(sMsg);
        oPacket.write(apPet.size());
        for (Item pPet : apPet) {
            if (pPet != null) {
                oPacket.writeLong(pPet.getPetId());
                oPacket.write(pPet.getPosition());
            }
        }
        return oPacket.getPacket();
    }

    public static byte[] AskPetAll(byte nSpeakerTypeID, int nSpeakerTemplateID, String sMsg, List<Item> apPet, boolean bExceptionExist) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NPC_TALK.getValue());
        oPacket.write(nSpeakerTypeID);
        oPacket.writeInt(nSpeakerTemplateID);
        oPacket.write(ScriptMessageType.AskPetAll.getMsgType());
        oPacket.write(0);
        oPacket.writeMapleAsciiString(sMsg);
        oPacket.write(apPet.size());
        oPacket.writeBoolean(bExceptionExist);
        for (Item pPet : apPet) {
            if (pPet != null) {
                oPacket.writeLong(pPet.getPetId());
                oPacket.write(pPet.getPosition());
            }
        }
        return oPacket.getPacket();
    }

    public static byte[] AskQuiz(int nSpeakerTypeID, int nSpeakerTemplateID, int nResCode, String sTitle, String sProblemText, String sHintText, int nMinInput, int nMaxInput, int tRemainInitialQuiz) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NPC_TALK.getValue());
        oPacket.write(nSpeakerTypeID);
        oPacket.writeInt(nSpeakerTemplateID);
        oPacket.write(ScriptMessageType.AskQuiz.getMsgType());
        oPacket.write(0);
        oPacket.write(nResCode);
        if (nResCode == NPCConversationManager.InitialQuizRes_Request) {// fail has no bytes <3
            oPacket.writeMapleAsciiString(sTitle);
            oPacket.writeMapleAsciiString(sProblemText);
            oPacket.writeMapleAsciiString(sHintText);
            oPacket.writeShort(nMinInput);
            oPacket.writeShort(nMaxInput);
            oPacket.writeInt(tRemainInitialQuiz);
        }
        return oPacket.getPacket();
    }

    public static byte[] AskSpeedQuiz(int nSpeakerTypeID, int nSpeakerTemplateID, int nResCode, int nType, int dwAnswer, int nCorrect, int nRemain, int tRemainInitialQuiz) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NPC_TALK.getValue());
        oPacket.write(nSpeakerTypeID);
        oPacket.writeInt(nSpeakerTemplateID);
        oPacket.write(ScriptMessageType.AskSpeedQuiz.getMsgType());
        oPacket.write(0);
        oPacket.write(nResCode);
        if (nResCode == NPCConversationManager.InitialQuizRes_Request) {// fail has no bytes <3
            oPacket.writeInt(nType);
            oPacket.writeInt(dwAnswer);
            oPacket.writeInt(nCorrect);
            oPacket.writeInt(nRemain);
            oPacket.writeInt(tRemainInitialQuiz);
        }
        return oPacket.getPacket();
    }

    public static byte[] AskSlideMenu(int nSpeakerTypeID, int nSpeakerTemplateID, boolean bSlideDlgEX, int nIndex, String sMsg) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NPC_TALK.getValue());
        oPacket.write(nSpeakerTypeID);
        oPacket.writeInt(nSpeakerTemplateID);
        oPacket.write(ScriptMessageType.AskSlideMenu.getMsgType());
        oPacket.write(0);
        oPacket.writeInt(bSlideDlgEX ? 1 : 0);// Neo City
        oPacket.writeInt(nIndex);// Dimensional Mirror.. There's also supportF for potions and such in higher versions.
        oPacket.writeMapleAsciiString(sMsg);
        return oPacket.getPacket();
    }

    public static byte[] getDimensionalMirror(String talk) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NPC_TALK.getValue());
        oPacket.write(4); // ?
        oPacket.writeInt(9010022);
        oPacket.write(0x0E);
        oPacket.write(0);
        oPacket.writeInt(0);
        oPacket.writeMapleAsciiString(talk);
        return oPacket.getPacket();
    }

    public static byte[] getSpeedQuiz(int npc, byte result, byte type, int objectID, int questionsCleared, int points, int timeLimit) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NPC_TALK.getValue());
        oPacket.write(1);
        oPacket.writeInt(npc);
        oPacket.write(6);
        oPacket.write(0);
        oPacket.write(result);
        oPacket.writeInt(type);
        oPacket.writeInt(objectID);
        oPacket.writeInt(questionsCleared);
        oPacket.writeInt(points);
        oPacket.writeInt(timeLimit);
        return oPacket.getPacket();
    }

    public static byte[] getNPCTalkStyle(int npc, String talk, int styles[]) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NPC_TALK.getValue());
        oPacket.write(4); // ?
        oPacket.writeInt(npc);
        oPacket.write(7);
        oPacket.write(0); // speaker
        oPacket.writeMapleAsciiString(talk);
        oPacket.write(styles.length);
        for (int i = 0; i < styles.length; i++) {
            oPacket.writeInt(styles[i]);
        }
        return oPacket.getPacket();
    }

    public static byte[] getNPCTalkNum(int npc, String talk, int def, int min, int max) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NPC_TALK.getValue());
        oPacket.write(4); // ?
        oPacket.writeInt(npc);
        oPacket.write(3);
        oPacket.write(0); // speaker
        oPacket.writeMapleAsciiString(talk);
        oPacket.writeInt(def);
        oPacket.writeInt(min);
        oPacket.writeInt(max);
        oPacket.writeInt(0);
        return oPacket.getPacket();
    }

    public static byte[] getNPCTalkText(int npc, String talk, String def) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.NPC_TALK.getValue());
        oPacket.write(4); // Doesn't matter
        oPacket.writeInt(npc);
        oPacket.write(2);
        oPacket.write(0); // speaker
        oPacket.writeMapleAsciiString(talk);
        oPacket.writeMapleAsciiString(def);// :D
        oPacket.writeInt(0);
        return oPacket.getPacket();
    }
}
