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
			cm.sendSimple("Oh Hey #e#b"+cm.getPlayer().getName()+"#k#n,\r\nYou have #e"+cm.getPlayer().getClient().getVotePoints()+" #rVP#k #nWhat would you like to purchase?\r\n#k#L1#Items\r\n#L0#Character Options#l\r\n#L3#Exchange VP for NX#l");
		} else if (status == 1) {
			if (selection == 0) {
			cm.sendSimple("#eCharacter Option :- #r\r\n#L0#Buy V.I.P Membership for this Character (15 VP) #b\r\n\#L3#Buy 2nd Burning Slot for this Character (30 VP) #b\r\n\#L2#Change Character's Name (30 VP)\r\n#b#L1#Change Gender (15 VP)#l\r\n#L4#Pet Never Gets Hungry (50 VP)\r\n#L5#Character Slots(Bera) (6,900 NX)");
			}
			if (selection == 1) {
			cm.sendSimple("The available #VP#k items are : \r\n#e#L100##i5150040#Randomized Royal Hair Coupon. (3 VP)\r\n#L102##i5220000#x20 Gachapon Ticket (9 VP)\r\n#L103##i5220000#x100 Gachapon Tickets (30 VP)");
			}
			if (selection == 3) {
			cm.sendSimple("Here is the exchange rate : \r\n\r\n #b#e#L200#7,500 NX for 1 Vote Point\r\n#L201#75,000 NX for 10 Vote Points\r\n#L203#225,000 NX for 30 Vote Points");
			}
		} else if (status == 2) {
			//----------------------------------------------------------------------------------------------------->VP npc<-----------------------------------------------------------------------------------------//
			//Royal Hair Coupon
			if (selection == 100) {
				if (cm.getPlayer().getClient().getVotePoints() >= 3) {	
			cm.getPlayer().getClient().addVotePoints(-3);
			cm.getPlayer().dropMessage("You have lost -3 VP");
			cm.gainItem(5150040, 1);
			cm.dispose();
				} else {
					cm.sendOk("You do not have enough #rVP");
				}
			//20 Gachapon Tickets	
			}else if (selection == 102) {
				if (cm.getPlayer().getClient().getVotePoints() >= 9) {	
			cm.getPlayer().getClient().addVotePoints(-9);
			cm.getPlayer().dropMessage("You have lost -9 VP");
			cm.gainItem(5220000, 20);
			cm.dispose();
				}
				else {
					cm.sendOk("You do not have enough #rVP");
				}	
				
			//100 Gachapon Tickets	
			}else if (selection == 103) {
				if (cm.getPlayer().getClient().getVotePoints() >= 30) {	
			cm.getPlayer().getClient().addVotePoints(-30);
			cm.getPlayer().dropMessage("You have lost -30 VP");
			cm.gainItem(5220000, 100);
			cm.dispose();
				}
				else {
					cm.sendOk("You do not have enough #rVP");
				}
			//Exchange Rates
			}else if (selection == 200) {
			if (cm.getPlayer().getClient().getVotePoints() >= 1) {	
			cm.getPlayer().getClient().addVotePoints(-1);
			cm.getPlayer().dropMessage("You have lost -1 VP");
			cm.getPlayer().getCashShop().gainCash(1, 7500);
			cm.dispose();
				}
				else {
					cm.sendOk("You do not have enough #rVP");
				}	
			//20k nx
			}else if (selection == 201) {
				if (cm.getPlayer().getClient().getVotePoints() >= 8) {	
			cm.getPlayer().getClient().addVotePoints(-10);
			cm.getPlayer().dropMessage("You have lost -10 VP");
			cm.getPlayer().getCashShop().gainCash(1, 75000);
			cm.dispose();
				} else {
					cm.sendOk("You do not have enough #rVP");
				}
			//60k NX
			}else if (selection == 202) {
			if (cm.getPlayer().getClient().getVotePoints() >= 30) {	
			cm.getPlayer().getClient().addVotePoints(-30);
			cm.getPlayer().dropMessage("You have lost -30 VP");
			cm.getPlayer().getCashShop().gainCash(1, 225000);
			cm.dispose();
				} else {
					cm.sendOk("You do not have enough #rVP");
				}	

			//--------------------------------------------------------------------------------------------------->Character Options<-------------------------------------------------------------------------------------//	
		//Gender Change
		} else if (selection == 1) {
			if (cm.getPlayer().getGender() == 1 && cm.getPlayer().getClient().getVotePoints() >= 15) {	
			cm.getPlayer().setGender(0);
			cm.getPlayer().fakeRelog();
			cm.getPlayer().getClient().addVotePoints(-15);
			cm.getPlayer().dropMessage("You have lost -15 VP");
			cm.dispose();
			}
			else if (cm.getPlayer().getGender() == 0 && cm.getPlayer().getClient().getVotePoints() >= 15) {
		    cm.getPlayer().setGender(1);
			cm.getPlayer().fakeRelog();
			cm.getPlayer().getClient().addVotePoints(-15);
			cm.getPlayer().dropMessage("You have lost -15 VP");
			cm.dispose();
			} else {
				cm.sendOk("You do not have enough #rVP");
				cm.dispose();
					}
			//Name Change
			}else if (selection == 2) {
			if (cm.getPlayer().getClient().getVotePoints() >= 30) {	
			cm.sendGetText("Type the desired #eIGN#n #r (illegal Names are not allowed)#k.");
			namechange = 1;
			} else {
			cm.sendOk("You do not have enough #rVP");
			cm.dispose();
				}	
			//Ultimate Pet	
			}else if (selection == 4) {
			if (cm.getPlayer().getClient().getVotePoints() >= 50) {	
			cm.sendOk("Done.");
			cm.getPlayer().setUltimatePet(1);
			cm.getPlayer().getClient().addVotePoints(-50);
			cm.getPlayer().saveCharToDB();
			cm.getPlayer().dropMessage("You have lost -50 VP");
			cm.dispose();
			} else {
			cm.sendOk("You do not have enough #rVP");
			cm.dispose();
				}
			//Bera Slots
			}else if (selection == 5) {
			if (cm.getPlayer().getCashShop().getCash(1) >= 6900) {
			cm.sendOk("Your character slot has been expanded by 1.");
			cm.getPlayer().getClient().gainCharacterSlot();
			cm.getPlayer().getCashShop().gainCash(1, -6900);
			cm.getPlayer().dropMessage("You have lost 6,900 NX");
			cm.dispose();
			} else {
			cm.sendOk("You do not have enough NX");
			cm.dispose();
			}	
			//Second burning character	
			}else if (selection == 3) {
			if (cm.getPlayer().getClient().getVotePoints() >= 30 && cm.getPlayer().CheckBurning() == 1 && cm.getPlayer().getLevel() < 119) {	
			cm.getPlayer().setburning(2);
			cm.getPlayer().getClient().addVotePoints(-30);
			cm.getPlayer().saveCharToDB();
			cm.getPlayer().dropMessage("You have lost -30 VP");			
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
			cm.sendOk("You do not have enough #rVP");
			cm.dispose();
				}	
			//VIP
			}else if (selection == 0) {
			if (cm.getPlayer().getClient().getVotePoints() >= 15 && cm.getPlayer().isNewVip() == 0 && !cm.getPlayer().isVIP()) {
				cm.getPlayer().setNewVIP(1);
				cm.getPlayer().dropMessage("You have lost -15 VP");
				cm.getPlayer().getClient().changeChannel([cm.getPlayer().getClient().getChannel()]);
				cm.getPlayer().getClient().addVotePoints(-15);
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
			cm.getPlayer().getClient().addVotePoints(-30);
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