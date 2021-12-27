function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
	cm.sendNext("See You later.");
        cm.dispose();
    } else {
        if (status == 0 && mode == 0) {
		cm.sendNext("See You later.");
		cm.dispose();
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) 
			  cm.sendOk("The medal ranking system is currently unavailable...");
	else if (status == 1) {
                cm.dispose();
        }
    }
}