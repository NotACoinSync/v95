package tools.packets;

import client.MapleCharacter;
import client.MapleClient;
import client.PlayerGMRank;
import constants.ServerConstants;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.SendOpcode;
import net.login.LoginCharacter;
import tools.Pair;
import tools.data.output.MaplePacketLittleEndianWriter;
import static tools.packets.PacketHelper.encodeCharacterInformation;
import static tools.packets.PacketHelper.getTime;

public class Login {

    // Gets a successful authentication and PIN Request packet.
    public static byte[] CheckPasswordResult(MapleClient c) { // TODO
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.LOGIN_STATUS.getValue());
        oPacket.write(0);// nNumOfCharacter
        oPacket.write(0);// sMsg + 500, 0 or 1 decodes a bunch of shit
        oPacket.writeInt(0);// not read

        oPacket.writeInt(c.getAccID()); // user id
        oPacket.write(c.getGender());
        PlayerGMRank rank = PlayerGMRank.getByLevel(c.getGMLevel());
        byte nSubGradeCode = 0;
        nSubGradeCode |= rank.getSubGrade();
        oPacket.writeBoolean(rank.getLevel() >= PlayerGMRank.GM.getLevel());// nGradeCode
        oPacket.writeShort(nSubGradeCode);
        // v90;
        // Value = (unsigned __int8)CInPacket::Decode1(v5);
        // v118 = ((unsigned int)(unsigned __int8)Value >> 8) & 1; this is for tester account.
        // v118 will only be 1 if nSubGradeCode is 0x100
        oPacket.writeBoolean(false);// nCountryID, admin accounts?
        //
        oPacket.writeMapleAsciiString(c.getAccountName());// sNexonClubID

