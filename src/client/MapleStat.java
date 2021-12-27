package client;

public enum MapleStat {
    SKIN(0x1),
    FACE(0x2),
    HAIR(0x4),
    PETSN(0x8),
    LEVEL(0x10),
    JOB(0x20),
    STR(0x40),
    DEX(0x80),
    INT(0x100),
    LUK(0x200),
    HP(0x400),
    MAXHP(0x800),
    MP(0x1000),
    MAXMP(0x2000),
    AVAILABLEAP(0x4000),
    AVAILABLESP(0x8000),
    EXP(0x10000),
    FAME(0x20000),
    MESO(0x40000),
    PETSN2(0x80000),
    PETSN3(0x100000),
    GACHAEXP(0x200000);

    private final int i;

    private MapleStat(int i) {
        this.i = i;
    }

    public int getValue() {
        return i;
    }

    public static MapleStat getByValue(int value) {
        for (MapleStat stat : MapleStat.values()) {
            if (stat.getValue() == value) {
                return stat;
            }
        }
        return null;
    }

    public static MapleStat getBy5ByteEncoding(int encoded) {
        switch (encoded) {
            case 64:
                return STR;
            case 128:
                return DEX;
            case 256:
                return INT;
            case 512:
                return LUK;
        }
        return null;
    }

    public static MapleStat getByString(String type) {
        if (type.equals("SKIN")) {
            return SKIN;
        } else if (type.equals("FACE")) {
            return FACE;
        } else if (type.equals("HAIR")) {
            return HAIR;
        } else if (type.equals("LEVEL")) {
            return LEVEL;
        } else if (type.equals("JOB")) {
            return JOB;
        } else if (type.equals("STR")) {
            return STR;
        } else if (type.equals("DEX")) {
            return DEX;
        } else if (type.equals("INT")) {
            return INT;
        } else if (type.equals("LUK")) {
            return LUK;
        } else if (type.equals("HP")) {
            return HP;
        } else if (type.equals("MAXHP")) {
            return MAXHP;
        } else if (type.equals("MP")) {
            return MP;
        } else if (type.equals("MAXMP")) {
            return MAXMP;
        } else if (type.equals("AVAILABLEAP")) {
            return AVAILABLEAP;
        } else if (type.equals("AVAILABLESP")) {
            return AVAILABLESP;
        } else if (type.equals("EXP")) {
            return EXP;
        } else if (type.equals("FAME")) {
            return FAME;
        } else if (type.equals("MESO")) {
            return MESO;
        } else if (type.equals("PET")) {
            return PETSN;
        }
        return null;
    }
}
