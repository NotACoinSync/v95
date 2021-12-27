/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/* Unknown
	Map Name (Map ID)
	Chair Gachapon.
 */
importPackage(Packages.tools); 
importPackage(Packages.server.life);

var itemToUse = 4031545;

var chairs = new Array(3010000, 3010001, 3010002, 3010003, 3010004, 3010005, 3010006, 3010007, 3010008, 3010009, 3010010, 3010011, 3010012, 3010013, 
3010015, 3010016, 3010017, 3010018, 3010019, 3010022, 3010023, 3010024, 3010025, 3010026, 3010028, 3010040, 3010041, 3010043, 3010045, 3010046, 3010047,
3010057,3010058,3010060,3010061,3010062,3010063, 3010064,3010065,3010066,3010067,3010069,3010071,3010072,3010073,3010080,3010081,3010082,3010083, 3010084,
3010085,3010097,3010098,3010099,3010101,3010106,3010116,3011000,3012005,3012010,3012011);
var chairAmount = 1;
var status;
var choice;
 
function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 0)
        cm.dispose();
    else {
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0 && mode == 1) {
			if(cm.getPlayer().getLevel() < 10) {
				cm.sendOk("Hello, I am the Chair Gachapon for #rElation#k!\r\n\r\nI am sorry, but I can only work for players #blevel 10 or over#k.");
				cm.dispose();
				return;
			}
			var outStr = "Hello, I am the Chair Gachapon for #rElation#k!\r\n";
			outStr += "You currently have #r#c" + itemToUse + "##k #t" + itemToUse + "#\r\n\r\n";
			outStr += "#L2#I would like to exchange 1 #t" + itemToUse + "# for " + chairAmount + " Random Chair" + (chairAmount > 1 ? "s" : "?") + "#l\r\n";
			cm.sendSimple(outStr);
		} else if(status == 1) {
			choice = selection;
			
			if(selection > 0) {
				if(!cm.haveItem(itemToUse)) {
					cm.sendOk("I'm sorry, but you don't have any #t" +itemToUse + ".");
					cm.dispose();
					return;
				}
			}
			
		    if(selection == 2) {
				// Exchange 1 Blue Wish Ticket for Chair
				cm.sendYesNo("Would you like to exchange 1 #t" + itemToUse + "# for " + chairAmount + " Random Chair" + (chairAmount > 1 ? "s" : "") + "?");
			} else {
				cm.dispose();
			}
		} else if(status == 2) {
				
			if(choice == 2 && cm.haveItem(itemToUse)) {
				if(!cm.getPlayer().getInventory(Packages.client.inventory.MapleInventoryType.SETUP).isFull(chairAmount)) {
					
					var chairStr = "";
					for(var i = 0; i < chairAmount; i++) {
						var chair = chairs[Math.floor(Math.random() * chairs.length)];
						cm.gainItem(chair, 1, true);
						cm.gainItem(itemToUse, -1);
						chairStr += chair + " ";
					}
					
					if(useVP)
						cm.getClient().useVotePoints(1);
					else
						cm.gainItem(itemToUse, -1);
						
					cm.logLeaf("Chair ID: " + chairStr);
					cm.dispose();
				} else {
					cm.sendOk("Please make sure you have enough space to hold the items!");
				}
			}
		} else {
			cm.dispose();
		}
    }
}