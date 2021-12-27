function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else if (mode == 0) {
        cm.dispose();
    } else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
			Check = cm.getPlayer().getMap().checkDPSMap();
			busy = cm.getPlayer().getMap().setBusy();
            cm.sendSimple("I am #bAgent E#k Player's Damage Specialist How can I help?\r\n\r\n#L0#I want to test my damage!#l\r\n#L1#Nothing, just passing by...#l");
        } else if (status == 1) {
            if (selection == 0) {
				if(cm.DpsMapAvailable() == true) {
				cm.sendOk("Sorry but someone else is testing, Please wait.");
				cm.dispose();
				}
				if(cm.DpsMapAvailable() == false) {
				cm.warp(180000001, 0);
                busy
				cm.getPlayer().saveCharToDB();
				cm.dispose();
				}
			}
            if (selection == 1) {
                cm.sendOk("Have a good day");
                cm.dispose();
        }
		}
	}
}