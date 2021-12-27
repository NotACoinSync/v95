var status = 0;
var map = Array(910000000);

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && status == 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            cm.sendSimple("Hello, do you want to trade in 1.2 billion mesos for a #b#v4001168##k? Or do you want to trade in my #b#v4001168##k for 1 billion mesos?\r\n#L1# I would like to trade my #b#v4001168##k for 1 billion mesos!#l\r\n\#L2# I would like to exchange 1.2 billion mesos for a #b#v4001168##k!#l");
        } else if (status == 1) {
            if (selection == 1) {
                if (cm.haveItem(4001168)) {
                    cm.gainMeso(1000000000);
                    cm.gainItem(4001168, -1);
                    cm.sendOk("Thank you for your mesos!");
                } else {
                    cm.sendOk("Sorry, you don't have a #b#v4001168##k!");
                }
                cm.dispose();
            } else if (status == 2) {
            } else if (selection == 2) {
                if (cm.getMeso() >= 1200000000) {
                    cm.gainMeso(-1200000000);
                    cm.gainItem(4001168, 1);
                    cm.sendOk("Thank you for your item!");
                } else {
                    cm.sendOk("Sorry, you don't have enough mesos!");
                }
                cm.dispose();
            } else {
                cm.sendOk("All right. Come back later");
            }
        }
    }
}  