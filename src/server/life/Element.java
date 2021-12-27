package server.life;

public enum Element {
    NEUTRAL,
    FIRE,
    ICE,
    LIGHTING,
    POISON,
    HOLY,
    DARK;

    public static Element getFromChar(char c) {
        switch (Character.toUpperCase(c)) {
            case 'F':
                return FIRE;
            case 'I':
                return ICE;
            case 'L':
                return LIGHTING;
            case 'S':
                return POISON;
            case 'H':
                return HOLY;
            case 'D':
                return DARK;
            case 'P':
                return NEUTRAL;
        }
        throw new IllegalArgumentException("unknown elemnt char " + c);
    }
}
