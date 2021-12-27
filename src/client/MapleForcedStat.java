package client;

public enum MapleForcedStat {
    STR(0x1),
    DEX(0x2),
    INT(0x4),
    LUK(0x8),
    PAD(0x10),
    PDD(0x20),
    MAD(0x40),
    MDD(0x80),
    ACC(0x100),
    EVA(0x200),
    SPEED(0x400),
    JUMP(0x800),
    SPEEDMAX_CS(0x1000);

    private final int i;

    private MapleForcedStat(int i) {
        this.i = i;
    }

    public int getValue() {
        return i;
    }

    public static MapleForcedStat getByValue(int value) {
        for (MapleForcedStat stat : MapleForcedStat.values()) {
            if (stat.getValue() == value) {
                return stat;
            }
        }
        return null;
    }

    public static MapleForcedStat getBy5ByteEncoding(int encoded) {
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

    public static MapleForcedStat getByString(String type) {
        if (type.equals("STR")) {
            return STR;
        } else if (type.equals("DEX")) {
            return DEX;
        } else if (type.equals("INT")) {
            return INT;
        } else if (type.equals("LUK")) {
            return LUK;
        } else if (type.equals("PAD")) {
            return PAD;
        } else if (type.equals("PDD")) {
            return PDD;
        } else if (type.equals("MAD")) {
            return MAD;
        } else if (type.equals("MDD")) {
            return MDD;
        } else if (type.equals("ACC")) {
            return ACC;
        } else if (type.equals("EVA")) {
            return EVA;
        } else if (type.equals("SPEED")) {
            return SPEED;
        } else if (type.equals("JUMP")) {
            return JUMP;
        } else if (type.equals("SPEEDMAX_CS")) {
            return SPEEDMAX_CS;
        }
        return null;
    }
}
