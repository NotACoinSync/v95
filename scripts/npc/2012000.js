
//Modified by Amity

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
        cm.sendSimple("#e#b"+cm.getPlayer().getName()+"#k#n, please select from the two travel options:\r\r\n#L0#Boat Tickets#l\r\r\n#L1#Express Travel#l");
    } else if (status == 1) {
        if (selection == 0) {
            cm.sendSimple("Boat Tickets: \r\r\n#L2#Ellinia (5,000 Mesos)#l\r\r\n#L3#Ludibrium (6,000 Mesos)#l\r\r\n#L4#Leafre (30,000 Mesos)#l\r\r\n#L5#Ariant (6,000 Mesos)#l");
        }
    if (selection == 1) {
        cm.sendSimple("Express Travel Options: \r\r\n#L11#Ellinia (75,000 Mesos)#l\r\r\n#L12#Ludibrium (90,000 Mesos)#l\r\r\n#L13#Leafre (450,000 Mesos)#l\r\r\n#L14#Ariant (90,000 Mesos)#l");
    }
  } else if (status == 2) {
    // Boat Tickets
    if (selection ==2){
        if (cm.getMeso() >= 5000 && cm.canHold(4031047)) {
            cm.gainItem(4031047, 1);
            cm.gainMeso(-5000);
            cm.dispose();
      } else {
            cm.sendOk("Please make sure you have enough mesos and space for the ticket.");
        }
  } else if (selection == 3) {
      if (cm.getMeso() >= 6000 && cm.canHold(4031074)) {
          cm.gainItem(4031074, 1);
          cm.gainMeso(-6000);
          cm.dispose();
      } else {
          cm.sendOk("Please make sure you have enough mesos and space for the ticket.");
      }
  } else if (selection == 4) {
      if (cm.getMeso() >= 30000 && cm.canHold(4031331)) {
          cm.gainItem(4031331, 1);
          cm.gainMeso(-30000);
          cm.dispose();
      } else {
          cm.sendOk("Please make sure you have enough mesos and space for the ticket.");
      }
  } else if (selection == 5) {
      if (cm.getMeso() >= 6000 && cm.canHold(4031576)) {
          cm.gainItem(4031576, 1);
          cm.gainMeso(-6000);
          cm.dispose();
      } else {
          cm.sendOk("Please make sure you have enough mesos and space for the ticket.");
      }
  //Express Travel
  } else if (selection == 11) {
      if (cm.getMeso() >= 75000) {
          cm.warp(101000300);
          cm.gainMeso(-75000);
          cm.dispose();
      } else {
          cm.sendOk("Please make sure you have enough mesos.");
      }
  } else if (selection == 12) {
      if (cm.getMeso() >= 90000) {
          cm.warp(2200000100);
          cm.gainMeso(-90000);
          cm.dispose();
      } else {
          cm.sendOk("Please make sure you have enough mesos.");
      }
  } else if (selection == 13) {
      if (cm.getMeso() >= 450000) {
          cm.warp(240000100);
          cm.gainMeso(-450000);
          cm.dispose();
      } else {
          cm.sendOk("Please make sure you have enough mesos.");
      }
  } else if (selection == 14) {
      if (cm.getMeso() >= 90000) {
          cm.warp(260000100);
          cm.gainMeso(-90000);
          cm.dispose();
      } else {
          cm.sendOk("Please make sure you have enough mesos");
	       }
       }
     }
   }
 }
