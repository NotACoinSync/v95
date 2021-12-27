function getInactiveReactors(map) {
    var reactors = new Array();
        
    var iter = map.getReactors().iterator();
    while (iter.hasNext()) {
        var mo = iter.next();
        if (mo.getState() >= 7) {
            reactors.push(mo);
        }
    }
        
    return reactors;
}

function start(ms) {   	       
	var map = ms.getClient().getChannelServer().getMapFactory().getMap(240040611);
        map.resetReactors(getInactiveReactors(map));
        map.resetFully();
	return(true);
}