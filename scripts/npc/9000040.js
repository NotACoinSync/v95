importPackage(Packages.client.processor);
importPackage(Packages.constants);

var status;
var mergeFee = 50000;
var name;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) {
        cm.dispose();
    } else {
        if (mode == 0 && type > 0) {
            cm.dispose();
            return;
        }
        if (mode == 1)
            status++;
        else
            status--;

        if (status == 0) {
            if (Packages.constants.ServerConstants.USE_ENABLE_CUSTOM_NPC_SCRIPT) {
                cm.sendOk("The medal ranking system is currently unavailable...");
                cm.dispose();
                return;
            }
        }
    }
}
