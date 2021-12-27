importPackage(Packages.tools); 
importPackage(Packages.server.life);

var status = 0;
var gettext;
var fee;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0) {
			cm.sendOk("Alright.");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			 cm.sendSimple("How can I help?\r\n\r\n#L0#I'd like to redeem VIP#l\r\n#L1#Redeem Beta Reward...#l");
		} else if (status == 1) {
			if (selection == 0) {
			cm.sendGetText("Please Enter a valid VIP #rCode :#k");
			}
			if (selection == 1 && cm.getPlayer().getClient().isBetaPlayer() == 1) {
			cm.getPlayer().setBeta(1);
			cm.sendOk("Rewarded. \r\n Please Relog or change channel to save the data.");
			cm.getPlayer().getClient().setBetaPoint(-1);
			cm.getPlayer().dropMessage(cm.getPlayer().getClient().isBetaPlayer()+"Beta Test");
			cm.dispose();
			}
			if (selection == 1 && cm.getPlayer().getClient().isBetaPlayer() <= 0) {
			cm.sendOk("You are not a BETA player, or you may have used it.");
			cm.dispose();
			}
		} else if (status == 2) {
			gettext = cm.getText();
			getValidity = cm.getCode([cm.getText()]);
			getNX = cm.getNXFromCode([cm.getText()]);
			getDP = cm.getDPFromCode([cm.getText()]);
			fee = cm.getText();
			if (getValidity != null) {
			cm.sendYesNo("The Code #e"+getValidity+"#n is #g#eVALID!#k#n\r\n Would you like to use it on #e#h # #nnow?");
			}
			if (getValidity == null) {
			cm.sendOk("The Code #e" + gettext +" is #g#eINVALID!#k#n\r\nTry again later");
			cm.dispose();
			}			
		} else if (status == 3) {
				setInValid = cm.UpdateUsedCode(cm.getPlayer(),(getValidity));
			if (cm.getPlayer().getClient().getGMLevel() > 2) {
				cm.sendOk("Are you looking for a demotion?");
				cm.dispose();
			} else {
				setInValid
				cm.getPlayer().getCashShop().gainCash(1, getNX);
				cm.getPlayer().getClient().addDP(getDP);
				cm.getPlayer().getClient().changeChannel(1);
				cm.getPlayer().dropMessage("You've gained "+getNX+" NX.");
				cm.getPlayer().dropMessage("You've gained "+getDP+" Donor Points.");
				cm.getPlayer().saveCharToDB();
				cm.getPlayer().getClient().UpdateDP();
				cm.dispose();
			}
		} else if (status == 4) {
			cm.sendOk("Bye")
            cm.dispose();
		}
	}
}