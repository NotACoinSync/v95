package server.maps;

/**
 * @author AngelSL
 */
public enum FieldLimit {
    JUMP(0x01),
    MOVEMENTSKILLS(0x02),
    SUMMON(0x04),
    DOOR(0x08),
    CHANGECHANNEL(0x10),
    EXPLOSS_PORTALSCROLL(0x20), // Not sure. Used in boss rooms
    CANNOTVIPROCK(0x40),
    CANNOTMINIGAME(0x80),
    SPECIFICPORTALSCROLLLIMIT(0x100),
    CANNOTUSEMOUNTS(0x200),
    STATCHANGEITEMCONSUMELIMIT(0x400),
    CANTSWITCHPARTYLEADER(0x800),
    CANNOTUSEPOTION(0x1000),
    CANTWEDDINGINVITE(0x2000),
    CASHWEATHER(0x4000),
    CANTUSEPET(0x8000), // Ariant colosseum-related?
    CANTUSEMACRO(0x10000), // No notes
    CANNOTJUMPDOWN(0x20000),
    SUMMONNPCLIMIT(0x40000),
    NOEXPDECREASE(0x80000),
    NOFALLDAMAGE(0x100000),
    SHOPS(0x200000),
    CANTDROP(0x400000),
    ROCKETBOOSTER_LIMIT(0x800000),// v95
    ;

    private long i;

    private FieldLimit(long i) {
        this.i = i;
    }

    public long getValue() {
        return i;
    }

    public boolean check(int fieldlimit) {
        return (fieldlimit & i) == i;
    }
}
