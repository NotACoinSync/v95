var exitMap;
var startMap;
var otherMap;
var minPlayers = 1;
var fightTime = 30;
var timer = 1000 * 60 * fightTime;

function init() {
	exitMap = em.getChannelServer().getMap(103040400);
	startMap = em.getChannelServer().getMap(103040440);
	otherMap = em.getChannelServer().getMap(103040450);
}

function setup() {
    var eim = em.newInstance("VIPRockSpirit_" + em.getProperty("player"));
    respawn(eim);
    eim.startEventTimer(timer);    
	return eim;
}

function respawn(eim) {
	var map = eim.getMapInstance(startMap.getId());
	var map2 = eim.getMapInstance(otherMap.getId());
	map.allowSummonState(true);
	map2.allowSummonState(true);
	map.instanceMapRespawn();
	map2.instanceMapRespawn();
	eim.schedule("respawn", 10000);
}


function playerEntry(eim, player) {
	var amplifierMap = eim.getMapInstance(startMap.getId());
	player.changeMap(amplifierMap);
    eim.schedule("timeOut", timer);
}

function playerRevive(eim, player) {
    player.setHp(50);
    player.setStance(0);
    eim.unregisterPlayer(player);
    player.changeMap(exitMap);
    return false;
}

function playerDead(eim, player) {}

function playerDisconnected(eim, player) {
    var party = eim.getPlayers();
	for (var i = 0; i < party.size(); i++) {
	    if (party.get(i).equals(player)) {
	        removePlayer(eim, player);
	    } else {
	        playerExit(eim, party.get(i));
	    }
	}
	eim.dispose();
}

function monsterValue(eim,mobId) { 
    return -1;
}

function leftParty(eim, player) {
    var party = eim.getPlayers();
    if (party.size() < minPlayers) {
        for (var i = 0; i < party.size(); i++)
            playerExit(eim,party.get(i));
        eim.dispose();
    }
    else
        playerExit(eim, player);
}

function disbandParty(eim) {}

function playerExit(eim, player) {
    eim.unregisterPlayer(player);
    player.changeMap(exitMap, exitMap.getPortal(0));
}


function moveMap(eim, player) {
	if (player.getMap().getId() == exitMap.getId()) {
		removePlayer(eim, player);
		player.getClient().announce(Field.DestroyClock());
		eim.dispose();
	}
}

function removePlayer(eim, player) {
    eim.unregisterPlayer(player);
    player.getMap().removePlayer(player);
    player.setMap(exitMap);
}

function cancelSchedule() {}

function dispose() {}

function clearPQ(eim) {}

function allMonstersDead(eim) {}

function timeOut(eim) {
    if (eim != null) {
        if (eim.getPlayerCount() > 0) {
            var pIter = eim.getPlayers().iterator();
            while (pIter.hasNext()){
				var player = pIter.next();
                playerExit(eim, player);
			}
        }
        eim.dispose();
    }
}