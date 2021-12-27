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
 *@Author:     kevintjuh93
*/

importPackage(Packages.tools); 
importPackage(Packages.server.life);
var player;

function start(ms) { 
	var mobId = 9001007;
	player = ms.getPlayer();
        player.resetEnteredScript(); 
        player.startMapEffect("DPS Counter Starts now", 5120020); 
	var map = player.getMap();
	if (map.getTimeLeft() > 0) {
		ms.getClient().announce(MaplePacketCreator.getClock(map.getTimeLeft()));
	} else {
		if(map.getMonsterById(mobId) != null){
		map.addDpsMapTimer(player,65);
		player.setDamage(0);
		player.StartDps();
			return;   	       
		}
		if(map.getMonsterById(mobId) == null){
		map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(mobId), new java.awt.Point(38, 215));
		map.addDpsMapTimer(player,65);
		player.setDamage(0);
		player.StartDps();
		}
	} 
}