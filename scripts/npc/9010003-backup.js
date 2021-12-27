importPackage(Packages.tools); 
importPackage(Packages.server.life);

var status = 0;
var gettext;
var fee;
var namechange = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
		if (mode == 0) {
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			cm.sendSimple("Oh Hey #e#b"+cm.getPlayer().getName()+"#k#n,\r\nYou have #e"+cm.getPlayer().getRewardPoints()+" #rRP#k #nWhat would you like to purchase?\r\n#k#L1#Cash Items\r\n#L0#Pets#l\r\n#L3#Surprise Box#l");
		} else if (status == 1) {
			if (selection == 0) {
			cm.sendSimple("#eCurrent Pet Options :- #r\r\n#L0#Buy V.I.P Membership for this Character (15 RP) #b\r\n\#L3#Buy 2nd Burning Slot for this Character (30 RP) #b\r\n\#L2#Change Character's Name (30 RP)\r\n#b#L1#Change Gender (15 RP)#l\r\n#L4#Pet Never Gets Hungry. (50 RP)");
			}
			if (selection == 1) {
			cm.sendSimple("The available Cash Items are : \r\n#e#L100##i5150040#Randomized Royal Hair Coupon. (5 RP)\r\n#L102##i1092064#Transparent Shield (25 RP)\r\n#L103##i1702565#Death's Scythe (30 RP)");
			}
			if (selection == 3) {
			cm.sendSimple("Cash Surprise Boxes: \r\n\r\n #b#e#L200#1#i5222000# for 2 Reward Point\r\n#L201#5#i5222000# for 10 Reward Points\r\n#L203#15#i5222000# for 30 Reward Points");
			}
		} else if (status == 2) {
			//----------------------------------------------------------------------------------------------------->RP npc<-----------------------------------------------------------------------------------------//
			//Royal Hair Coupon
			if (selection == 100) {
				if (cm.getPlayer().getRewardPoints() >= 5) {	
			cm.getPlayer().getClient().addRewardPoints(-5);
			cm.getPlayer().dropMessage("You have lost -5 RP");
			cm.gainItem(5150040, 1);
			cm.dispose();
				} else {
					cm.sendOk("You do not have enough #rRP");
				}
			//Transparent Shield	
			}else if (selection == 102) {
				if (cm.getPlayer().getRewardPoints() >= 25) {	
			cm.getPlayer().getClient().addRewardPoints(-25);
			cm.getPlayer().dropMessage("You have lost -25 RP");
			cm.gainItem(1092064, 1);
			cm.dispose();
				}
				else {
					cm.sendOk("You do not have enough #rRP");
				}	
				
			//Death's Scythe	
			}else if (selection == 103) {
				if (cm.getPlayer().getRewardPoints() >= 30) {	
			cm.getPlayer().getClient().addRewardPoints(-30);
			cm.getPlayer().dropMessage("You have lost -30 RP");
			cm.gainItem(1702565, 1);
			cm.dispose();
				}
				else {
					cm.sendOk("You do not have enough #rRP");
				}
			//Cash Surprise Boxes
			}else if (selection == 200) {
			if (cm.getPlayer().getRewardPoints() >= 2) {	
			cm.getPlayer().getClient().addRewardPoints(-2);
			cm.getPlayer().dropMessage("You have lost -2 RP");
			cm.gainItem(5222000, 1);
			cm.dispose();
				}
				else {
					cm.sendOk("You do not have enough #rRP");
				}	
			//5 Boxes
			}else if (selection == 201) {
				if (cm.getPlayer().getRewardPoints() >= 10) {	
			cm.getPlayer().getClient().addRewardPoints(-10);
			cm.getPlayer().dropMessage("You have lost -10 RP");
			cm.gainItem(5222000, 5);
			cm.dispose();
				} else {
					cm.sendOk("You do not have enough #rRP");
				}
			//15 Boxes
			}else if (selection == 202) {
			if (cm.getPlayer().getRewardPoints() >= 30) {	
			cm.getPlayer().getClient().addRewardPoints(-30);
			cm.getPlayer().dropMessage("You have lost -30 RP");
			cm.gainItem(5222000, 15);
			cm.dispose();
				} else {
					cm.sendOk("You do not have enough #rRP");
				}	

			//--------------------------------------------------------------------------------------------------->Character Options<-------------------------------------------------------------------------------------//	
		//Gender Change
		} else if (selection == 1) {
			if (cm.getPlayer().getGender() == 1 && cm.getPlayer().getClient().getRewardPoints() >= 15) {	
			cm.getPlayer().setGender(0);
			cm.getPlayer().fakeRelog();
			cm.getPlayer().getClient().addRewardPoints(-15);
			cm.getPlayer().dropMessage("You have lost -15 RP");
			cm.dispose();
			}
			else if (cm.getPlayer().getGender() == 0 && cm.getPlayer().getClient().getRewardPoints() >= 15) {
		    cm.getPlayer().setGender(1);
			cm.getPlayer().fakeRelog();
			cm.getPlayer().getClient().addRewardPoints(-15);
			cm.getPlayer().dropMessage("You have lost -15 RP");
			cm.dispose();
			} else {
				cm.sendOk("You do not have enough #rRP");
				cm.dispose();
					}
			//Name Change
			}else if (selection == 2) {
			if (cm.getPlayer().getClient().getRewardPoints() >= 30) {	
			cm.sendGetText("Type the desired #eIGN#n #r (illegal Names are not allowed)#k.");
			namechange = 1;
			} else {
			cm.sendOk("You do not have enough #rRP");
			cm.dispose();
				}	
			//Ultimate Pet	
			}else if (selection == 4) {
			if (cm.getPlayer().getClient().getRewardPoints() >= 50) {	
			cm.sendOk("Done.");
			cm.getPlayer().setUltimatePet(1);
			cm.getPlayer().getClient().addRewardPoints(-50);
			cm.getPlayer().saveCharToDB();
			cm.getPlayer().dropMessage("You have lost -50 RP");
			cm.dispose();
			} else {
			cm.sendOk("You do not have enough #rRP");
			cm.dispose();
				}
			//Second burning character	
			}else if (selection == 3) {
			if (cm.getPlayer().getClient().getRewardPoints() >= 30 && cm.getPlayer().CheckBurning() == 1 && cm.getPlayer().getLevel() < 119) {	
			cm.getPlayer().setburning(2);
			cm.getPlayer().getClient().addRewardPoints(-30);
			cm.getPlayer().saveCharToDB();
			cm.getPlayer().dropMessage("You have lost -30 RP");			
			cm.getPlayer().dropMessage("Your character now is On burning");
			cm.getPlayer().getClient().changeChannel([cm.getPlayer().getClient().getChannel()]);
			cm.dispose();
			}
			//Burning
			else if (cm.getPlayer().CheckBurning() == 0) {
			cm.sendOk("No Burning Detected on the account,\r\nUse @burning to obtain it for free!\r\nThis service is only available for the second burning slot.");
			cm.dispose();
			}else if (cm.getPlayer().CheckBurning() == 2) {
			cm.sendOk("You have 2 Burning Characters!\r\nI can provide the service for 2nd Character Only!");
			cm.dispose();
			} else {
			cm.sendOk("You do not have enough #rRP");
			cm.dispose();
				}	
			//VIP
			}else if (selection == 0) {
			if (cm.getPlayer().getRewardPoints() >= 15 && cm.getPlayer().isNewVip() == 0 && !cm.getPlayer().isVIP()) {
				cm.getPlayer().setNewVIP(1);
				cm.getPlayer().dropMessage("You have lost -15 RP");
				cm.getPlayer().getClient().changeChannel([cm.getPlayer().getClient().getChannel()]);
				cm.getPlayer().getClient().addRewardPoints(-15);
				cm.dispose();
			}
			else if (cm.getPlayer().isVIP()) {
			cm.sendOk("You are already a #r#eVIP#k!");	
			cm.dispose();
				}
			} else {
			cm.getPlayer().dropMessage(1,"Didnt meet previous condition");
			cm.dispose();
			}
				//<------------------------------------------------------------------------------->End Of Character Options<----------------------------------------------------------------------------------------------------------//
		} else if (status == 3) {
			gettext = cm.getText();
			if (namechange == 1 && cm.getPlayer().canCreateChar([gettext]) && gettext != null) {
			cm.getPlayer().setName([gettext]);
			cm.getPlayer().getClient().changeChannel([cm.getPlayer().getClient().getChannel()]);
			cm.getPlayer().getClient().addRewardPoints(-30);
			} else {
				cm.sendNext(gettext+" is already taken \r\n or may be forbidden");
				cm.dispose();
			}
		} else if (status == 4) {
			cm.sendOk("Bye")
            cm.dispose();
		}
	}
}
