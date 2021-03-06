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

//Time Setting is in millisecond
var closeTime = 4 * 60 * 1000; //The time to close the gate
var beginTime = 5 * 60 * 1000; //The time to begin the ride
var rideTime = 10 * 60 * 1000; //The time that require move to destination
var Orbis_btf;
var Genie_to_Orbis;
var Orbis_docked;
var Ariant_btf;
var Genie_to_Ariant;
var Ariant_docked;

function init() {
    Orbis_btf = em.getChannelServer().getMap(200000152);
    Ariant_btf = em.getChannelServer().getMap(260000110);
    Genie_to_Orbis = em.getChannelServer().getMap(200090410);
    Genie_to_Ariant = em.getChannelServer().getMap(200090400);
    Orbis_docked = em.getChannelServer().getMap(200000151);
    Ariant_docked = em.getChannelServer().getMap(260000100);
    Orbis_Station = em.getChannelServer().getMap(200000100);
    
    var cal = Packages.java.util.Calendar.getInstance();
    cal.setTime(new Packages.java.util.Date());
    var unroundedMins = cal.get(Packages.java.util.Calendar.MINUTE);
    var mod = unroundedMins % 10;
    cal.add(Packages.java.util.Calendar.MINUTE, 10 - mod);
    cal.set(Packages.java.util.Calendar.SECOND, 0);
    cal.set(Packages.java.util.Calendar.MILLISECOND, 0);
    
    em.setProperty("docked", "true");
    em.setProperty("entry", "true");
    
    em.scheduleAtTimestamp("stopEntry", cal.getTimeInMillis() - 60000);
    em.scheduleAtTimestamp("takeoff", cal.getTimeInMillis());
}



function scheduleNew() {
    Ariant_docked.setDocked(true);
    Orbis_docked.setDocked(true);
    Ariant_docked.announce(Packages.tools.packets.CField.ContiMove.State(1, false));
    Orbis_docked.announce(Packages.tools.packets.CField.ContiMove.State(1, false));   
    em.setProperty("docked", "true");
    em.setProperty("entry", "true");
    em.schedule("stopEntry", closeTime);
    em.schedule("takeoff", beginTime);
}

function stopEntry() {
    em.setProperty("entry","false");
}

function takeoff() {
	em.setProperty("docked","false");
    Ariant_docked.setDocked(false);
    Orbis_docked.setDocked(false);
    Ariant_docked.announce(Packages.tools.packets.CField.ContiMove.State(2, false));
    Orbis_docked.announce(Packages.tools.packets.CField.ContiMove.State(2, false));
	Orbis_btf.warpEveryone(Genie_to_Ariant.getId());
	Ariant_btf.warpEveryone(Genie_to_Orbis.getId());
    em.schedule("arrived", rideTime);
}

function arrived() {
	Genie_to_Orbis.warpEveryone(Orbis_Station.getId());
	Genie_to_Ariant.warpEveryone(Ariant_docked.getId());
    scheduleNew();
}

function cancelSchedule() {
}