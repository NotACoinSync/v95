var status = 0;
var skillid = 0;
var current_skill_level = 0;
var skill;
var jobs = [];
var c_job = 0;
var current_skill = 0;
var p_job = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    jobs = jobs.length > 0 ? jobs : cm.getPlayer().getClient().getSupportedJobsForLinkSkills();  // basic cache
    if (mode == -1) {
        cm.dispose();
        return;
    } else if (mode == 1) {
        status++;
    } else {
        status--;
    }
        
    if (status == 0) {
        cm.sendYesNo("#r"+cm.getPlayer().getName()+".#k#n\r\n\Do you have any #b#eLinked Skill#n#k You'd like to level up? \r\n\r\n#e#rNote#n#k:In order to level your skill you will need : \r\n1-Intermediate Secret Crystal.#n(#r#eExtremely rare#k#n)\r\n\r\nIt can be obtained from #b#eBosses#k#n and #b#eBossPQ");
    } else if (status == 1) {
	var message = jobs.length + " Jobs Available.\r\n\r\n#b";
        cm.sendSimple("Which #bClass#k would you like to level up its #r#eLinked Skill?#n\r\n\r\n#eWarrior Classes:#n#k\r\n\#L0#Hero#l\r\n#L2#Dark Knight\r\n#L1#Paladin#l\r\n\r\n#e#bMagician Classes:#n#k\r\n#L3#ArchMage(F/P)#l\r\n#L4#ArchMage(I/L)#l\r\n#L5#Bishop#l\r\n\r\n#e#gArcher Classes:\r\n#k#n#L6#BowMaster#l\r\n\#L7#Sniper#l#r\n\r\n\r\n#d#eThief Classes:\r\n#k#n#L8#NightLord#l\r\n#L9#Shadower#l\r\n\r\n#ePirate Classes :\r\n#n#L10#Buccaneer#l\r\n#L11#Corsair#l");
    } else if (status == 2) {
        if(mode == 1) { 
            c_job = jobs[selection];
        } else {
            c_job = p_job;
        }
        p_job = c_job;
        current_skill_level = cm.getPlayer().getClient().getLinkSkillLevelForJob(c_job);
        skillid = cm.getPlayer().getClient().getLinkedSkillIDForJob(c_job);
        current_skill_max_level = skillid >= 0 ? cm.getSkillMaxLevel(skillid) : -1;
	if(current_skill_level <= 0) {
	    cm.sendOk("You need to unlock the skill in order to level it.");
	    cm.dispose();
        } else if(skillid <= 0) {
            cm.sendOk("This class does not yet support linked skills.");
            cm.dispose();
        } else if (current_skill_max_level < 0) {
            cm.sendOk("Error retrieving skill data for skill ID: " + skillid);
            cm.dispose();
        } else if (current_skill_level >= current_skill_max_level ) {
            cm.sendOk("#b#q" + skillid + "#" + " is already the max level of " + current_skill_level);
            cm.dispose();
        } else {
            cm.sendSimple("#s" + skillid + "# #b#q" + skillid + "##k Current Level : #r["+current_skill_level+"/" + current_skill_max_level + "]#k\r\n\r\n" + cm.getSkillDesc(skillid) + "\r\n\r\nWhat would you like to do? \r\n\r\n#L0#I'd like to add +2 Levels to the skill.#l\r\n#L1#No Thanks I am good#l");
        }
    } else if (status == 3 && selection == 0) {
        if (cm.getPlayer().haveItem(4251201) && current_skill_level < current_skill_max_level) {
            cm.getPlayer().getClient().setLinkedSkillForJobID(c_job, Math.min(current_skill_level + 2, current_skill_max_level));
            cm.gainItem(4251201, -1);
            current_skill_level = cm.getPlayer().getClient().getLinkSkillLevelForJob(c_job);
            cm.teachSkill(skillid , current_skill_level, 30, -1, true);
            cm.sendOk("You have Leveled up #r#q"+skillid+"##k successfully! \r\nCurrent Skill Level is : ["+current_skill_level+"/"+current_skill_max_level+"]\r\n");
            cm.dispose();
        }
        else if (!cm.getPlayer().haveItem(4251201)) {
                cm.sendOk("You need #i4251201# In order to level up your skill!");
                cm.dispose();
        }	
    } else if (status == 4 && selection == 1) {
        cm.sendOk("Come back if you want to level up your skills");
        cm.dispose();	
    } else {
        cm.sendOk("See you next time then.");
        cm.dispose();
    }
}  