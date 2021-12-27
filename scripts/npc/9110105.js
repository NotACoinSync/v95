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
            cm.sendSimple("I can take you to show you the way to #rCastellan Toad#k\r\nbut the #rminimum#k link level is 300.#n#k\r\n\r\n#L0#Lets go#l\r\n#L1#Pshhh, no!#l");
        } else if (status == 1) {
            if (selection == 0) {
				if(cm.getPlayer().getLinkedTotal() >= 300) {
                cm.warp(800040300);
                cm.dispose();
				}
				if(cm.getPlayer().getLinkedTotal() < 300) {
				cm.sendOk("Sorry but you are not strong enough yet!");
				cm.dispose();
				}
            } else if (selection == 1) {
                cm.dispose();
        }
    }
}
}