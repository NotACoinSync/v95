package tools.packets;

import constants.GameConstants;
import constants.skills.BladeMaster;
import java.util.Map;
import java.util.Map.Entry;
import net.SendOpcode;
import tools.data.output.MaplePacketLittleEndianWriter;
import tools.packets.CField.userpool.UserEffectType;

public class EffectPacket {

    public static byte[] encode(int effect) {
        final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
        oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
        oPacket.write(effect);
        System.out.println("encode " + effect);
        return oPacket.getPacket();
    }

    public static class Local {

        public static byte[] LevelUp() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.LevelUp);
            return oPacket.getPacket();
        }

        public static class SkillUse {

            public static byte[] encode(int skillID, int CharLevel, int skillLevel) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(skillID);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                return oPacket.getPacket();
            }

            public static byte[] LoadDarkForceEffect(int CharLevel, int skillLevel, boolean Load) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(1320006);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                oPacket.writeBoolean(Load);
                return oPacket.getPacket();
            }

            public static byte[] CreateDragonEffect(int CharLevel, int skillLevel, boolean Create) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(22160000);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                oPacket.writeBoolean(Create);
                return oPacket.getPacket();
            }

            public static byte[] HookingChainAnimation(int CharLevel, int skillLevel, byte Left, int MobID) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(4341002 + 3);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                oPacket.write(Left);
                oPacket.writeInt(MobID);
                return oPacket.getPacket();
            }

            public static byte[] LoadSwallowingEffect(int CharLevel, int skillLevel) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(33101005);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                return oPacket.getPacket();
            }

            public static byte[] RemoveSwallowingEffect(int CharLevel, int skillLevel) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(33101006);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                return oPacket.getPacket();
            }

            public static byte[] ShowEffectRocketBooster(int CharLevel, int skillLevel) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(35101004);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                return oPacket.getPacket();
            }

            public static byte[] ShowEffectSiege(int CharLevel, int skillLevel) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(35111004);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                return oPacket.getPacket();
            }

            public static byte[] ShowEffectSiege_MissileTank(int CharLevel, int skillLevel) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(35121005);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                return oPacket.getPacket();
            }

            public static byte[] ShowEffectSiegeStance(int CharLevel, int skillLevel) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(35121013);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                return oPacket.getPacket();
            }

            public static byte[] ShowEffectFlameThrower(int CharLevel, int skillLevel) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(35001001);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                return oPacket.getPacket();
            }

            public static byte[] ShowEffectFlameThrowerEnhanced(int CharLevel, int skillLevel) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(35101009);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                return oPacket.getPacket();
            }

            public static byte[] LoadMoreWildEffect(int CharLevel, int skillLevel) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(33121006);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                return oPacket.getPacket();
            }

            public static byte[] LoadMoreWildEffect(int CharLevel, int skillLevel, byte Left, short X, short Y) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(30001062);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                oPacket.write(Left);
                oPacket.writeShort(X);
                oPacket.writeShort(Y);
                return oPacket.getPacket();
            }

            public static byte[] showUnregistedSkill(int skillID, int CharLevel, int skillLevel, byte Left) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(skillID);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                if (skillID != 30001061 && GameConstants.is_unregisterd_skill(skillID)) {
                    oPacket.write(Left);
                }
                return oPacket.getPacket();
            }

            public static byte[] showCaptureEffect(int skillID, int CharLevel, int skillLevel, byte type) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(skillID);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                oPacket.write(type);
                // type = 0: Monster successfully captured.
                // type = 1; Capture failed. Monster HP too high.
                // type = 2; Monster cannot be captured.
                return oPacket.getPacket();
            }
        }

        public static byte[] SkillAffected(int skillID, int skillLevel) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.SkillAffected);
            oPacket.writeInt(skillID);
            oPacket.write(skillLevel);
            return oPacket.getPacket();
        }

        public static byte[] SkillAffectedSelect(int Select, int skillID, int skillLevel) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.SkillAffected);
            oPacket.writeInt(Select);
            oPacket.writeInt(skillID);
            oPacket.write(skillLevel);
            return oPacket.getPacket();
        }

        public static byte[] SkillSpecialAffected(int skillID, int skillLevel) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.SkillAffected);
            oPacket.writeInt(skillID);
            oPacket.write(skillLevel);
            return oPacket.getPacket();
        }

        public static byte[] Quest(Map<Integer, Integer> questList, String text, int effect) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.Quest);
            oPacket.write(questList.size());
            for (Entry<Integer, Integer> quest : questList.entrySet()) {
                oPacket.writeInt(quest.getKey()); // Item ID
                oPacket.writeInt(quest.getValue()); // Quantity
            }
            oPacket.writeMapleAsciiString(text); // Không chắc
            oPacket.writeInt(effect); // effect >= 0
            return oPacket.getPacket();
        }

        public static byte[] Quest(int itemID, int quantity) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.Quest);
            oPacket.write(1);
            oPacket.writeInt(itemID); // Item ID
            oPacket.writeInt(quantity); // Quantity
            oPacket.writeMapleAsciiString("Nice item Bruh!!");
            oPacket.writeInt(0);
            return oPacket.getPacket();
        }

        public static byte[] Pet(int type, byte petIndex) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.Pet);
            oPacket.write(type);
            // 0 = LevelUp; 1 = Teleport; 2 = Warp; 3 = Evolution.
            oPacket.write(petIndex);
            return oPacket.getPacket();
        }

        public static byte[] SkillSpecial(int skillID, int timeBombX, int timeBombY, int skillLevel) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.SkillSpecial);
            oPacket.writeInt(skillID);
            if (skillID == 4341002) { // BattleMage.DARK_SHOCK
                oPacket.writeInt(timeBombX);
                oPacket.writeInt(timeBombY);
                oPacket.writeInt(skillLevel);
            }
            return oPacket.getPacket();
        }

        public static byte[] ProtectOnDieItemUse(boolean useSafetyCharm, byte days, byte times, int itemID) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.ProtectOnDieItemUse);
            oPacket.writeBoolean(useSafetyCharm);
            oPacket.write(days);
            oPacket.write(times);
            if (!useSafetyCharm) {
                // The EXP did not drop after using %s item.
                oPacket.writeInt(itemID);
            } else {
                // The EXP did not drop after using the Safety Charm once.(%d days/%d times left)
            }
            return oPacket.getPacket();
        }

        public static byte[] PlayPortalSE() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.PlayPortalSE);
            return oPacket.getPacket();
        }

        public static byte[] JobChanged() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.JobChanged);
            return oPacket.getPacket();
        }

        public static byte[] QuestComplete() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.QuestComplete);
            return oPacket.getPacket();
        }

        public static byte[] IncDecHPEffect(byte Delta) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.IncDecHPEffect);
            oPacket.write(Delta);
            return oPacket.getPacket();
        }

        public static byte[] BuffItemEffect(int ItemId) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.BuffItemEffect);
            oPacket.writeInt(ItemId);
            return oPacket.getPacket();
        }

        public static byte[] SquibEffect(String path) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.SquibEffect);
            oPacket.writeMapleAsciiString(path);
            // Map/MapHelper.img/weather/squib/squib%d", "CUserPreview::ShowFireCrack"
            return oPacket.getPacket();
        }

        public static byte[] MonsterBookCardGet() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.MonsterBookCardGet);
            // Effect/BasicEff.img/MonsterBook/cardGet
            return oPacket.getPacket();
        }

        public static byte[] LotteryUse(int itemID, boolean showText, String Text) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.LotteryUse);
            oPacket.writeInt(itemID);
            oPacket.writeBoolean(showText);
            if (showText) {
                oPacket.writeMapleAsciiString(Text);
            }
            // Effect/BasicEff.img/MonsterBook/cardGet
            return oPacket.getPacket();
        }

        public static byte[] ItemLevelUp() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.ItemLevelUp);
            // Effect/BasicEff.img/ItemLevelUp
            return oPacket.getPacket();
        }

        public static byte[] ItemMaker(boolean EnchantFailure) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.ItemMaker);
            oPacket.writeInt(EnchantFailure ? 1 : 0);
            // Effect/BasicEff.img/ItemLevelUp
            return oPacket.getPacket();
        }

        public static byte[] ExpItemConsumed(boolean EnchantFailure) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.ExpItemConsumed);
            // Effect/BasicEff.img/IncEXP
            return oPacket.getPacket();
        }

        public static byte[] ShowEffectByPath(String path) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.ShowEffectByPath);
            oPacket.writeMapleAsciiString(path);
            return oPacket.getPacket();
        }

        public static byte[] ConsumeEffect(int effect) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.ConsumeEffect);
            oPacket.writeInt(effect);
            return oPacket.getPacket();
        }

        public static byte[] UpgradeTombItemUse(byte left) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.UpgradeTombItemUse);
            oPacket.write(left);
            // You have used 1 Wheel of Destiny in order to revive at the current map. (%d left)
            return oPacket.getPacket();
        }

        public static byte[] BattlefieldItemUse(String path) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.BattlefieldItemUse);
            oPacket.writeMapleAsciiString(path);
            return oPacket.getPacket();
        }

        public static byte[] AvatarOriented(String path) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.AvatarOriented);
            oPacket.writeMapleAsciiString(path);
            oPacket.writeInt(1);
            // Used for stuff in Effect.wz
            return oPacket.getPacket();
        }

        public static byte[] IncubatorUse(int itemID, String path) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.IncubatorUse);
            oPacket.writeInt(itemID);
            oPacket.writeMapleAsciiString(path);
            return oPacket.getPacket();
        }

        public static byte[] PlaySoundWithMuteBGM(String path) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.PlaySoundWithMuteBGM);
            oPacket.writeMapleAsciiString(path);
            return oPacket.getPacket();
        }

        public static byte[] SpiritStoneUse() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.SpiritStoneUse);
            // You have revived on the current map through the effect of the Spirit Stone.
            return oPacket.getPacket();
        }

        public static byte[] IncDecHPEffect_EX(int Delta) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.IncDecHPEffect_EX);
            oPacket.writeInt(Delta);
            return oPacket.getPacket();
        }

        public static byte[] DeliveryQuestItemUse(int itemID) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.DeliveryQuestItemUse);
            oPacket.writeInt(itemID);
            return oPacket.getPacket();
        }

        public static byte[] RepeatEffectRemove() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.RepeatEffectRemove);
            return oPacket.getPacket();
        }

        public static byte[] EvolRing() {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_LOCAL.getValue());
            oPacket.write(UserEffectType.EvolRing);
            return oPacket.getPacket();
        }
    }

    public static class Remote {

        public static byte[] LevelUp(int dwCharacterID) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.LevelUp);
            return oPacket.getPacket();
        }

        public static class SkillUse {

            public static byte[] encode(int dwCharacterID, int skillID, int CharLevel, int skillLevel) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
                oPacket.writeInt(dwCharacterID);
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(skillID);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                return oPacket.getPacket();
            }

            public static byte[] LoadDarkForceEffect(int dwCharacterID, int CharLevel, int skillLevel, boolean Load) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
                oPacket.writeInt(dwCharacterID);
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(1320006);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                oPacket.writeBoolean(Load);
                return oPacket.getPacket();
            }

            public static byte[] CreateDragonEffect(int dwCharacterID, int CharLevel, int skillLevel, boolean Create) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
                oPacket.writeInt(dwCharacterID);
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(22160000);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                oPacket.writeBoolean(Create);
                return oPacket.getPacket();
            }

            public static byte[] HookingChainAnimation(int dwCharacterID, int CharLevel, int skillLevel, byte Left, int MobID) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
                oPacket.writeInt(dwCharacterID);
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(4341002 + 3);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                oPacket.write(Left);
                oPacket.writeInt(MobID);
                return oPacket.getPacket();
            }

            public static byte[] LoadSwallowingEffect(int dwCharacterID, int CharLevel, int skillLevel) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
                oPacket.writeInt(dwCharacterID);
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(33101005);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                return oPacket.getPacket();
            }

            public static byte[] RemoveSwallowingEffect(int dwCharacterID, int CharLevel, int skillLevel) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
                oPacket.writeInt(dwCharacterID);
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(33101006);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                return oPacket.getPacket();
            }

            public static byte[] ShowEffectRocketBooster(int dwCharacterID, int CharLevel, int skillLevel) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
                oPacket.writeInt(dwCharacterID);
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(35101004);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                return oPacket.getPacket();
            }

            public static byte[] ShowEffectSiege(int dwCharacterID, int CharLevel, int skillLevel) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
                oPacket.writeInt(dwCharacterID);
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(35111004);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                return oPacket.getPacket();
            }

            public static byte[] ShowEffectSiege_MissileTank(int dwCharacterID, int CharLevel, int skillLevel) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
                oPacket.writeInt(dwCharacterID);
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(35121005);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                return oPacket.getPacket();
            }

            public static byte[] ShowEffectSiege3(int dwCharacterID, int CharLevel, int skillLevel) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
                oPacket.writeInt(dwCharacterID);
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(35121013);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                return oPacket.getPacket();
            }

            public static byte[] ShowEffectFlameThrower(int dwCharacterID, int CharLevel, int skillLevel) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
                oPacket.writeInt(dwCharacterID);
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(35001001);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                return oPacket.getPacket();
            }

            public static byte[] ShowEffectFlameThrowerEnhanced(int dwCharacterID, int CharLevel, int skillLevel) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
                oPacket.writeInt(dwCharacterID);
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(35101009);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                return oPacket.getPacket();
            }

            public static byte[] LoadMoreWildEffect(int dwCharacterID, int CharLevel, int skillLevel) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
                oPacket.writeInt(dwCharacterID);
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(33121006);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                return oPacket.getPacket();
            }

            public static byte[] LoadMoreWildEffect(int dwCharacterID, int CharLevel, int skillLevel, byte Left, short X, short Y) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
                oPacket.writeInt(dwCharacterID);
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(30001062);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                oPacket.write(Left);
                oPacket.writeShort(X);
                oPacket.writeShort(Y);
                return oPacket.getPacket();
            }

            public static byte[] showUnregistedSkill(int dwCharacterID, int skillID, int CharLevel, int skillLevel, byte Left) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
                oPacket.writeInt(dwCharacterID);
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(skillID);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                if (skillID != 30001061 && GameConstants.is_unregisterd_skill(skillID)) {
                    oPacket.write(Left);
                }
                return oPacket.getPacket();
            }

            public static byte[] showCaptureEffect(int dwCharacterID, int skillID, int CharLevel, int skillLevel, byte type) {
                final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
                oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
                oPacket.writeInt(dwCharacterID);
                oPacket.write(UserEffectType.SkillUse);
                oPacket.writeInt(skillID);
                oPacket.write(CharLevel);
                oPacket.write(skillLevel);
                oPacket.write(type);
                // type = 0: Monster successfully captured.
                // type = 1; Capture failed. Monster HP too high.
                // type = 2; Monster cannot be captured.
                return oPacket.getPacket();
            }

        }

        public static byte[] SkillAffected(int dwCharacterID, int skillID, int skillLevel) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.SkillAffected);
            oPacket.writeInt(skillID);
            oPacket.write(skillLevel);
            return oPacket.getPacket();
        }

        public static byte[] SkillAffectedSelect(int dwCharacterID, int Select, int skillID, int skillLevel) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.SkillAffected);
            oPacket.writeInt(Select);
            oPacket.writeInt(skillID);
            oPacket.write(skillLevel);
            return oPacket.getPacket();
        }

        public static byte[] SkillSpecialAffected(int dwCharacterID, int skillID, int skillLevel) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.SkillAffected);
            oPacket.writeInt(skillID);
            oPacket.write(skillLevel);
            return oPacket.getPacket();
        }

        public static byte[] Quest(int dwCharacterID, Map<Integer, Integer> questList, String text, int effect) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.Quest);
            oPacket.write(questList.size());
            for (Entry<Integer, Integer> quest : questList.entrySet()) {
                oPacket.writeInt(quest.getKey()); // Item ID
                oPacket.writeInt(quest.getValue()); // Quantity
            }
            oPacket.writeMapleAsciiString(text); // Không chắc
            oPacket.writeInt(effect); // effect >= 0
            return oPacket.getPacket();
        }

        public static byte[] Pet(int dwCharacterID, int type, byte petIndex) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.Pet);
            oPacket.write(type);
            // 0 = LevelUp; 1 = Teleport; 2 = Warp; 3 = Evolution.
            oPacket.write(petIndex);
            return oPacket.getPacket();
        }

        public static byte[] SkillSpecial(int dwCharacterID, int skillID, int timeBombX, int timeBombY, int skillLevel) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.SkillSpecial);
            oPacket.writeInt(skillID);
            if (skillID == BladeMaster.FINAL_CUT) {
                oPacket.writeInt(timeBombX);
                oPacket.writeInt(timeBombY);
                oPacket.writeInt(skillLevel);
            }
            return oPacket.getPacket();
        }

        public static byte[] ProtectOnDieItemUse(int dwCharacterID, boolean useSafetyCharm, byte days, byte times, int itemID) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.ProtectOnDieItemUse);
            oPacket.writeBoolean(useSafetyCharm);
            oPacket.write(days);
            oPacket.write(times);
            if (!useSafetyCharm) {
                // The EXP did not drop after using %s item.
                oPacket.writeInt(itemID);
            }
            // The EXP did not drop after using the Safety Charm once.(%d days/%d times left)
            return oPacket.getPacket();
        }

        public static byte[] PlayPortalSE(int dwCharacterID) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.PlayPortalSE);
            return oPacket.getPacket();
        }

        public static byte[] JobChanged(int dwCharacterID) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.JobChanged);
            return oPacket.getPacket();
        }

        public static byte[] QuestComplete(int dwCharacterID) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.QuestComplete);
            return oPacket.getPacket();
        }

        public static byte[] IncDecHPEffect(int dwCharacterID, byte Delta) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.IncDecHPEffect);
            oPacket.write(Delta);
            return oPacket.getPacket();
        }

        public static byte[] BuffItemEffect(int dwCharacterID, int ItemId) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.BuffItemEffect);
            oPacket.writeInt(ItemId);
            return oPacket.getPacket();
        }

        public static byte[] SquibEffect(int dwCharacterID, String path) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.SquibEffect);
            oPacket.writeMapleAsciiString(path);
            // Map/MapHelper.img/weather/squib/squib%d", "CUserPreview::ShowFireCrack"
            return oPacket.getPacket();
        }

        public static byte[] MonsterBookCardGet(int dwCharacterID) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.MonsterBookCardGet);
            // Effect/BasicEff.img/MonsterBook/cardGet
            return oPacket.getPacket();
        }

        public static byte[] LotteryUse(int dwCharacterID, int itemID, boolean showText, String Text) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.LotteryUse);
            oPacket.writeInt(itemID);
            oPacket.writeBoolean(showText);
            if (showText) {
                oPacket.writeMapleAsciiString(Text);
            }
            // Effect/BasicEff.img/MonsterBook/cardGet
            return oPacket.getPacket();
        }

        public static byte[] ItemLevelUp(int dwCharacterID) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.ItemLevelUp);
            // Effect/BasicEff.img/ItemLevelUp
            return oPacket.getPacket();
        }

        public static byte[] ItemMaker(int dwCharacterID, boolean EnchantFailure) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.ItemMaker);
            oPacket.writeInt(EnchantFailure ? 1 : 0);
            // Effect/BasicEff.img/ItemLevelUp
            return oPacket.getPacket();
        }

        public static byte[] ExpItemConsumed(int dwCharacterID, boolean EnchantFailure) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.ExpItemConsumed);
            // Effect/BasicEff.img/IncEXP
            return oPacket.getPacket();
        }

        public static byte[] ShowEffectByPath(int dwCharacterID, String path) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.ShowEffectByPath);
            oPacket.writeMapleAsciiString(path);
            return oPacket.getPacket();
        }

        public static byte[] ConsumeEffect(int dwCharacterID, int effect) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.ConsumeEffect);
            oPacket.writeInt(effect);
            return oPacket.getPacket();
        }

        public static byte[] UpgradeTombItemUse(int dwCharacterID, byte left) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.UpgradeTombItemUse);
            oPacket.write(left);
            // You have used 1 Wheel of Destiny in order to revive at the current map. (%d left)
            return oPacket.getPacket();
        }

        public static byte[] BattlefieldItemUse(int dwCharacterID, String path) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.BattlefieldItemUse);
            oPacket.writeMapleAsciiString(path);
            return oPacket.getPacket();
        }

        public static byte[] AvatarOriented(int dwCharacterID, String path) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.AvatarOriented);
            oPacket.writeMapleAsciiString(path);
            oPacket.writeInt(1);
            // Used for stuff in Effect.wz
            return oPacket.getPacket();
        }

        public static byte[] IncubatorUse(int dwCharacterID, int itemID, String path) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.IncubatorUse);
            oPacket.writeInt(itemID);
            oPacket.writeMapleAsciiString(path);
            return oPacket.getPacket();
        }

        public static byte[] PlaySoundWithMuteBGM(int dwCharacterID, String path) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.PlaySoundWithMuteBGM);
            oPacket.writeMapleAsciiString(path);
            return oPacket.getPacket();
        }

        public static byte[] SpiritStoneUse(int dwCharacterID) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.SpiritStoneUse);
            // You have revived on the current map through the effect of the Spirit Stone.
            return oPacket.getPacket();
        }

        public static byte[] IncDecHPEffect_EX(int dwCharacterID, int Delta) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.IncDecHPEffect_EX);
            oPacket.writeInt(Delta);
            return oPacket.getPacket();
        }

        public static byte[] DeliveryQuestItemUse(int dwCharacterID, int itemID) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.DeliveryQuestItemUse);
            oPacket.writeInt(itemID);
            return oPacket.getPacket();
        }

        public static byte[] RepeatEffectRemove(int dwCharacterID) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.RepeatEffectRemove);
            return oPacket.getPacket();
        }

        public static byte[] EvolRing(int dwCharacterID) {
            final MaplePacketLittleEndianWriter oPacket = new MaplePacketLittleEndianWriter();
            oPacket.writeShort(SendOpcode.USER_EFFECT_REMOTE.getValue());
            oPacket.writeInt(dwCharacterID);
            oPacket.write(UserEffectType.EvolRing);
            return oPacket.getPacket();
        }
    }
}
