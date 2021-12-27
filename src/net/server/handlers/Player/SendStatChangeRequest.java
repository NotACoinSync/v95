package net.server.handlers.Player;

import client.*;
import client.autoban.AutobanFactory;
import client.autoban.AutobanManager;
import client.autoban.AutobanManager.UpdateType;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.EquipSlot;
import constants.skills.*;
import net.AbstractMaplePacketHandler;
import server.ItemData;
import server.ItemInformationProvider;
import tools.data.input.SeekableLittleEndianAccessor;

public final class SendStatChangeRequest extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        AutobanManager abm = player.getAutobanManager();
        int timestamp = iPacket.readInt();// TODO: Readd
        abm.setTimestamp(0, timestamp, 3);
        
        int flag = iPacket.readInt();
        int nHP = 0, nMP = 0;
        if ((flag & MapleStat.HP.getValue()) > 0) {
            nHP = iPacket.readShort();
        }
        if ((flag & MapleStat.MP.getValue()) > 0) {
            nMP = iPacket.readShort();
        }
        int nOption = iPacket.readByte();
        double recovery = c.getPlayer().getMap().getMapData().getRecovery();
        if ((nOption & 2) > 0) {
            recovery *= 1.5D;
        }
        Item item = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short) EquipSlot.OVERALL.getSlots()[0]);
        if (item != null) {
            if (recovery > 1.0D) {
                ItemData data = ItemInformationProvider.getInstance().getItemData(item.getItemId());
                if (data.recovery != 1) {
                    recovery *= data.recovery;
                }
            }
        }
        long time = System.currentTimeMillis();
        if (nHP > 0) {
            long hpDuration = 10000;
            if ((nOption & 1) > 0) {
                hpDuration = getEndureDuration(player);
            }
            if ((time - abm.getLastUpdate(UpdateType.CHARACTER_HP_INC)) < hpDuration - 2000) {
                abm.increaseIllegalUpdateType(UpdateType.CHARACTER_HP_INC);
                return;
            }
            double hp = ((getHPRecoveryUpgrade(player) + 10) * recovery);
            // System.out.println("HP: " + hp);
            if (player.getChair() > 100) {
                ItemData chair = ItemInformationProvider.getInstance().getItemData(player.getChair());
                hp += chair.recoveryHP;
            }
            // System.out.println("hp Calculated: " + hp + " given: " + nHP);
            if (hp < nHP) {
                AutobanFactory.HIGH_HP_HEALING.alert(player, "HP: " + nHP + "; Max is " + hp + ".");
                abm.increaseIllegalUpdateType(UpdateType.HP_INC_SIZE);
                return;
            }
            player.addHP((int) Math.round(hp));
        }
        if (nMP > 0) {
            if ((time - abm.getLastUpdate(UpdateType.CHARACTER_MP_INC)) < 8000) {
                abm.increaseIllegalUpdateType(UpdateType.CHARACTER_MP_INC);
                return;
            }
            double mp = ((getMPRecoveryUpgrade(player) + 5) * recovery);
            if (player.getChair() > 100) {
                ItemData chair = ItemInformationProvider.getInstance().getItemData(player.getChair());
                mp += chair.recoveryMP;
            }
            // System.out.println("mp Calculated: " + mp + " given: " + nMP);
            if (mp < nMP) {
                AutobanFactory.HIGH_MP_HEALING.alert(player, "MP: " + nMP + "; Max is " + mp + ".");
                abm.increaseIllegalUpdateType(UpdateType.MP_INC_SIZE);
                return;
            }
            player.addMP((int) Math.round(mp));
        }
    }

    public long getEndureDuration(MapleCharacter chr) {
        Skill skill = SkillFactory.getSkill(4100002);// thief
        int skillLevel = chr.getSkillLevel(skill);
        if (skill != null && skillLevel > 0) {
            return skill.getEffect(skillLevel).getDuration();
        }
        skill = SkillFactory.getSkill(4200001);
        skillLevel = chr.getSkillLevel(skill);
        if (skill != null && skillLevel > 0) {
            return skill.getEffect(skillLevel).getDuration();
        }
        skill = SkillFactory.getSkill(4310000);// endure(dual blade)
        skillLevel = chr.getSkillLevel(skill);
        if (skill != null && skillLevel > 0) {
            return skill.getEffect(skillLevel).getDuration();
        }
        return 0;
    }

    public int getHPRecoveryUpgrade(MapleCharacter chr) {
        Skill skill = SkillFactory.getSkill(1000000);// warrior
        int skillLevel = chr.getSkillLevel(skill);
        if (skill != null && skillLevel > 0) {
            return skill.getEffect(skillLevel).getHp();
        }
        skill = SkillFactory.getSkill(4100002);// thief
        skillLevel = chr.getSkillLevel(skill);
        if (skill != null && skillLevel > 0) {
            return skill.getEffect(skillLevel).getHp();
        }
        skill = SkillFactory.getSkill(4200001);
        skillLevel = chr.getSkillLevel(skill);
        if (skill != null && skillLevel > 0) {
            return skill.getEffect(skillLevel).getHp();
        }
        skill = SkillFactory.getSkill(4310000);// endure(dual blade)
        skillLevel = chr.getSkillLevel(skill);
        if (skill != null && skillLevel > 0) {
            return skill.getEffect(skillLevel).getHp();
        }
        return 0;
    }

    public int getMPRecoveryUpgrade(MapleCharacter chr) {
        if (!chr.getJob().isMagician()) {
            Skill skill = SkillFactory.getSkill(DawnWarrior.SWORD_MASTERY);
            int skillLevel = chr.getSkillLevel(skill);
            if (skill != null && skillLevel > 0) {
                return skill.getEffect(skillLevel).getMp();
            }
            skill = SkillFactory.getSkill(Assassin.ENDURE);//
            skillLevel = chr.getSkillLevel(skill);
            if (skill != null && skillLevel > 0) {
                return skill.getEffect(skillLevel).getMp();
            }
            skill = SkillFactory.getSkill(Bandit.ENDURE);
            skillLevel = chr.getSkillLevel(skill);
            if (skill != null && skillLevel > 0) {
                return skill.getEffect(skillLevel).getMp();
            }
            skill = SkillFactory.getSkill(WhiteKnight.IMPROVING_MP_RECOVERY);//
            skillLevel = chr.getSkillLevel(skill);
            if (skill != null && skillLevel > 0) {
                return skill.getEffect(skillLevel).getMp();
            }
            return 0;
        }
        Skill skill = SkillFactory.getSkill(Magician.IMPROVED_MP_RECOVERY);
        double skillLevel = chr.getSkillLevel(skill);
        if (skillLevel > 0) {
            return (int) Math.round(skillLevel * chr.getLevel() * 0.1);
        }
        return 0;
    }
}
