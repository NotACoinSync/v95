package net.server.handlers.Player;

import client.*;
import client.MapleCharacter.CancelCooldownAction;
import client.player.SecondaryStat;
import net.server.channel.handlers.AbstractDealDamageHandler;
import server.MapleStatEffect;
import server.TimerManager;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.CField.userpool.UserRemote;
import tools.packets.PacketHelper;

public final class TryDoingMagicAttack extends AbstractDealDamageHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor iPacket, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if (player == null) {
            c.disconnect(true, false);
            return;
        }

        AttackInfo attack = parseDamage(iPacket, player, false, true, false);

        if (player.getBuffEffect(SecondaryStat.Morph) != null) {
            if (player.getBuffEffect(SecondaryStat.Morph).isMorphWithoutAttack()) {
                // How are they attacking when the client won't let them?
                player.getClient().disconnect(false, false);
                return;
            }
        }
        byte[] packet = UserRemote.magicAttack(player, attack);
        player.getMap().announce(player, packet, false, true);
        MapleStatEffect effect = attack.getAttackEffect(player, null);
        Skill skill = SkillFactory.getSkill(attack.skillID);
        MapleStatEffect effect_ = skill.getEffect(player.getSkillLevel(skill));
        if (effect_.getCooldown() > 0) {
            if (player.skillisCooling(attack.skillID)) {
                return;
            } else {
                c.announce(PacketHelper.skillCooldown(attack.skillID, effect_.getCooldown()));
                player.addCooldown(attack.skillID, System.currentTimeMillis(), effect_.getCooldown() * 1000, TimerManager.getInstance().schedule("mdh-cancel", new CancelCooldownAction(player, attack.skillID), effect_.getCooldown() * 1000));
            }
        }
        applyAttack(attack, player, effect.getAttackCount());
        Skill eaterSkill = SkillFactory.getSkill((player.getJob().getId() - (player.getJob().getId() % 10)) * 10000);// MP Eater, works with right job
        if (eaterSkill != null) {
            int eaterLevel = player.getSkillLevel(eaterSkill);
            if (eaterLevel > 0) {
                for (Integer singleDamage : attack.allDamage.keySet()) {
                    eaterSkill.getEffect(eaterLevel).applyPassive(player, player.getMap().getMapObject(singleDamage), 0);
                }
            }
        }
    }
}
