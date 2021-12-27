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
			Check = cm.getPlayer().CheckBurning();
            cm.sendSimple("We can take you to #rNinja Castle#k for a small fee \r\nbut the #rminimum#k link level is 300.#n#k\r\n\r\n#L0#Lets go#l\r\n#L1#Pshhh, no!#l");
        } else if (status == 1) {
            if (selection == 0) {
				if(cm.getPlayer().getLinkedTotal() >= 300) {
                cm.warp(800040000);
				cm.gainMeso(-10000);
                cm.dispose();
				}
				if(cm.getPlayer().getLinkedTotal() < 300) {
				cm.sendOk("Sorry but you are weak!");
				cm.dispose();
				}
            } else if (selection == 1) {
                cm.dispose();
        }
    }
}
}