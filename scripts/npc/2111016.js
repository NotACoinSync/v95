var status = 0;
var skills;
var jobs = [112,122,130,212,222,231,312,322,410,421,512,520];
var c_job = 0;
var current_skill = 0;
var p_job = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    jobs = jobs.length > 0 ? jobs: cm.getPlayer().getClient().getSupportedJobsForLinkSkills();  // basic cache
    if (mode == -1) {
        cm.dispose();
        return;
    } else if (mode == 1) {
        status++;
    } else {
        status--;
    }
        
    if (status == 0) {
        var message = jobs.length + " Jobs Available.\r\n\r\n#b";
        cm.sendSimple("Choose #bClass#k to view the available #r#eLinked Skills:#n\r\n\r\n#eWarrior Classes:#n#k\r\n\#L0#Hero#l\r\n#L2#Dark Knight\r\n#L1#Paladin#l\r\n\r\n#e#bMagician Classes:#n#k\r\n#L3#ArchMage(F/P)#l\r\n#L4#ArchMage(I/L)#l\r\n#L5#Bishop#l\r\n\r\n#e#gArcher Classes:\r\n#k#n#L6#BowMaster#l\r\n\#L7#Sniper#l#r\n\r\n\r\n#d#eThief Classes:\r\n#k#n#L8#NightLord#l\r\n#L9#Shadower#l\r\n\r\n#ePirate Classes :\r\n#n#L10#Buccaneer#l\r\n#L11#Corsair#l");
    } else if (status == 1) {
        if(mode == 1) {
            c_job = jobs[selection];
        }
        else {
            c_job = p_job;
        }
        p_job = c_job;
        
        current_skill_level = cm.getPlayer().getClient().getLinkSkillLevelForJob(c_job);
        skillid = cm.getPlayer().getClient().getLinkedSkillIDForJob(c_job);
        current_skill_max_level = skillid > 0 ? cm.getSkillMaxLevel(skillid) : -1;
	if(current_skill_level <= 0) {
	    cm.sendOk("You need to level a character of this class to 150 first in order to use its link skill.");
	    cm.dispose();
        } else if(skillid <= 0) {
            cm.sendOk("This class does not yet support linked skills.");
            cm.dispose();
        } else if (current_skill_max_level < 0) {
            cm.sendOk("Error retrieving skill data for skill ID: " + skillid);
            cm.dispose();
        } else {
            cm.sendNextPrev("#s" + skillid + "# #b#q" + skillid + "##k #r["+current_skill_level+"/"+ current_skill_max_level +"]#k\r\n\r\n" + cm.getSkillDesc(skillid)+"");
        }
    } else if (status == 2) {
        cm.sendSimple("Which key do you want #e#r#q" + skillid + "##n#k on? #b\r\n#L59#F1#L60#F2#L61#F3#L62#F4#L63#F5#L64#F6#L65#F7#L66#F8#L67#F9 \r\n #L68#F10#L87#F11#L88#F12 \r\n#L2#1#L3#2#L4#3#L5#4#L6#5#L7#6#L8#7#L9#8#L10#9#L11#0#L12#-#L13#= \r\n#L16#Q#L17#W#L18#E#L19#R#L20#T#L21#Y#L22#U#L23#I#L24#O#L25#P#L26#[#L27#] \r\n#L30#A#L31#S#L32#D#L33#F#L34#G#L35#H#L36#J#L37#K#L38#L#L39#;#L40#' \r\n#L42#Shift#L44#Z#L45#X#L46#C#L47#V#L48#B#L49#N#L50#M#L51#,#L52#.#L42#Shift \r\n#L29#Ctrl#L56#Alt#L57#SPACE#L56#Alt#L29#Ctrl \r\n#L82#Ins#L71#Hm#L73#Pup#L83#Del#L79#End#L81#Pdn");
    } else if (status == 3) {
        cm.changeKeyBinding(selection, 1, skillid);
        cm.teachSkill(skillid , current_skill_level, 30, -1, true);
        cm.sendOk("#eCongratulations!#k#n\r\nYou have setup #r#q"+skillid+"##k successfully! \r\nCurrent Skill Level is : "+current_skill_level+" \r\n");
        cm.dispose();
    } else {
        cm.sendOk("See you next time then.");
        cm.dispose();
    }
}  