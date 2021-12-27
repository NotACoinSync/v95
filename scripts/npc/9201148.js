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
/*
	NPC Name: 		Nurse Pretty
        Map(s): 		Victoria Road : Henesys Hair Salon (100000104)
	Description: 		Royal Faces

        GMS-like revised by Amity
*/

// Modified by Amity
var status = 0;

var mhair_r = Array(30000, 30010, 30020, 30030, 30040, 30050, 30060, 30070, 30080, 30090, 30100, 30110, 30120, 30130, 30140, 30180, 30190, 30200, 30210, 30220, 30230, 30240, 30250, 30320, 30260, 30270, 30280, 30290, 30300, 30310, 30330, 30340, 30350, 30360, 30370, 30400, 30410, 30420, 30440,
30450, 30460, 30470, 30480, 30490, 30510, 30520, 30530, 30540, 30550, 30560, 30570, 30580, 30590, 30600, 30610, 30620, 30630, 30640, 30650, 30660, 30670, 30680, 30690, 30700, 30710, 30720, 30730, 30740, 30750, 30760, 30770, 30780, 30790, 30800, 30810, 30820, 30830, 30840, 30850, 30860,
30870, 30880, 30890, 30900, 30910, 30920, 30930, 30940, 30950, 30990, 32380, 32390, 32410, 33000, 33040, 33050, 33100, 33110, 33120, 33130, 33150, 33170, 33180, 33220, 33240, 33260, 33270, 33280, 33290, 33330, 33360, 33370, 33380, 32550, 33440, 33450, 33460, 33470, 33480, 33500, 33510,
32370, 33530, 30300, 33550, 33460, 32160, 33620, 33630, 33660, 33670, 33680, 33690, 34130);
var fhair_r = Array(31000, 31010, 31020, 31030, 31040, 31050, 31060, 31070, 31080, 31090, 31100, 31110, 31120, 31130, 31140, 31150, 31160, 31170, 31180, 31190, 31200, 31210, 31220, 31230, 31240, 31250, 31260, 31270, 31280, 31290, 31300, 31310, 31320, 31330, 31340, 31350, 31360, 31400, 31410,
31420, 31440, 31450, 31460, 31470, 31480, 31490, 31510, 31520, 31530, 31540, 31550, 31560, 31570, 31580, 31590, 31600, 31610, 31620, 31630, 31640, 31650, 31660, 31670, 31680, 31690, 31700, 31710, 31720, 31730, 31740, 31750, 31760, 31770, 31800, 31810, 31820, 31830, 31840, 31850, 31860, 31870,
31880, 31890, 31910, 31920, 31940, 31950, 31990, 32340, 32360, 33160, 34000, 34010, 34020, 34030, 34040, 34050, 34060, 34070, 34120, 34150, 34170, 34180, 34210, 34220, 34240, 34250, 34260, 34270, 34310, 34320, 34330, 32540, 34360, 34370, 34380, 34400, 34410, 34420, 33760, 34450, 34470,
34480, 34490, 34510, 34590, 34610, 34620, 34630, 34650, 34660, 34670, 34680, 34690, 34720, 34780);

