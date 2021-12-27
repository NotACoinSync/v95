package constants;

import client.MapleCharacter;
import client.MapleJob;
import client.Skill;
import constants.skills.*;
import constants.skills.resistance.*;
import java.util.Calendar;

public class GameConstants {

    // Maple Trade System's Constants (ITC)
    public static final int nRegisterFeeMeso = 5000, // mesos to sell
            nCommissionRate = 10, // % added to everything
            nCommissionBase = 100, // amount to add to the current sell amount
            nAuctionDurationMin = 1, // minimum hours to auction an item
            nAuctionDurationMax = 168; // maximum hours to auction an item
    public static final int MAIN_NX_TYPE = 4;
    public static final String MAIN_NX_NAME = "NX";
    public static final int MAX_DAMAGE = 199999;
    public static final int maxAbilityStat = 999;

    public static boolean isWeekend() {
        Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            return true;
        }
        return false;
    }

    public static int getHiddenSkill(final int skill) {
        switch (skill) {
            case Aran.HIDDEN_FULL_DOUBLE:
            case Aran.HIDDEN_FULL_TRIPLE:
                return Aran.FULL_SWING;
            case Aran.HIDDEN_OVER_DOUBLE:
            case Aran.HIDDEN_OVER_TRIPLE:
                return Aran.OVER_SWING;
        }
        return skill;
    }

    public static int get_novice_skill_point(MapleCharacter chr) {
        int nValue = 0;
        nValue += chr.getSkillLevel(1000);
        nValue += chr.getSkillLevel(1001);
        nValue += chr.getSkillLevel(1002);
        nValue += chr.getSkillLevel(10001000);
        nValue += chr.getSkillLevel(10001001);
        nValue += chr.getSkillLevel(10001002);
        nValue += chr.getSkillLevel(20001000);
        nValue += chr.getSkillLevel(20001001);
        nValue += chr.getSkillLevel(20001002);
        nValue += chr.getSkillLevel(20011000);
        nValue += chr.getSkillLevel(20011001);
        nValue += chr.getSkillLevel(20011002);
        int nLevel = chr.getLevel();
        if (nLevel >= 7) {
            nLevel = 7;
        }
        return nLevel + nValue - 1;
    }

    public static boolean hasExtendedSPTable(MapleJob job) {
        return job.getId() / 1000 == 3 || job.getId() / 100 == 22 || job.getId() == 2001;
    }

    public static int getAttackDelay(final int id, final Skill skill) {
        switch (id) { // Assume it's faster(2)
            case Bowmaster.HURRICANE:
            case WindArcher.HURRICANE:
            case Corsair.RAPID_FIRE:
            case Gunslinger.RECOIL_SHOT:
                return 40;
            case NightWalker.TRIPLE_THROW:
            case NightLord.TRIPLE_THROW:
            case 5221007:
                return 570;
            case Aran.DOUBLE_SWING:
            case Aran.TRIPLE_SWING:
                return 300;
            case Aran.BODY_PRESSURE:
                return 0;
            case ChiefBandit.BAND_OF_THIEVES:
                return 660;
            case BlazeWizard.FIRE_PILLAR:
                return 1200;
            case 0:
            case ILWizard.THUNDER_BOLT:
                return 810;
        }
        if (skill != null && skill.getSkillType() == 3) {
            return 0; // final attack
        }
        if (skill != null && skill.getDelay() > 0 && !isNoDelaySkill(id)) {
            return skill.getDelay();
        }
        // TODO delay for final attack, weapon type, swing,stab etc
        return 330; // Default usually
    }

    public static boolean is_unregisterd_skill(int skillId) {
        return skillId / 10000000 == 9;
    }

    public static final int getLinkedAttackSkill(final int id) {
        switch (id) {
            case Mechanic.EnhancedFlameLauncher:
            case Mechanic.EnhancedGatlingGun:
                return Mechanic.HeavyWeaponMastery;
            case Mechanic.MechSiegeMode2:
                return 35120013;
            case 35121011:
                return Mechanic.BotsnTots;
            case Mechanic.Satellite2:
            case Mechanic.Satellite3:
                return Mechanic.Satellite;
            case 35100004:
                return Mechanic.RocketBooster;
            case 35110004:
                return Mechanic.MechSiegeMode;
            case 35120005:
                return Mechanic.MechMissileTank;
            case 35120013:
                return Mechanic.MechSiegeMode2;
            case 35000001:
                return Mechanic.FlameLauncher;
            case 35100009:
                return Mechanic.EnhancedFlameLauncher;
            // Unchecked LinkedSkill
            case 36121013:
            case 36121014:
                return 36121002;
            case 21110015:
            case 21110007:
            case 21110008:
                return 21110002;
            case 21000006:
                return 21000002;
            case 21120015:
            case 21120009:
            case 21120010:
                return 21120002;
            case 4321001:
                return 4321000;
            case 33101008:
                return 33101004;
            case 32001007:
            case 32001008:
            case 32001009:
            case 32001010:
            case 32001011:
                return 32001001;
            case 5300007:
                return 5301001;
            case 5320011:
                return 5321004;
            case 23101007:
                return 23101001;
            case 23111010:
            case 23111009:
                return 23111008;
            case 31001006:
            case 31001007:
            case 31001008:
                return 31000004;
            case 27120211:
                return 27121201;
            case 61001004:
            case 61001005:
            case 61110212:
            case 61120219:
                return 61001000;
            case 61110211:
            case 61120007:
            case 61121217:
                return 61101002;
            case 61111215:
                return 61001101;
            case 61111217:
                return 61101101;
            case 61111216:
                return 61101100;
            case 61111219:
                return 61111101;
            case 61111113:
            case 61111218:
                return 61111100;
            case 61121201:
                return 61121100;
            case 61121203:
                return 61121102;
            case 61110009:
                return 61111003;
            /*
             case 61121217:
             return 61120007;
             */
            case 61121116:
                return 61121104;
            case 61121223:
                return 61121221;
            case 61121221:
                return 61121104;
            case 65101006:
                return 65101106;
            case 65121007:
            case 65121008:
                return 65121101;
            case 61111220:
                return 61121105;
            //case 61120018:
            //      return 61121105;
            case 65111007:
                return 65111100;
            case 4100012:
                return 4100011;
            case 24121010:
                return 24121003;
            case 24111008:
                return 24111006;
            case 5001008:
                return 5001005;
            case 61121053://kaiser hyper
            case 61120008:
                return 61111008;
            case 51100006:
                return 51101006;
            case 31011004:
            case 31011005:
            case 31011006:
            case 31011007:
                return 31011000;
            case 31201007:
            case 31201008:
            case 31201009:
            case 31201010:
                return 31201000;
            case 31211007:
            case 31211008:
            case 31211009:
            case 31211010:
                return 31211000;
            case 31221009:
            case 31221010:
            case 31221011:
            case 31221012:
                return 31221000;
            case 31211011:
                return 31211002;
            case 31221014:
                return 31221001;
            case 25100010:
                return 25100009;
            case 25120115:
                return 25120110;
            case 36101008:
            case 36101009:
                return 36101000;
            case 36111010:
            case 36111009:
                return 36111000;
            case 36121011:
            case 36121012:
                return 36121001;
            case 2121055:
                return 2121052;
            case 11121055:
                return 11121052;
            case 1120017:
                return 1121008;
            case 25000003:
                return 25001002;
            case 25000001:
                return 25001000;
            case 25100001:
                return 25101000;
            case 25110001:
            case 25110002:
            case 25110003:
                return 25111000;
            case 25120001:
            case 25120002:
            case 25120003:
                return 25121000;
            case 95001000:
                return 3111013;
            case 4210014:
                return 4211006;
            case 101000102:
                return 101000101;
            case 14101021:
                return 14101020;
            case 14111021:
                return 14111020;
            case 14111023:
                return 14111022;
            case 14121002:
                return 14121001;
            case 12120011:
                return 12121001;
            case 12120012:
                return 12121003;
            //   case 101000102:
            //          return 101000101;
            case 101000202:
                return 101000201;
            case 101100202:
                return 101100201;
            case 101110201:
                return 101110200;
            case 101110204:
                return 101110203;
            case 101120101:
                return 101120100;
            case 101120103:
                return 101120102;
            case 101120105:
            case 101120106:
                return 101120104;
            case 101120203:
                return 101120202;
            case 101120205:
            case 101120206:
                return 101120204;
            case 101120200:
                return 101121200;
            case 41001005:
            case 41001004:
                return 41001000;
            case 41101009:
            case 41101008:
                return 41101000;
            case 41111012:
            case 41111011:
                return 41111000;
            case 41001000:
                return 41001002;
            // case 41120013:
            case 41001002:
            case 41121012:
            case 41121011:
                return 41121000;
            case 42001006:
            case 42001005:
                return 42001000;
            case 42001007:
                return 42001002;
            case 42100010:
                return 42101001;
            case 33101006:
            case 33101007:
                return 33101005;
            case 35001001:
                return 35101009;
            case 42111011:
                return 42111000;
        }
        return id;
    }

    public static boolean is_able_notweapon_skill(int skillId) {
        return skillId == 4321002;
    }

    public static boolean is_able_to_jumpshoot(int skillId, int jobId) {
        if (skillId > 33101001) {
            if (skillId == 33121001) {
                return true;
            }
            return skillId == 33121009;
        } else {
            if (skillId == 33101001) {
                return true;
            }
            if (skillId != 0) {
                return jobId / 100 == 33;
            }
            return skillId == 33001000;
        }
    }

    public static boolean is_able_to_map_macrosys_skill(int skillId) {
        int v1 = skillId / 1000 % 10;
        return v1 != 0
                && v1 != 9
                && !is_keydown_skill(skillId)
                && skillId != 2101002
                && skillId != 2201002
                && skillId != 2301001
                && skillId != 12101003
                && skillId != 22101001
                && skillId != 4211009
                && skillId != 4111006
                && skillId != 4211006
                && skillId != 11101005
                && skillId != 14101004
                && skillId != 4321003
                && skillId != 33001002
                && skillId != 32001002;
    }

    public static boolean is_able_to_move_during_gauge_skill(int skillId) {
        if (skillId > 5201002) {
            if (skillId > 22121000) {
                if (skillId == 22151001) {
                    return true;
                }
                return skillId == 33121009;
            } else {
                if (skillId == 22121000 || skillId == 14111006) {
                    return true;
                }
                return skillId == 15101003;
            }
        } else {
            if (skillId == 5201002) {
                return true;
            }
            if (skillId > 2321001) {
                if (skillId == 3221001) {
                    return true;
                }
                return skillId == 5101004;
            } else {
                if (skillId == 2321001 || skillId == 2121001) {
                    return true;
                }
                return skillId == 2221001;
            }
        }
    }

    public static boolean is_acc_upgrade_item(int itemId) {
        return itemId / 100 == 20492;
    }

    public static boolean is_active_skill(int skillId) {
        return skillId / 1000 % 10 != 9;
    }

    public static boolean is_admin_job(int jobId) {
        return jobId % 1000 / 100 == 9;
    }

    public static boolean is_adventure_ring_item(int itemId) {
        return itemId == 1112427
                || itemId == 1112428
                || itemId == 1112429
                || itemId == 1112405
                || itemId == 1112445;
    }

    public static boolean is_alert_back_action(int action) {
        return action == 64 || action == 65;
    }

    public static boolean is_antirepeat_buff_skill(int jobId) {
        return jobId / 100 == 33;
    }

    public static boolean is_aran_job(int jobId) {
        return jobId / 100 == 21 || jobId == 2000;
    }

    public static boolean is_attack_area_set_by_data(int skillId) {
        if (skillId > 13111000) {
            if (skillId > 35001001) {
                if (skillId == 35101009 || skillId == 35111015) {
                    return true;
                }
                return skillId == 35121012;
            }
            if (skillId != 35001001) {
                if (skillId > 33101002) {
                    return skillId == 33121001;
                } else {
                    if (skillId == 33101002 || skillId == 14101006) {
                        return true;
                    }
                    return skillId == 21120006;
                }
            }
            return true;
        }
        if (skillId == 13111000) {
            return true;
        }
        if (skillId > 5201001) {
            if (skillId > 5221008) {
                return skillId == 13101005;
            }
            return !(skillId != 5221008 && (skillId < 5211004 || skillId > 5211005));
        }
        if (skillId == 5201001) {
            return true;
        }
        if (skillId > 3201003) {
            return skillId == 3211004;
        } else {
            if (skillId == 3201003 || skillId == 3101003) {
                return true;
            }
            return skillId == 3111004;
        }
    }

    public static boolean is_back_action(int action, int morphed) {
        if (morphed == 1) {
            if (action != 9 && action != 10) {
                return false;
            }
        } else if (action != 45 && action != 46 && action != 130 && action != 129) {
            return false;
        }
        return true;
    }

    public static boolean is_beginner_job(int jobId) {
        return !(jobId % 1000 == 0) || jobId == 2001;
    }

    public static boolean is_black_upgrade_item(int itemId) {
        return itemId / 100 == 20491;
    }

    public static boolean is_blade(int itemId) {
        return itemId / 10000 == 134;
    }

    public static boolean is_bmage_aura_skill(int skillId) {
        if (skillId > 32110000) {
            if (skillId < 32120000 || skillId > 32120001) {
                return false;
            }
        } else if (skillId != 32110000 && skillId != 32001003 && (skillId <= 32101001 || skillId > 32101003)) {
            return false;
        }
        return true;
    }

    public static boolean is_bmage_job(int jobId) {
        return jobId / 100 == 32;
    }

    public static boolean is_book_item(int itemId) {
        return itemId / 10000 == 416;
    }

    public static boolean is_bridle_item(int itemId) {
        return itemId / 10000 == 227;
    }

    public static boolean is_cash_morph_item(int itemId) {
        return itemId / 10000 == 530;
    }

    public static boolean is_cash_package_item(int itemId) {
        return itemId / 10000 == 910;
    }

    public static boolean is_cash_pet_food_item(int itemId) {
        return itemId / 10000 == 524;
    }

    public static boolean is_changemaplepoint_item(int itemId) {
        return itemId == 5200009 || itemId == 5200010;
    }

    public static boolean is_friendship_equip_item(int itemId) {
        return itemId / 100 == 11128 && itemId % 10 <= 2;
    }

    public static boolean is_charslot_inc_item(int itemId) {
        return itemId / 1000 == 5430;
    }

    public static boolean is_character_sale_item(int itemId) {
        return itemId == 5431000 || itemId == 5432000;
    }

    public static boolean is_equipslot_ext_item(int itemId) {
        return itemId / 10000 == 555;
    }

    public static boolean is_slot_inc_item(int itemId) {
        return itemId / 10000 == 911
                || itemId / 1000 == 5430
                || itemId == 5431000
                || itemId == 5432000;
    }

    public static boolean is_command_skill(int skillId) {
        if (skillId <= 21110008) {
            if (skillId >= 21110006) {
                return true;
            }
            if (skillId > 21100002) {
                return !(skillId > 21110004 || skillId < 21110003 && (skillId < 21100004 || skillId > 21100005));
            }
            if (skillId >= 21100001) {
                return true;
            }
            if (skillId > 20000016) {
                return skillId == 21000002;
            } else {
                if (skillId >= 20000014) {
                    return true;
                }
                return skillId == 4331005;
            }
        }
        if (skillId > 32101000) {
            if (skillId == 32111002) {
                return true;
            }
            return skillId == 32121002;
        }
        if (skillId == 32101000) {
            return true;
        }
        if (skillId > 21120010) {
            if (skillId >= 32001000) {
                return skillId <= 32001001;
            }
        } else {
            if (skillId >= 21120009) {
                return true;
            }
            if (skillId >= 21120005) {
                return skillId <= 21120007;
            }
        }
        return false;
    }

    public static boolean is_correct_job_for_skill_root(int jobId, int skillRoot) {
        if (!(skillRoot % 100 == 0)) {
            return skillRoot / 100 == jobId / 100;
        }
        return !(skillRoot / 10 != jobId / 10 || jobId % 10 < skillRoot % 10);
    }

    public static boolean is_cygnus_job(int jobId) {
        return jobId / 1000 == 1;
    }

    public static boolean is_delayed_hit_sfx_needed_skill(int skillId) {
        return skillId == 3211003;
    }

    public static boolean is_dual_add_damage_except_skill(int skillId) {
        return skillId >= 4341002 && skillId <= 4341004;
    }

    public static boolean is_dual_job(int jobId) {
        return jobId / 10 == 43;
    }

    public static boolean is_durability_upgrade_item(int itemId) {
        return itemId / 1000 == 2047;
    }

    public static boolean is_engagement_ring_box_item(int itemId) {
        return itemId / 10000 == 224;
    }

    public static boolean is_evan_job(int jobId) {
        return jobId / 100 == 22 || jobId == 2001;
    }

    public static boolean is_exp_up_item(int itemId) {
        return itemId / 10000 == 237;
    }

    public static boolean is_extendsp_job(int jobId) {
        return jobId / 1000 == 3 || jobId / 100 == 22 || jobId == 2001;
    }

    public static boolean is_gachapon_box_item(int itemId) {
        return itemId / 10000 == 428;
    }

    public static boolean is_ignore_master_level_for_common(int skillId) {
        if (skillId > 3220010) {
            if (skillId <= 5220012) {
                if (skillId == 5220012 || skillId == 4120010 || skillId == 4220009) {
                    return true;
                }
                return skillId == 5120011;
            }
            if (skillId != 32120009) {
                return skillId == 33120010;
            }
            return true;
        }
        if (skillId >= 3220009) {
            return true;
        }
        if (skillId > 2120009) {
            if (skillId > 2320010) {
                if (skillId < 3120010 || skillId > 3120011) {
                    return false;
                }
            } else if (skillId != 2320010) {
                return skillId == 2220009;
            }
            return true;
        }
        if (skillId == 2120009 || skillId == 1120012 || skillId == 1220013) {
            return true;
        }
        return skillId == 1320011;
    }

    public static boolean is_invitation_bundle_item(int itemId) {
        return itemId == 4031377 || itemId == 4031395;
    }

    public static boolean is_invitation_guest_item(int itemId) {
        return itemId == 4031406 || itemId == 4031407;
    }

    public static boolean is_jaguar_melee_attack_skill(int skillId) {
        if (skillId > 33111002) {
            if (skillId == 33111006) {
                return true;
            }
            return skillId == 33121002;
        } else {
            if (skillId == 33111002 || skillId == 33101002) {
                return true;
            }
            return skillId == 33101007;
        }
    }

    public static boolean is_jobchange_level_in_evan(int level) {
        switch (level) {
            case 10:
            case 20:
            case 30:
            case 40:
            case 50:
            case 60:
            case 80:
            case 100:
            case 120:
            case 160:
                return true;
            default:
                return false;
        }
    }

    public static boolean is_mage_job(int jobId) {
        switch (jobId / 100) {
            case 2:
            case 12:
            case 22:
            case 32:
                return true;
            default:
                return false;
        }
    }

    public static boolean is_manager_job(int jobId) {
        return jobId % 1000 / 100 == 8;
    }

    public static boolean is_maptransfer_item(int itemId) {
        return itemId / 10000 == 232;
    }

    public static boolean is_masterybook_item(int itemId) {
        return itemId / 10000 == 229
                || itemId / 10000 == 562 && (itemId == 5620006 || itemId != 5620007 && itemId != 5620008)
                || itemId == 5620006
                || itemId == 5620007
                || itemId == 5620008;
    }

    public static boolean is_mechanic_job(int jobId) {
        return jobId / 100 == 35;
    }

    public static boolean is_minigame_item(int itemId) {
        return itemId / 10000 == 408;
    }

    public static boolean is_mobsummon_item(int itemId) {
        return itemId / 10000 == 210;
    }

    public static boolean is_new_year_card_item_con(int itemId) {
        return itemId / 10000 == 216;
    }

    public static boolean is_new_year_card_item_etc(int itemId) {
        return itemId / 10000 == 430;
    }

    public static boolean is_non_cash_effect_item(int itemId) {
        return itemId / 10000 == 429;
    }

    public static boolean is_pet_food_item(int itemId) {
        return itemId / 10000 == 212;
    }

    public static boolean is_pigmy_egg(int itemId) {
        return itemId / 10000 == 417;
    }

    public static boolean is_portable_chair_item(int itemId) {
        return itemId / 10000 == 301;
    }

    public static boolean is_portal_scroll_item(int itemId) {
        return itemId / 10000 == 203;
    }

    public static boolean is_pronestab_action(int action) {
        return action == 41 || action == 57;
    }

    public static boolean is_raise_item(int itemId) {
        return itemId / 1000 == 4220;
    }

    public static boolean is_release_item(int itemId) {
        return itemId / 10000 == 246;
    }

    public static boolean is_resistance_job(int jobId) {
        return jobId / 1000 == 3;
    }

    public static boolean is_select_npc_item(int itemId) {
        return itemId / 10000 == 545 || itemId / 10000 == 239;
    }

    public static boolean is_script_run_item(int itemId) {
        return itemId / 10000 == 243 || itemId == 3994225;
    }

    public static boolean is_wildhunter_job(int jobId) {
        return jobId / 100 == 33;
    }

    public static boolean is_third_job(int jobId) {
        return jobId / 1000 == 2;
    }

    public static boolean is_matched_itemid_job(int jobId, int itemId) {
        if (jobId / 100 != 22 && jobId != 2001) {
            if ((itemId - 5050005) <= 4) {
                return false;
            }
        } else if ((itemId - 5050001) <= 3) {
            return false;
        }
        return true;
    }

    public static int get_job_category(int jobId) {
        return jobId % 1000 / 100;
    }

    public static int get_dualjob_change_level(int step) {
        int result;
        switch (step) {
            case 400:
                result = 10;
                break;
            case 430:
                result = 20;
                break;
            case 431:
                result = 30;
                break;
            case 432:
                result = 55;
                break;
            case 433:
                result = 70;
                break;
            case 434:
                result = 120;
                break;
            default:
                result = 200;
                break;
        }
        return result;
    }

    public static boolean isAranSkills(final int skill) {
        return Aran.FULL_SWING == skill || Aran.OVER_SWING == skill || Aran.COMBO_TEMPEST == skill || Aran.COMBO_PENRIL == skill || Aran.COMBO_DRAIN == skill || Aran.HIDDEN_FULL_DOUBLE == skill || Aran.HIDDEN_FULL_TRIPLE == skill || Aran.HIDDEN_OVER_DOUBLE == skill || Aran.HIDDEN_OVER_TRIPLE == skill || Aran.COMBO_SMASH == skill || Aran.DOUBLE_SWING == skill || Aran.TRIPLE_SWING == skill;
    }

    public static boolean isHiddenSkills(final int skill) {
        return BladeSpecialist.TORNADO_SPIN_TWIRL == skill
                || Aran.HIDDEN_FULL_DOUBLE == skill
                || Aran.HIDDEN_FULL_TRIPLE == skill
                || Aran.HIDDEN_OVER_DOUBLE == skill
                || Aran.HIDDEN_OVER_TRIPLE == skill;
    }

    public static boolean isInJobTree(int skillId, int jobId) {
        int skill = skillId / 10000;
        return is_correct_job_for_skill_root(jobId, skill);
    }

    public static boolean isBeginnerSkill(int skillid) {
        // Skills 'taught' to you somehow, not by distribute sp.
        if (skillid >= 1003 && skillid <= 9002) {
            return true;// Explorer
        }
        if (skillid >= 10001003 && skillid <= 10009002) {
            return true;// Knight of Cygnus
        }
        if (skillid >= 20001003 && skillid <= 20009002) {
            return true;// Legend
        }
        if (skillid >= 20011003 && skillid <= 20019002) {
            return true;// Evan
        }
        switch (skillid) {// skills you can put SP in, handle in a separate method?
            case 1000:
            case 1001:
            case 1002:
            case 10001000:
            case 10001001:
            case 10001002:
            case 20001000:
            case 20001001:
            case 20001002:
            case 20011000:
            case 20011001:
            case 20011002:
                return true;
            default:
                return false;
        }
    }

    public static boolean isPqSkill(final int skill) {
        return skill >= 20001013 && skill <= 20000018 || skill % 10000000 == 1020 || skill == 10000013 || skill % 10000000 >= 1009 && skill % 10000000 <= 1011;
    }

    public static boolean bannedBindSkills(final int skill) {
        return isAranSkills(skill) || isPqSkill(skill);
    }

    public static boolean isGMSkills(final int skillId) {
        return skillId >= 9001000 && skillId <= 9101008 || skillId >= 8001000 && skillId <= 8001001;
    }

    public static boolean isDojo(int mapid) {
        return mapid >= 925020100 && mapid <= 925023814;
    }

    public static boolean isPyramid(int mapid) {
        return mapid >= 926010010 & mapid <= 930010000;
    }

    public static boolean isPQSkillMap(int mapid) {
        return isDojo(mapid) || isPyramid(mapid);
    }

    public static boolean isFinisherSkill(int skillId) {
        return skillId > 1111002 && skillId < 1111007 || skillId == 11111002 || skillId == 11111003;
    }

    public static boolean isNoDelaySkill(int skillId) {
        switch (skillId) {
            case Marauder.ENERGY_CHARGE:
            case ThunderBreaker.ENERGY_CHARGE:
                return true;
        }
        return false;
    }

    public static boolean isAffectedByAttackSpeed(int skillId) {
        switch (skillId) {
            case Cleric.Heal:
            case ChiefBandit.BAND_OF_THIEVES:
                return false;
        }
        return true;
    }

    public static boolean isBadMap(int mapid) {
        switch (mapid) {
            case 180000000:
            case 280030000:
            case 240060000:
            case 240060100:
            case 240060200:
            case 551030200:
                return true;
        }
        return false;
    }

    public static boolean is_keydown_skill(int skillId) {
        if (skillId > 13111002) {
            if (skillId > 33101005) {
                if (skillId == 33121009 || skillId == 35001001) {
                    return true;
                }
                return skillId == 35101009;
            } else {
                if (skillId == 33101005) {
                    return true;
                }
                if (skillId > 22121000) {
                    return skillId == 22151001;
                } else {
                    if (skillId == 22121000 || skillId == 14111006) {
                        return true;
                    }
                    return skillId == 15101003;
                }
            }
        }
        if (skillId == 13111002) {
            return true;
        }
        if (skillId <= 3221001) {
            if (skillId == 3221001) {
                return true;
            }
            if (skillId > 2321001) {
                return skillId == 3121004;
            } else {
                if (skillId == 2321001 || skillId == 2121001) {
                    return true;
                }
                return skillId == 2221001;
            }
        }
        if (skillId > 5201002) {
            return skillId == 5221004;
        }
        if (skillId != 5201002) {
            if (skillId < 4341002) {
                return false;
            }
            if (skillId > 4341003) {
                return skillId == 5101004;
            }
        }
        return false;
    }

    public static boolean is_teleport_mastery_skill(int skillId) {
        if (skillId > 2311007) {
            return skillId == 32111010;
        } else {
            if (skillId == 2311007 || skillId == 2111007) {
                return true;
            }
            return skillId == 2211007;
        }
    }

    public static boolean is_shoot_skill_not_consuming_bullet(int skillId) {
        if (is_shoot_skill_not_using_shooting_weapon(skillId)) {
            return true;
        }
        if (skillId > 35001001) {
            if (skillId > 35111015) {
                return skillId == 35121005 || skillId > 35121011 && skillId <= 35121013;
            }
            if (skillId != 35111015) {
                if (skillId <= 35101010) {
                    return skillId >= 35101009 || skillId == 35001004;
                }
                if (skillId != 35111004) {
                    return false;
                }
            }
            return true;
        }
        if (skillId == 35001001) {
            return true;
        }
        if (skillId <= 13101005) {
            return skillId == 13101005 || skillId == 3101003 || skillId == 3201003 || skillId == 4111004;
        }
        return skillId == 14101006 || skillId == 33101002;
    }

    public static boolean is_shoot_skill_not_using_shooting_weapon(int skillId) {
        if (skillId > 15111007) {
            if (skillId > 21120006) {
                return skillId == 33101007;
            } else {
                if (skillId == 21120006 || skillId == 21100004) {
                    return true;
                }
                return skillId == 21110004;
            }
        } else {
            if (skillId >= 15111006) {
                return true;
            }
            if (skillId > 5121002) {
                return skillId == 11101004;
            } else {
                if (skillId == 5121002 || skillId == 4121003) {
                    return true;
                }
                return skillId == 4221003;
            }
        }
    }

    public static boolean is_skill_need_master_level(int skillId) {
        int result;
        if (is_ignore_master_level_for_common(skillId)) {
            return false;
        }
        result = skillId / 10000;
        if (skillId / 10000 / 100 == 22 || result == 2001) {
            return !(JobConstants.getJobIndex(skillId / 10000) != 9
                    && JobConstants.getJobIndex(result) != 10
                    && skillId != 22111001
                    && skillId != 22141002
                    && skillId != 22140000);
        }
        if (result / 10 == 43) {
            return !(JobConstants.getJobIndex(skillId / 10000) != 4
                    && skillId != 4311003
                    && skillId != 4321000
                    && skillId != 4331002
                    && skillId != 4331005);
        }
        if (!(result % 100 != 0)) {
            return false;
        }
        return result % 10 == 2;
    }

    public static double get_damage_adjusted_by_elemAttr(double damage, int attr, double adjust, double boost) {
        switch (attr) {
            case 1: {
                return (1.0 - adjust) * damage;
            }
            case 2: {
                return (1.0 - (adjust * 0.5 + boost)) * damage;
            }
            case 3: {
                double result = (adjust * 0.5 + boost + 1.0) * damage;
                if (damage >= result) {
                    result = damage;
                }
                if (result >= 999999.0) {
                    result = 999999.0;
                }
                return result;
            }
            default: {
                return damage;
            }
        }
    }

    public static int get_required_combo_count(int skillId) {
        if (skillId > 21110004) {
            if (skillId >= 21120006 && skillId <= 21120007) {
                return 200;
            }
        } else {
            if (skillId == 21110004) {
                return 100;
            }
            if (skillId >= 21100004 && skillId <= 21100005) {
                return 30;
            }
        }
        return 0;
    }
}
