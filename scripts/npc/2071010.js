function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (status == 0 && mode == 0) {
            cm.sendNext("Enjoy your trip.");
            cm.dispose();
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0)
            cm.sendSimple("Oh! You found me.\r\n I can give you special powers if you are #e#rspecial!#n#k\r\n\r\n #L0#Am I? I would love to have it!#l#\r\n#L1#No, I fine on my own!");
        if (status == 1 && selection == 0)
            cm.sendYesNo("You Chose the power?");
        if (status == 1 && selection == 1)
            cm.sendYesNo("Well then! Lets see how far you can get!");
        if (status == 3 && selection == 0)
            cm.sendYesNo("bye?!");
        else if (status == 4) {
            cm.warp(104000000, 0);
            cm.dispose();
        }
    }
}