
var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == 1) {
        status++;
        }
    else {
            status--;
    }
    if(mode == -1) {
        cm.dispose();
    } else {
        if(mode == 0) {
            cm.dispose();
            return;
        }
        if(status == 0) {
            cm.sendSimple("#e#b"+cm.getPlayer().getName()+"#k#n, you must have some business to take care of here, right? We have added express travel, are you okay with that?\r\n#k#L0#Of course I am!#l");
        } else if (status == 1) {
            if (selection == 0) {
                cm.sendSimple("Travel Options: #r\r\n#L1#Boat Ticket (6000 Mesos)#l\r\r\n#L2#Express Route (90,000 Mesos)#l");
            }
        } else if (status == 2) {
            if (selection == 1) {
                if (cm.getMeso() >= 6000 && cm.canHold(4031045)) {
                    cm.gainItem(4031045,1);
                    cm.gainMeso(-6000);
                    cm.dispose();
                } else {
                    cm.sendOk("Are you sure you have enough mesos. If so, check your inventory for space.");
                }
            } else if (selection == 2) {
                if (cm.getMeso() >= 90000) {
                    cm.warp(200000100);
                    cm.gainMeso(-90000);
                    cm.dispose();
                } else {
                    cm.sendOk("Are you sure you have 90,000 mesos#k?");
                    cm.dispose();
                }
            }
        }
    }
}
