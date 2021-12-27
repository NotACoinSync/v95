package net.server.handlers.Player;

import client.*;
import client.MapleCharacter.CancelCooldownAction;
import client.player.SecondaryStat;
import constants.GameConstants;
import constants.skills.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.server.channel.handlers.AbstractDealDamageHandler;
import server.MapleStatEffect;
import server.TimerManager;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.userpool.UserRemote;
import tools.packets.PacketHelper;
import tools.packets.WvsContext;

public final class TryDoingMeleeAttack extends AbstractDealDamageHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }
        AttackInfo attack = parseDamage(iPacket, player, false, false, false);
        if (player.getBuffEffect(SecondaryStat.Morph) != null) {
            if (player.getBuffEffect(SecondaryStat.Morph).isMorphWithoutAttack()) {
                // How are they attacking when the client won't let them?
                player.getClient().disconnect(false, false);
                return;
            }
        }
        player.getMap().announce(player, UserRemote.closeRangeAttack(player, attack), false, true);
        int numFinisherOrbs = 0;
        Integer comboBuff = player.getBuffedValue(SecondaryStat.ComboCounter);
        if (GameConstants.isFinisherSkill(attack.skillID)) {
            if (comboBuff != null) {
                numFinisherOrbs = comboBuff - 1;
            }
            player.handleOrbconsume();
        } else if (attack.numAttacked > 0) {
            if (attack.skillID != 1111008 && comboBuff != null) {
                int orbcount = player.getBuffedValue(SecondaryStat.ComboCounter);
                int oid = player.isCygnus() ? DawnWarrior.COMBO : Crusader.COMBO;
                int advcomboid = player.isCygnus() ? DawnWarrior.ADVANCED_COMBO : Hero.ADVANCED_COMBO;
                Skill combo = SkillFactory.getSkill(oid);
                Skill advcombo = SkillFactory.getSkill(advcomboid);
                MapleStatEffect ceffect;
                int advComboSkillLevel = player.getSkillLevel(advcombo);
                if (advComboSkillLevel > 0) {
                    ceffect = advcombo.getEffect(advComboSkillLevel);
                } else {
                    ceffect = combo.getEffect(player.getSkillLevel(combo));
                }
                if (orbcount < ceffect.getX() + 1) {
                    int neworbcount = orbcount + 1;
                    if (advComboSkillLevel > 0 && ceffect.makeChanceResult()) {
                        if (neworbcount <= ceffect.getX()) {
                            neworbcount++;
                        }
                    }
                    int duration = combo.getEffect(player.getSkillLevel(oid)).getDuration();
                    List<Pair<SecondaryStat, BuffDataHolder>> stat = Collections.singletonList(new Pair<>(SecondaryStat.ComboCounter, new BuffDataHolder(0, 0, neworbcount)));
                    player.setBuffedValue(SecondaryStat.ComboCounter, neworbcount);
                    duration -= (int) (System.currentTimeMillis() - player.getBuffedStarttime(SecondaryStat.ComboCounter));
                    c.announce(WvsContext.setTemporaryStat(player, oid, duration, stat));
                    player.getMap().announce(player, UserRemote.setTemporaryStat(player, stat), false);
                }
            } else if (player.getSkillLevel(player.isCygnus() ? SkillFactory.getSkill(15100004) : SkillFactory.getSkill(5110001)) > 0 && (player.getJob().isA(MapleJob.MARAUDER) || player.getJob().isA(MapleJob.THUNDERBREAKER2))) {
                for (int i = 0; i < attack.numAttacked; i++) {
                    player.handleEnergyChargeGain();
                }
            }
        }
        if (attack.numAttacked > 0 && attack.skillID == DragonKnight.SACRIFICE) {
            int totDamageToOneMonster = 0; // sacrifice attacks only 1 mob with 1 attack
            final Iterator<List<Pair<Integer, Boolean>>> dmgIt = attack.allDamage.values().iterator();
            if (dmgIt.hasNext()) {// TODO: Crit boolean?
                totDamageToOneMonster = dmgIt.next().get(0).left;
            }
            int remainingHP = player.getHp() - totDamageToOneMonster * attack.getAttackEffect(player, null).getX() / 100;
            if (remainingHP > 1) {
                player.setHp(remainingHP);
            } else {
                player.setHp(1);
            }
            player.updateSingleStat(MapleStat.HP, player.getHp());
        }
        if (attack.numAttacked > 0 && attack.skillID == 1211002) {
            boolean advcharge_prob = false;
            int advcharge_level = player.getSkillLevel(SkillFactory.getSkill(1220010));
            if (advcharge_level > 0) {
                advcharge_prob = SkillFactory.getSkill(1220010).getEffect(advcharge_level).makeChanceResult();
            }
            if (!advcharge_prob) {
                player.cancelEffectFromSecondaryStat(SecondaryStat.WeaponCharge);
            }
        }
        int attackCount = 1;
        if (attack.skillID != 0) {
            attackCount = attack.getAttackEffect(player, null).getAttackCount();
        }
        if (numFinisherOrbs == 0 && GameConstants.isFinisherSkill(attack.skillID)) {
            return;
        }
        if (attack.skillID > 0) {
            Skill skill = SkillFactory.getSkill(attack.skillID);
            MapleStatEffect effect_ = skill.getEffect(attack.skillLevel);
            if (effect_.getCooldown() > 0 && !player.isGM()) {
                if (player.skillisCooling(attack.skillID)) {
                    return;
                } else {
                    c.announce(PacketHelper.skillCooldown(attack.skillID, effect_.getCooldown()));
                    player.addCooldown(attack.skillID, System.currentTimeMillis(), effect_.getCooldown() * 1000, TimerManager.getInstance().schedule("crdh-cooldown", new CancelCooldownAction(player, attack.skillID), effect_.getCooldown() * 1000));
                }
            }
        }
        boolean darksight = player.getBuffedValue(SecondaryStat.DarkSight) != null;
        if ((player.getSkillLevel(SkillFactory.getSkill(NightWalker.VANISH)) > 0 || player.getSkillLevel(SkillFactory.getSkill(Rogue.DARK_SIGHT)) > 0) && darksight) {// && player.getBuffSource(SecondaryStat.DarkSight) != 9101004
            Skill adsSkill = SkillFactory.getSkill(BladeLord.ADVANCED_DARK_SIGHT);
            int ads = player.getSkillLevel(adsSkill);
            if (ads > 0 && !adsSkill.getEffect(ads).makeChanceResult()) {// hmm
                player.cancelEffectFromSecondaryStat(SecondaryStat.DarkSight);
                player.cancelBuffStats(SecondaryStat.DarkSight);
            } else if (ads <= 0) {
                player.cancelEffectFromSecondaryStat(SecondaryStat.DarkSight);
                player.cancelBuffStats(SecondaryStat.DarkSight);
            }
        }
        applyAttack(attack, player, attackCount);
    }
}
