package tools.packets.CField.userpool;

import client.*;
import client.player.SecondaryStat;
import constants.skills.Buccaneer;
import constants.skills.ChiefBandit;
import constants.skills.Corsair;
import constants.skills.ThunderBreaker;
import java.util.*;
import net.SendOpcode;
import net.server.channel.handlers.AbstractDealDamageHandler.AttackInfo;
import net.server.guild.MapleGuild;
import server.movement.MovePath;
import tools.Pair;
import tools.data.output.LittleEndianWriter;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.packets.PacketHelper;

public class UserRemote {

    public static byte[] closeRangeAttack(MapleCharacter player, AttackInfo info) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CLOSE_RANGE_ATTACK.getValue());
        addAttackBody(oPacket, player, info);
        return oPacket.getPacket();
    }

    public static byte[] rangedAttack(MapleCharacter player, AttackInfo info) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.RANGED_ATTACK.getValue());
        addAttackBody(oPacket, player, info);
        return oPacket.getPacket();
    }

    public static byte[] magicAttack(MapleCharacter player, AttackInfo info) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MAGIC_ATTACK.getValue());
        addAttackBody(oPacket, player, info);
        return oPacket.getPacket();
    }

    public static byte[] bodyAttack(MapleCharacter player, AttackInfo info) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.ENERGY_ATTACK.getValue());
        addAttackBody(oPacket, player, info);
        return oPacket.getPacket();
    }

    private static void addAttackBody(LittleEndianWriter oPacket, MapleCharacter player, AttackInfo info) {
        oPacket.writeInt(player.getId());
        oPacket.write(info.numAttackedAndDamage);
        oPacket.write(0x5B); // nLevel
        oPacket.write(info.skillLevel);
        if (info.skillLevel > 0) {
            oPacket.writeInt(info.skillID);
        }
        oPacket.write(0);
        oPacket.writeInt(0);
        oPacket.write(info.display);// bSerialAttack = CInPacket::Decode1(v4) & 32;
        oPacket.write(info.AttackAction);// short, has bLeft and nAction
        oPacket.write(info.AttackActionType);
        oPacket.write(info.AttackSpeed);// nActionSpeed
        oPacket.write(0x0A);// nMastery
        oPacket.writeInt(info.projectile);// nBulletItemID
        for (Integer oned : info.allDamage.keySet()) {// mob, lines, crit.
            List<Pair<Integer, Boolean>> onedList = info.allDamage.get(oned);
            if (onedList != null) {
                oPacket.writeInt(oned);
                oPacket.write(0xFF);// 0x07 in another src
                if (info.skillID == ChiefBandit.MesoExplosion) {
                    oPacket.write(onedList.size());
                }
                for (Pair<Integer, Boolean> eachd : onedList) {
                    oPacket.writeBoolean(eachd.right);
                    oPacket.writeInt(eachd.left);
                }
            }
        }
        if (info.ranged) {
            oPacket.writeShort(0); // ptBallStart.x
            oPacket.writeShort(0); // ptBallStart.y
        }
        if (info.skillID == 2121001 || info.skillID == 2221001 || info.skillID == 2321001 || info.skillID == 22121000 || info.skillID == 22151001) {
            oPacket.writeInt(info.KeyDown);
        } else if (info.skillID == 33101007) {
            oPacket.writeInt(0);// dwSwallowMobTemplateID
        }
    }

    public static byte[] skillPrepare(int playerId, int nPrepareSkillID, int nPrepareSkillLevel, short flags, int nPrepareSkillActionSpeed) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SKILL_EFFECT.getValue());
        oPacket.writeInt(playerId);
        oPacket.writeInt(nPrepareSkillID);
        oPacket.write(nPrepareSkillLevel);
        oPacket.writeShort(flags);
        oPacket.write(nPrepareSkillActionSpeed);
        return oPacket.getPacket();
    }

    public static byte[] skillCancel(int playerId, int nSkillID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CANCEL_SKILL_EFFECT.getValue());
        oPacket.writeInt(playerId);
        oPacket.writeInt(nSkillID);
        return oPacket.getPacket();
    }

    public static byte[] Hit(int playerId, int nAttackIdx, int monsteridfrom, int nDamage, int fake, int direction, boolean pgmr, int pgmr_1, boolean is_pg, int oid, int pos_x, int pos_y) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.DAMAGE_PLAYER.getValue());
        oPacket.writeInt(playerId);
        oPacket.write(nAttackIdx);
        oPacket.writeInt(nDamage);
        oPacket.writeInt(monsteridfrom);
        oPacket.write(direction);
        if (pgmr) {
            oPacket.write(pgmr_1);
            oPacket.write(is_pg ? 1 : 0);
            oPacket.writeInt(oid);
            oPacket.write(6);
            oPacket.writeShort(pos_x);
            oPacket.writeShort(pos_y);
            oPacket.write(0);
        } else {
            oPacket.writeShort(0);
        }
        oPacket.writeInt(nDamage);
        if (fake > 0) {
            oPacket.writeInt(fake);
        }
        return oPacket.getPacket();
    }

    // Facial Expression presents all player in map
    public static byte[] Emotion(int playerId, int nEmotion, boolean bEmotionByItemOption) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.FACIAL_EXPRESSION.getValue());
        oPacket.writeInt(playerId);
        oPacket.writeInt(nEmotion);
        oPacket.writeInt(10000); // tDuration
        oPacket.writeBoolean(bEmotionByItemOption);
        return oPacket.getPacket();
    }

    public static byte[] setActiveEffectItem(int playerId, int nItemID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SHOW_ITEM_EFFECT.getValue());
        oPacket.writeInt(playerId);
        oPacket.writeInt(nItemID);
        return oPacket.getPacket();
    }

    // CUserLocal::throwGrenade(121) (Handling)    
    public static byte[] ShowUpgradeTombEffect(int playerId, int nPosX, int nPosY, int tKeyDown, int nSkillID, int nSLV) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SHOW_UPGRADE_TOMB_EFFECT.getValue());
        oPacket.writeInt(playerId);
        oPacket.writeInt(nPosX);
        oPacket.writeInt(nPosY);
        oPacket.writeInt(tKeyDown);
        oPacket.writeInt(nSkillID);
        oPacket.writeInt(nSLV);
        return oPacket.getPacket();
    }

    public static byte[] throwGrenade(int playerId, MovePath moves) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.THROW_GRENADE.getValue());
        oPacket.writeInt(playerId);
        moves.encode(oPacket);
        return oPacket.getPacket();
    }

    public static byte[] Move(int playerId, MovePath moves) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.MOVE_PLAYER.getValue());
        oPacket.writeInt(playerId);
        moves.encode(oPacket);
        return oPacket.getPacket();
    }

    public static byte[] setActivePortableChair(int playerId, int nPortableChairID) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.SHOW_CHAIR.getValue());
        oPacket.writeInt(playerId);
        oPacket.writeInt(nPortableChairID);
        return oPacket.getPacket();
    }

    public static class AvatarModifiedType {

        public static final int LOOK = 0x1, SPEED = 0x2, CHOCO = 0x4;
    }

    public static byte[] avatarModified(MapleCharacter player) {
        int type = 1;
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.UPDATE_CHAR_LOOK.getValue());
        oPacket.writeInt(player.getId());
        oPacket.write(type);
        if ((type & AvatarModifiedType.LOOK) > 0) {
            PacketHelper.encodeAvatarLook(oPacket, player, false);
        }
        if ((type & AvatarModifiedType.SPEED) > 0) {
            oPacket.write(0);// AttackSpeed
        }
        if ((type & AvatarModifiedType.CHOCO) > 0) {
            oPacket.write(0);// choco?
        }
        PacketHelper.encodeRingLook(oPacket, player.getCrushRings());
        PacketHelper.encodeRingLook(oPacket, player.getFriendshipRings());
        PacketHelper.encodeMarriageRingLook(oPacket, player);
        oPacket.writeInt(0);// completedSetItemID
        return oPacket.getPacket();
    }

    public static byte[] setTemporaryStat(MapleCharacter player, List<Pair<SecondaryStat, BuffDataHolder>> statups) {// CUserRemote::OnSetTemporaryStat
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
        oPacket.writeInt(player.getId());
        player.secondaryStat.encodeRemote(oPacket, statups);
        oPacket.writeShort(0);// tDelay
        return oPacket.getPacket();
    }

    public static byte[] setTemporaryPirateStat(int playerId, int buffid, int time, List<Pair<SecondaryStat, BuffDataHolder>> statups) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        boolean infusion = buffid == Buccaneer.SPEED_INFUSION || buffid == ThunderBreaker.SPEED_INFUSION || buffid == Corsair.SPEED_INFUSION;
        oPacket.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
        oPacket.writeInt(playerId);
        PacketHelper.encodeFlag(oPacket, statups);
        oPacket.writeShort(0);
        for (Pair<SecondaryStat, BuffDataHolder> statup : statups) {
            oPacket.writeInt(statup.getRight().getValue());
            oPacket.writeInt(buffid);
            oPacket.skip(infusion ? 10 : 5);
            oPacket.writeShort(time);
        }
        oPacket.writeShort(0);
        oPacket.write(2);
        return oPacket.getPacket();
    }

    public static byte[] showMonsterRiding(int playerId, MapleMount mount) { // Gtfo with this, this is just giveForeignBuff
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.GIVE_FOREIGN_BUFF.getValue());
        oPacket.writeInt(playerId);
        List<SecondaryStat> temp = new ArrayList<>();
        temp.add(SecondaryStat.RideVehicle);
        PacketHelper.encodeFlagFromList(oPacket, temp);
        oPacket.writeShort(0);
        oPacket.writeInt(mount.getItemId());
        oPacket.writeInt(mount.getSkillId());
        oPacket.writeInt(0); // Server Tick value.
        oPacket.writeShort(0);
        oPacket.write(0); // Times you have been buffed
        return oPacket.getPacket();
    }

    public static byte[] resetTemporaryStat(int playerId, long mask) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CANCEL_FOREIGN_BUFF.getValue());
        oPacket.writeInt(playerId);
        oPacket.writeLong(0);
        oPacket.writeLong(mask);
        return oPacket.getPacket();
    }

    public static byte[] resetTemporaryStat(int playerId, List<SecondaryStat> statups) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.CANCEL_FOREIGN_BUFF.getValue());
        oPacket.writeInt(playerId);
        PacketHelper.encodeFlagFromList(oPacket, statups);
        return oPacket.getPacket();
    }

    public static byte[] receiveHP(int playerId, int curhp, int maxhp) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.UPDATE_PARTYMEMBER_HP.getValue());
        oPacket.writeInt(playerId);
        oPacket.writeInt(curhp);
        oPacket.writeInt(maxhp);
        return oPacket.getPacket();
    }

    public static byte[] guildNameChanged(int playerId, String guildName) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.GUILD_NAME_CHANGED);
        oPacket.writeInt(playerId);
        oPacket.writeMapleAsciiString(guildName);
        return oPacket.getPacket();
    }

    public static byte[] guildMarkChanged(int playerId, short logoBG, byte logoBGColor, short logo, byte logoColor) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.GUILD_MARK_CHANGED);
        oPacket.writeInt(playerId);
        oPacket.writeShort(logoBG);
        oPacket.write(logoBGColor);
        oPacket.writeShort(logo);
        oPacket.write(logoColor);
        return oPacket.getPacket();
    }

    public static byte[] guildMarkChanged(int playerId, MapleGuild guild) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.GUILD_MARK_CHANGED);
        oPacket.writeInt(playerId);
        oPacket.writeShort(guild.getLogoBG());
        oPacket.write(guild.getLogoBGColor());
        oPacket.writeShort(guild.getLogo());
        oPacket.write(guild.getLogoColor());
        return oPacket.getPacket();
    }
}