        oPacket.write(0);// nPurchaseExp
        oPacket.write(0); // isquietbanned, nChatBlockReason
        oPacket.writeLong(0);// isquietban time, dtChatUnblockDate
        oPacket.writeLong(0); // creation time, dtRegisterDate
        oPacket.writeInt(0);// nNumOfCharacter? or just reusing a variable
        oPacket.write(2);// pin
        oPacket.write(0);
        oPacket.writeLong(0);// LABEL_120
        // Generates a random sessionID and saves it
        Random random = new Random();
        long sessionID = random.nextLong();
        c.setSessionID(sessionID);
        return oPacket.getPacket();
    }

    /**
     * Gets a login failed packet. Possible values for <code>reason</code>:<br>
     * 2: ID deleted or blocked<br>
     * 3: ID deleted or blocked<br>
     * 4: Incorrect password<br>
     * 5: Not a registered id<br>
     * 6: System error<br>
     * 7: Already logged in<br>
     * 8: System error<br>
     * 9: System error<br>
     * 10: Cannot process so many connections<br>
     * 11: Only users older than 20 can use this channel<br>
     * 13: Unable to log on as master at this ip<br>
     * 14: Wrong gateway or personal info and weird korean button<br>
     * 15: Processing request with that korean button!<br>
     * 16: Please verify your account through email...<br>
     * 17: Wrong gateway or personal info<br>
     * 21: Please verify your account through email...<br>
     * 23: License agreement<br>
     * 25: Maple Europe notice =[ FUCK YOU NEXON<br>
     * 27: Some weird full client notice, probably for trial versions<br>
     *
     * @param reason The reason logging in failed.
     * @return The login failed packet.
     */
    public static byte[] getLoginFailed(int reason) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.LOGIN_STATUS.getValue());
        oPacket.write(reason);
        oPacket.write(0);
        oPacket.writeInt(0);
        return oPacket.getPacket();
    }


    public static byte[] getPermBan(byte reason) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.LOGIN_STATUS.getValue());
        oPacket.write(2); // Account is banned
        oPacket.write(0);
        oPacket.writeInt(0);
        oPacket.write(0);
        oPacket.writeLong(getTime(-1));
        return oPacket.getPacket();
    }

    public static byte[] getTempBan(long timestampTill, byte reason) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.LOGIN_STATUS.getValue());
        oPacket.write(2);
        oPacket.write(0);
        oPacket.writeInt(0);
        oPacket.write(reason);
        oPacket.writeLong(getTime(timestampTill)); // Tempban date is handled as a 64-bit long, number of 100NS intervals since 1/1/1601. Lulz.
        return oPacket.getPacket();
    }

    public static byte[] GuestIDLoginResult() { // TODO
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.GUEST_ID_LOGIN.getValue());
        oPacket.write(0); // TODO
        oPacket.write(0); // m_nRegStatID
        oPacket.writeInt(0); // dwAccountId
        oPacket.write(0); // nGender 
        oPacket.write(0); // nCountryID 
        oPacket.write(0); // nPurchaseExp  
        oPacket.write(0);
        oPacket.writeMapleAsciiString("http://google.com"); // sGuestIDRegistrationURL
        oPacket.write(0); // sNexonClubID
        oPacket.write(0);
        oPacket.writeLong(getTime(-2));
        oPacket.writeLong(getTime(System.currentTimeMillis()));
        oPacket.writeInt(0); // nNumOfCharacter
        oPacket.writeMapleAsciiString("http://google.com"); // TODO: Change
        return oPacket.getPacket();
    }

    public static byte[] AccountInfoResult() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.ACCOUNT_INFO.getValue());
        oPacket.write(0);
        return oPacket.getPacket();
    }

    /**
     * Gets a packet detailing a server status message. Possible values for
     * <code>status</code>:<br>
     * 0 - Normal<br>
     * 1 - Highly populated<br>
     * 2 - Full
     *
     * @param status The server status.
     * @return The server status packet.
     */
    public static byte[] CheckUserLimitResult(int bOverUserLimit, int bPopulateLevel) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SERVERSTATUS.getValue());
        oPacket.write(bOverUserLimit);
        oPacket.write(bPopulateLevel);
        return oPacket.getPacket();
    }

    public static byte[] SetAccountResult() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.GENDER_DONE.getValue());
        oPacket.write(0);
        oPacket.write(0);
        return oPacket.getPacket();
    }

    public static byte[] ConfirmEULAResult() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CONFIRM_EULA_RESULT.getValue());
        oPacket.write(0);
        return oPacket.getPacket();
    }

    /**
     * Gets a packet detailing a PIN operation. Possible values for
     * <code>mode</code>:<br>
     * 0 - PIN was accepted<br>
     * 1 - Register a new PIN<br>
     * 2 - Invalid pin / Reenter<br>
     * 3 - Connection failed due to system error<br>
     * 4 - Enter the pin
     *
     * @return
     */
    public static byte[] CheckPinCodeResult(byte mode) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CHECK_PINCODE.getValue());
        oPacket.write(mode);
        return oPacket.getPacket();
    }

    public static byte[] UpdatePinCodeResult() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.UPDATE_PINCODE.getValue());
        oPacket.write(0);
        return oPacket.getPacket();
    }

    public static byte[] ViewAllCharResult(int show, int worldid, List<MapleCharacter> chars, int nCountRelatedSvrs, int nCountCharacters) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.VIEW_ALL_CHAR.getValue());
        oPacket.write(show);
        if (show == 0) {
            oPacket.write(worldid);
            oPacket.write(chars.size());
            for (MapleCharacter chr : chars) {
                encodeCharacterInformation(oPacket, chr, true);
            }
        } else {
            oPacket.writeInt(nCountRelatedSvrs);
            oPacket.writeInt(nCountCharacters);
        }
        return oPacket.getPacket();
    }

    /**
     * Possible values for <code>reason</code>:<br>
     * 2: ID deleted or blocked<br>
     * 3: ID deleted or blocked<br>
     * 4: Incorrect password<br>
     * 5: Not a registered id<br>
     * 6: Trouble logging into the game?<br>
     * 7: Already logged in<br>
     * 8: Trouble logging into the game?<br>
     * 9: Trouble logging into the game?<br>
     * 10: Cannot process so many connections<br>
     * 11: Only users older than 20 can use this channel<br>
     * 12: Trouble logging into the game?<br>
     * 13: Unable to log on as master at this ip<br>
     * 14: Wrong gateway or personal info and weird korean button<br>
     * 15: Processing request with that korean button!<br>
     * 16: Please verify your account through email...<br>
     * 17: Wrong gateway or personal info<br>
     * 21: Please verify your account through email...<br>
     * 23: Crashes<br>
     * 25: Maple Europe notice =[ FUCK YOU NEXON<br>
     * 27: Some weird full client notice, probably for trial versions<br>
     *
     * @param reason The reason logging in failed.
     * @return The login failed packet.
     */
    public static byte[] SelectCharacterByVACResult(int reason, int dwCharacterID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SELECT_CHARACTER_BY_VAC.getValue());
        oPacket.writeShort(reason); // using other types then stated above = CRASH
        oPacket.writeShort(dwCharacterID);
        return oPacket.getPacket();
    }

    /**
     * Gets a packet detailing a server and its channels.
     *
     * @param serverId
     * @param serverName The name of the server.
     * @param channelLoad Load of the channel - 1200 seems to be max.
     * @return The server info packet.
     */
    public static byte[] WorldInformation(int serverId, String serverName, int flag, String eventmsg, Map<Integer, Integer> channelLoad) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SERVERLIST.getValue());
        oPacket.write(serverId); // nWorldID
        oPacket.writeMapleAsciiString(serverName); // sName
        oPacket.write(flag); // nWorldState
        oPacket.writeMapleAsciiString(eventmsg); // sWorldEventDesc
        oPacket.writeShort(100); // nWorldEventEXP_WSE
        oPacket.writeShort(100); // nWorldEventDrop_WSE
        oPacket.write(0); // nBlockCharCreation
        if (channelLoad == null) {
            oPacket.write(0);
        } else {
            oPacket.write(channelLoad.size());
            for (int ch : channelLoad.keySet()) {
                oPacket.writeMapleAsciiString(serverName + "-" + (ch + 1));// sName
                oPacket.writeInt((channelLoad.get(ch) * 1200) / ServerConstants.CHANNEL_LOAD);// nUserNO
                oPacket.write(1);// nWorldID
                oPacket.write(ch);// nChannelID
                oPacket.writeBoolean(false);// bAdultChannel
            }
        }
        oPacket.writeShort(0);// m_nBalloonCount
        // oPacket.writeShort(nX); - Balloon X position
        // oPacket.writeShort(nY); - Balloon Y position
        // oPacket.writeMapleAsciiString("Balloon Context String");
        return oPacket.getPacket();
    }

    /**
     * Gets a packet saying that the server list is over.
     *
     * @return The end of server list packet.
     */
    public static byte[] getEndOfServerList() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SERVERLIST.getValue());
        oPacket.write(0xFF);
        return oPacket.getPacket();
    }

    /**
     * Gets a packet with a list of characters.
     *
     * @param c The MapleClient to load characters of.
     * @param serverId The ID of the server requested.
     * @return The character list packet.
     */
    public static byte[] SelectWorldResult(MapleClient c, int serverId) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CHARLIST.getValue());
        oPacket.write(0); // error
        // GW_CharacterStat::Decode
        List<LoginCharacter> chars = c.loadLoginCharacters(serverId);
        oPacket.write((byte) chars.size());
        for (LoginCharacter chr : chars) {
            PacketHelper.encodeCharacterInformation(oPacket, chr, false);
        }
        if (ServerConstants.ENABLE_PIC) {// m_bLoginOpt
            oPacket.write(c.getPic() == null || c.getPic().length() == 0 ? 0 : 1);
        } else {
            oPacket.write(2);
        }
        oPacket.writeInt(c.getCharacterSlots());// nSlotCount
        oPacket.writeInt(c.nBuyCharacterCount);// m_nBuyCharCount
        return oPacket.getPacket();
    }

    /**
     * Gets a packet telling the client the IP of the channel server.
     *
     * @param inetAddr The InetAddress of the requested channel server.
     * @param port The port the channel is on.
     * @param dwCharacterID The ID of the client.
     * @return The server IP packet.
     */
    public static byte[] SelectCharacterResult(InetAddress inetAddr, int port, int dwCharacterID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SERVER_IP.getValue());
        oPacket.write(0); // error
        /**
         * 2, 3: Deleted or blocked 4: Incorrect password 5: Not a registered id
         * 6, 8: Trouble logging in? 7: Id already logged in 10: Could not
         * process due to too many connections 11: Only those who are 20 years
         * old or older can use this. 13: Unable to log-on as a master at IP 14,
         * 17: You have either selected the wrong gateway, or you have yet to
         * change your personal information 15: Processing a request, etc, etc
         * 16: Opens 'http://passport.nexon.net/?PART=/Registration/AgeCheck'
         * 21: Please verify your account via email in order to play the game.
         * 17, 25: TODO Testing
         */
        oPacket.write(0); // dwCharacterID
        /**
         * 1, 2, 3, 19, 25, 27, 28: TODO Testing
         */
        byte[] addr = inetAddr.getAddress();
        oPacket.write(addr);
        oPacket.writeShort(port);
        oPacket.writeInt(dwCharacterID);
        oPacket.write(0); // bAuthenCode
        oPacket.writeInt(0);
        return oPacket.getPacket();
    }

    public static byte[] CheckDuplicatedIDResult(String charname, boolean nameUsed) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CHAR_NAME_RESPONSE.getValue());
        oPacket.writeMapleAsciiString(charname);
        oPacket.write(nameUsed ? 1 : 0);
        return oPacket.getPacket();
    }

    public static byte[] CreateNewCharacterResult(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.ADD_NEW_CHAR_ENTRY.getValue());
        oPacket.write(0);
        encodeCharacterInformation(oPacket, chr, false);
        return oPacket.getPacket();
    }

    /**
     * State: 0 = Deleted, 12 = invalid bday, 14 = incorrect pic, 9 = invalid
     * job, 20 = wrong pic, 10, 22, 29, 18, 24, 9, 26, 6, 35, 36 = TODO testing,
     *
     * @param cid
     * @param state
     * @return
     */
    public static byte[] DeleteCharacterResult(int cid, int state) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.DELETE_CHAR_RESPONSE.getValue());
        oPacket.writeInt(cid);
        oPacket.write(state);
        return oPacket.getPacket();
    }

    public static byte[] LatestConnectedWorld(int world) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.LAST_CONNECTED_WORLD.getValue());
        oPacket.writeInt(world);// According to GMS, it should be the world that contains the most characters (most active)
        return oPacket.getPacket();
    }

    public static byte[] RecommendWorldMessage(List<Pair<Integer, String>> worlds) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.RECOMMENDED_WORLD_MESSAGE.getValue());
        oPacket.write(worlds.size()); // size
        for (Iterator<Pair<Integer, String>> it = worlds.iterator(); it.hasNext();) {
            Pair<Integer, String> world = it.next();
            oPacket.writeInt(world.getLeft());
            oPacket.writeMapleAsciiString(world.getRight());
        }
        return oPacket.getPacket();
    }

    public static byte[] ExtraCharInfoResult() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.EXTRA_CHARINFO_RESULT.getValue());
        oPacket.writeInt(0);
        oPacket.write(0); // m_bCanHaveExtraChar
        return oPacket.getPacket();
    }

    public static byte[] CheckSPWResult() {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CHECK_SPW_RESULT.getValue());
        oPacket.write(0);
        return oPacket.getPacket();
    }
}
