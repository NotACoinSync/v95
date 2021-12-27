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
			cm.sendOk("Alright, See you soon!");
			cm.dispose();
			return;
		}
		if (mode == 1)
			status++;
		else
			status--;
		if (status == 0) {
			cm.sendSimple("Oh Hey #e#b"+cm.getPlayer().getName()+"#k#n,\r\nYou have #e"+cm.getPlayer().getClient().getDonor()+" #rDP#k #nWhat would you like to purchase?\r\n#k#L1#Donor Items\r\n#L2#Donor Cosmetics #b(Permenant)#k\r\n#L0#Character Options#l\r\n#L3#Exchange Donor Points#l");
		} else if (status == 1) {
			if (selection == 0) {
			cm.sendSimple("#eCharacter Option :- #r\r\n#L0#Buy V.I.P Membership for this Character (500 DP) #b\r\n\#L3#Buy 2nd Burning Slot for this Character (2500 DP) #b\r\n\#L2#Change Character's Name (3000 DP)\r\n#b#L1#Change Gender (2000 DP)#l\r\n#L4#Pet Never Gets Hungry. (3000 DP)");
			}
			if (selection == 1) {
			cm.sendSimple("The available #bDonor#k items are : \r\n#e#L100##i5150040#Randomized Royal Hair Coupon. (500 DP) \r\n#L101##i5150044#VIP Royal Hair Coupon (1300 DP)\r\n#L104##i4031545#x10 Chair Gachapon Ticket (5500 DP)\r\n#L105##i4031545#x25 Chair Gachapon Ticket (12000 DP)");
			}
			if (selection == 2) {
			cm.sendSimple("The Available #bDonor#k Cosmetics : \r\n#e#L200##i1092064#Permenant Transparent Shield (3000 DP)\r\n#L201##i1102349#Fairy Wing Cape (2000 DP)\r\n#L202##i1102356#Angelic Emerald Cape (2000 DP)\r\n#L203##i1102374#Monkey Cape (2000 DP)\r\n#L204##i1702220#Transparent Weapon (3000 DP)\r\n#L205##i1003778#Fluffy Cat Hood (2000 DP)\r\n#L206##i1003186#Cat Hood(Pink) (2000 DP)\r\n#L207##i1003187#Gray Cat Hood (2000 DP)\r\n#L208##i1003559#Blue Cat Hood (2000 DP)\r\n#L209##i1003560#Yellow Cat Hood (2000 DP)\r\n#L210##i1003221#Pink Polka Dot Bow (2000 DP)\r\n#L211##i1003222#Blue Polka Dot Bow (2000 DP)");
			}
			if (selection == 3) {
			cm.sendSimple("Exchange Options :- #r\r\n#L110#Exchange 1 Red Luck Sack For 500 DP #b\r\n\#L112#Exchange 500 DP for Red Luck Sack \r\n\#L115#Exchange 500 DP for 50,000 NX");
			}
		} else if (status == 2) {
			//----------------------------------------------------------------------------------------------------->Donor Items<-----------------------------------------------------------------------------------------//
			//Royal Hair Coupon
			if (selection == 100) {
				if (cm.getPlayer().getClient().getDonor() >= 500) {	
			cm.getPlayer().getClient().addDP(-500);
			cm.getPlayer().dropMessage("You have lost -500 DP");
			cm.gainItem(5150040, 1);
			cm.dispose();
				} else {
					cm.sendOk("You do not have enough #rDP");
					cm.dispose()
				}
			//VIP Royal Hair Coupon	
			}else if (selection == 101) {
				if (cm.getPlayer().getClient().getDonor() >= 1300) {	
			cm.getPlayer().getClient().addDP(-1300);
			cm.getPlayer().dropMessage("You have lost -1300 DP");
			cm.gainItem(5150044, 1);
			cm.dispose();
				}
				else {
					cm.sendOk("You do not have enough #rDP");
					cm.dispose()
				}
			//Get 500 DP	
			}else if (selection == 110) {
				if (cm.getPlayer().haveItem(3993003)) {	
			cm.getPlayer().getClient().addDP(500);
			cm.getPlayer().dropMessage("You have gained 500 DP");
			cm.gainItem(3993003, -1);
			cm.dispose();
				}
				else {
					cm.sendOk("You do not have Sack");
					cm.dispose()
				}	
			} // 4 Red Luck Sack for 2000 DP. Disabled due to player.haveItem() not working as expected.
			/*
			else if (selection == 111) {
				if (cm.getPlayer().haveItem(3993003, 4)) {	
			cm.getPlayer().getClient().addDP(2000);
			cm.getPlayer().dropMessage("You have gained 2000 DP");
			cm.gainItem(3993003, -4);
			cm.dispose();
				}
				else {
					cm.sendOk("You do not have Sack");
					cm.dispose()
				}
			
			} */
			// 1 Red Luck Sack for 500 DP
			else if (selection == 112) {
			if (cm.getPlayer().getClient().getDonor() >= 500 && cm.canHold(3993003)) {	
			cm.getPlayer().getClient().addDP(-500);
			cm.getPlayer().dropMessage("You have lost -500 DP");
			cm.gainItem(3993003, 1);
			cm.dispose();
				}
				else {
					cm.sendOk("You do not have enough #rDP");
					cm.dispose()
				}	
						
							
				
			// 2000DP for 4 Red Luck Sack. Disabled for consistency.
			} /*
			else if (selection == 113) {
			if (cm.getPlayer().getClient().getDonor() >= 2000 && cm.canHold(3993003)) {	
			cm.getPlayer().getClient().addDP(-2000);
			cm.getPlayer().dropMessage("You have lost -2000 DP");
			cm.gainItem(3993003, 4);
			cm.dispose();
				}
				else {
					cm.sendOk("You do not have enough #rDP");
					cm.dispose()
				}	
				//10Chair Gachapon Ticket
			} */
			//NX Exchange
			else if (selection == 115) {
			if (cm.getPlayer().getClient().getDonor() >= 500) {
			cm.sendOk("You have gained 50,000 NX.");	
			cm.getPlayer().getClient().addDP(-500);
			cm.getPlayer().dropMessage("You have lost -500 DP");
			cm.getPlayer().getCashShop().gainCash(1, 50000);
			cm.dispose();
				}
				else {
					cm.sendOk("You do not have enough #rDP");
				}	
			
			}else if (selection == 104) {
				if (cm.getPlayer().getClient().getDonor() >= 5500) {	
			cm.getPlayer().getClient().addDP(-5500);
			cm.getPlayer().dropMessage("You have lost -5500 DP");
			cm.gainItem(4031545, 10);
			cm.dispose();
				}
				else {
					cm.sendOk("You do not have enough #rDP");
					cm.dispose()
				}	
				//25Chair Gachapon Ticket
			}else if (selection == 105) {
				if (cm.getPlayer().getClient().getDonor() >= 12000) {	
			cm.getPlayer().getClient().addDP(-12000);
			cm.getPlayer().dropMessage("You have lost -12000 DP");
			cm.gainItem(4031545, 25);
			cm.dispose();
			}else {
			cm.sendOk("You do not have enough #rDP");
			}
	
			//Cosmetics Start-----------------------------------------
			//Transparent Shield
			}else if (selection == 200) {
				if (cm.getPlayer().getClient().getDonor() >= 3000) {	
			cm.getPlayer().getClient().addDP(-3000);
			cm.getPlayer().dropMessage("You have lost -3000 DP");
			cm.gainItem(1092064, 1);
			cm.dispose();
				}
				else {
					cm.sendOk("You do not have enough #rDP");
				}	
			//Fairy Wing Cape
			}else if (selection == 201) {
				if (cm.getPlayer().getClient().getDonor() >= 2000) {	
			cm.getPlayer().getClient().addDP(-2000);
			cm.getPlayer().dropMessage("You have lost -2000 DP");
			cm.gainItem(1102349, 1);
			cm.dispose();
				} else {
					cm.sendOk("You do not have enough #rDP");
				}
			//Angelic Emerald Cape
			}else if (selection == 202) {
			if (cm.getPlayer().getClient().getDonor() >= 2000) {	
			cm.getPlayer().getClient().addDP(-2000);
			cm.getPlayer().dropMessage("You have lost -2000 DP");
			cm.gainItem(1102356, 1);
			cm.dispose();
				} else {
					cm.sendOk("You do not have enough #rDP");
				}	
			//Monkey Cape
			}else if (selection == 203) {
			if (cm.getPlayer().getClient().getDonor() >= 2000) {	
			cm.getPlayer().getClient().addDP(-2000);
			cm.getPlayer().dropMessage("You have lost -2000 DP");
			cm.gainItem(1102374, 1);
			cm.dispose();
				} else {
					cm.sendOk("You do not have enough #rDP");
				}	
			//Invisible Weapon
			}else if (selection == 204) {
			if (cm.getPlayer().getClient().getDonor() >= 3000) {	
			cm.getPlayer().getClient().addDP(-3000);
			cm.getPlayer().dropMessage("You have lost -3000 DP");
			cm.gainItem(1702220, 1);
			cm.dispose();
				} else {
					cm.sendOk("You do not have enough #rDP");
				}
			//Fluffy Cat Hood
			}else if (selection == 205) {
			if (cm.getPlayer().getClient().getDonor() >= 2000) {	
			cm.getPlayer().getClient().addDP(-2000);
			cm.getPlayer().dropMessage("You have lost -2000 DP");
			cm.gainItem(1003778, 1);
			cm.dispose();
				} else {
					cm.sendOk("You do not have enough #rDP");
				}
			//Cat Hood (Pink)	
			}else if (selection == 206) {
			if (cm.getPlayer().getClient().getDonor() >= 2000) {	
			cm.getPlayer().getClient().addDP(-2000);
			cm.getPlayer().dropMessage("You have lost -2000 DP");
			cm.gainItem(1003186, 1);
			cm.dispose();
				} else {
					cm.sendOk("You do not have enough #rDP");
				}
			//Gray Cat Hood
			}else if (selection == 207) {
			if (cm.getPlayer().getClient().getDonor() >= 2000) {	
			cm.getPlayer().getClient().addDP(-2000);
			cm.getPlayer().dropMessage("You have lost -2000 DP");
			cm.gainItem(1003187, 1);
			cm.dispose();
				} else {
					cm.sendOk("You do not have enough #rDP");
				}	
			// Blue Cat Hood
			}else if (selection == 208) {
			if (cm.getPlayer().getClient().getDonor() >= 2000) {	
			cm.getPlayer().getClient().addDP(-2000);
			cm.getPlayer().dropMessage("You have lost -2000 DP");
			cm.gainItem(1003559, 1);
			cm.dispose();
				} else {
					cm.sendOk("You do not have enough #rDP");
				}
			// Yellow Cat Hood
			}else if (selection == 209) {
			if (cm.getPlayer().getClient().getDonor() >= 2000) {	
			cm.getPlayer().getClient().addDP(-2000);
			cm.getPlayer().dropMessage("You have lost -2000 DP");
			cm.gainItem(1003560, 1);
			cm.dispose();
				} else {
					cm.sendOk("You do not have enough #rDP");
				}
			// Pink Polka Dot Bow
			}else if (selection == 210) {
			if (cm.getPlayer().getClient().getDonor() >= 2000) {	
			cm.getPlayer().getClient().addDP(-2000);
			cm.getPlayer().dropMessage("You have lost -2000 DP");
			cm.gainItem(1003221, 1);
			cm.dispose();
				} else {
					cm.sendOk("You do not have enough #rDP");
				}
			// Blue Polka Dot Bow
			}else if (selection == 211) {
			if (cm.getPlayer().getClient().getDonor() >= 2000) {	
			cm.getPlayer().getClient().addDP(-2000);
			cm.getPlayer().dropMessage("You have lost -2000 DP");
			cm.gainItem(1003222, 1);
			cm.dispose();
				} else {
					cm.sendOk("You do not have enough #rDP");
				}	
			//--------------------------------------------------------------------------------------------------->Character Options<-------------------------------------------------------------------------------------//	
		//Gender Change
		} else if (selection == 1) {
			if (cm.getPlayer().getGender() == 1 && cm.getPlayer().getClient().getDonor() >= 2000) {	
			cm.getPlayer().setGender(0);
			cm.getPlayer().fakeRelog();
			cm.getPlayer().getClient().addDP(-2000);
			cm.getPlayer().dropMessage("You have lost -2,000 DP");
			cm.dispose();
			}
			else if (cm.getPlayer().getGender() == 0 && cm.getPlayer().getClient().getDonor() >= 2000) {
		    cm.getPlayer().setGender(1);
			cm.getPlayer().fakeRelog();
			cm.getPlayer().getClient().addDP(-2000);
			cm.getPlayer().dropMessage("You have lost -2,000 DP");
			cm.dispose();
			} else {
				cm.sendOk("You do not have enough #rDP");
				cm.dispose();
					}
			//Name Change
			}else if (selection == 2) {
			if (cm.getPlayer().getClient().getDonor() >= 3000) {	
			cm.sendGetText("Type the desired #eIGN#n #r (illegal Names are not allowed)#k.");
			namechange = 1;
			} else {
			cm.sendOk("You do not have enough #rDP");
			cm.dispose();
				}	
			//Ultimate Pet	
			}else if (selection == 4) {
			if (cm.getPlayer().getClient().getDonor() >= 3000) {	
			cm.sendOk("Done.");
			cm.getPlayer().setUltimatePet(1);
			cm.getPlayer().saveCharToDB();
			cm.getPlayer().dropMessage("You have lost -3,000 DP");
			cm.dispose();
			} else {
			cm.sendOk("You do not have enough #rDP");
			cm.dispose();
				}
			//Second burning character	
			}else if (selection == 3) {
			if (cm.getPlayer().getClient().getDonor() >= 2500 && cm.getPlayer().CheckBurning() == 1 && cm.getPlayer().getLevel() < 119) {	
			cm.getPlayer().setburning(2);
			cm.getPlayer().getClient().addDP(-2500);
			cm.getPlayer().saveCharToDB();
			cm.getPlayer().dropMessage("You have lost -2,500 DP");			
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
			cm.sendOk("You do not have enough #rDP");
			cm.dispose();
				}	
			//VIP
			}else if (selection == 0) {
			if (cm.getPlayer().getClient().getDonor() >= 500 && cm.getPlayer().isNewVip() == 0 && !cm.getPlayer().isVIP()) {
				cm.getPlayer().setNewVIP(1);
				cm.getPlayer().dropMessage("You have lost -500 DP");
				cm.getPlayer().getClient().changeChannel([cm.getPlayer().getClient().getChannel()]);
				cm.getPlayer().getClient().addDP(-500);
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
			cm.getPlayer().getClient().addDP(-3000);
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
