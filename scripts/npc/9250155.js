function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
	cm.sendNext("Think about it.");
        cm.dispose();
    } else {
        if (status == 0 && mode == 0) {
		cm.sendNext("Fine then.");
		cm.dispose();
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) 
			cm.sendYesNo("Would you like to leave this place?");
		 if (status == 1) 
			cm.sendYesNo("Do you want to go back to New Leaf City?");
	else if (status == 2) {
                cm.warp(600000000, 0);
                cm.dispose();
        }
    }
}