var mhair_v = Array(30000, 30010, 30020, 30030, 30040, 30050, 30060, 30070, 30080, 30090, 30100, 30110, 30120, 30130, 30140, 30180, 30190, 30200, 30210, 30220, 30230, 30240, 30250, 30320, 30260, 30270, 30280, 30290, 30300, 30310, 30330, 30340, 30350, 30360, 30370, 30400, 30410, 30420, 30440,
30450, 30460, 30470, 30480, 30490, 30510, 30520, 30530, 30540, 30550, 30560, 30570, 30580, 30590, 30600, 30610, 30620, 30630, 30640, 30650, 30660, 30670, 30680, 30690, 30700, 30710, 30720, 30730, 30740, 30750, 30760, 30770, 30780, 30790, 30800, 30810, 30820, 30830, 30840, 30850, 30860,
30870, 30880, 30890, 30900, 30910, 30920, 30930, 30940, 30950, 30990, 32380, 32390, 32410, 33000, 33040, 33050, 33100, 33110, 33120, 33130, 33150, 33170, 33180, 33220, 33240, 33260, 33270, 33280, 33290, 33330, 33360, 33370, 33380, 32550, 33440, 33450, 33460, 33470, 33480, 33500, 33510,
32370, 33530, 30300, 33550, 33460, 32160, 33620, 33630, 33660, 33670, 33680, 33690, 34130);
var fhair_v = Array(31000, 31010, 31020, 31030, 31040, 31050, 31060, 31070, 31080, 31090, 31100, 31110, 31120, 31130, 31140, 31150, 31160, 31170, 31180, 31190, 31200, 31210, 31220, 31230, 31240, 31250, 31260, 31270, 31280, 31290, 31300, 31310, 31320, 31330, 31340, 31350, 31360, 31400, 31410,
31420, 31440, 31450, 31460, 31470, 31480, 31490, 31510, 31520, 31530, 31540, 31550, 31560, 31570, 31580, 31590, 31600, 31610, 31620, 31630, 31640, 31650, 31660, 31670, 31680, 31690, 31700, 31710, 31720, 31730, 31740, 31750, 31760, 31770, 31800, 31810, 31820, 31830, 31840, 31850, 31860, 31870,
31880, 31890, 31910, 31920, 31940, 31950, 31990, 32340, 32360, 33160, 34000, 34010, 34020, 34030, 34040, 34050, 34060, 34070, 34120, 34150, 34170, 34180, 34210, 34220, 34240, 34250, 34260, 34270, 34310, 34320, 34330, 32540, 34360, 34370, 34380, 34400, 34410, 34420, 33760, 34450, 34470,
34480, 34490, 34510, 34590, 34610, 34620, 34630, 34650, 34660, 34670, 34680, 34690, 34720, 34780);

var hairnew = Array();

function pushIfItemExists(array, itemid) {
    if ((itemid = cm.getCosmeticItem(itemid)) != -1 && !cm.isCosmeticEquipped(itemid)) {
        array.push(itemid);
    }
}
function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode < 1) {  // disposing issue with stylishs found thanks to Vcoc
        cm.dispose();
    } else {
        if (mode == 1)
            status++;
        else
            status--;

        if (status == 0) {
            cm.sendSimple("Hi, I'm #p1012117#, the most charming and stylish stylist around. If you're looking for the best looking hairdos around, look no further!\r\n\#L0##i5150040##t5150040##l\r\n\#L1##i5150044##t5150044##l");
        } else if (status == 1) {
            if (selection == 0) {
                beauty = 1;
                cm.sendYesNo("If you use this REGULAR coupon, your hair may transform into a random new look...do you still want to do it using #b#t5150040##k, I will do it anyways for you. But don't forget, it will be random!");
            } else {
                beauty = 2;

                hairnew = Array();
                if (cm.getPlayer().getGender() == 0) {
                    for(var i = 0; i < mhair_v.length; i++) {
                        pushIfItemExists(hairnew, mhair_v[i] + parseInt(cm.getPlayer().getHair() % 10));
                    }
                }
                else {
                    for(var i = 0; i < fhair_v.length; i++) {
                        pushIfItemExists(hairnew, fhair_v[i] + parseInt(cm.getPlayer().getHair() % 10));
                    }
                }

                cm.sendStyle("Using the SPECIAL coupon you can choose the style your hair will become. Pick the style that best provides you delight...", hairnew);
            }
        } else if (status == 2) {
            if (beauty == 1) {
                if (cm.haveItem(5150040) == true){
                    hairnew = Array();
                    if (cm.getPlayer().getGender() == 0) {
                        for(var i = 0; i < mhair_r.length; i++) {
                            pushIfItemExists(hairnew, mhair_r[i] + parseInt(cm.getPlayer().getHair() % 10));
                        }
                    }
                    else {
                        for(var i = 0; i < fhair_r.length; i++) {
                            pushIfItemExists(hairnew, fhair_r[i] + parseInt(cm.getPlayer().getHair() % 10));
                        }
                    }

                    cm.gainItem(5150040, -1);
                    cm.setHair(hairnew[Math.floor(Math.random() * hairnew.length)]);
                    cm.sendOk("Enjoy your new and improved hairstyle!");
                } else {
                    cm.sendOk("Hmmm...it looks like you don't have our designated coupon...I'm afraid I can't give you a haircut without it. I'm sorry...");
                }
            } else if (beauty == 2) {
                if (cm.haveItem(5150044) == true){
                    cm.gainItem(5150044, -1);
                    cm.setHair(hairnew[selection]);
                    cm.sendOk("Enjoy your new and improved hairstyle!");
                } else {
                    cm.sendOk("Hmmm...it looks like you don't have our designated coupon...I'm afraid I can't give you a haircut without it. I'm sorry...");
                }
            }

            cm.dispose();
        }
    }
}
