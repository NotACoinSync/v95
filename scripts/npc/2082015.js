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
            cm.sendSimple("Oh! You found me.\r\nI can give you special powers if you are #e#rspecial!#n#k\r\n\r\n#L0#Am I? I would love to have it!#l\r\n#L1#No, I fine on my own!#l");
        } else if (status == 1) {
            if (selection == 0) {
				if(Check == 0) {
                cm.sendOk("You have been #e#bboosted#k#n\r\nYou have the ability to level up 3 times per level! \r\nBut It will only last #r#euntil level 120!");
				cm.getPlayer().setburning(1);
                cm.dispose();
				}
				if(Check == 1 || cm.getPlayer().getLevel() >= 120) {
				cm.sendOk("Sorry but you are not special! \r\nOr your level is higher than 120!");
				cm.dispose();
				}
            } else if (selection == 1) {
                cm.sendOk("Well then! I am available if you wanted a boost");
                cm.dispose();
        }
    }
}
}