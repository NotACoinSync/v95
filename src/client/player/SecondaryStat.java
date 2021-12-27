package client.player;

public enum SecondaryStat {
    PAD(0),
    PDD(1),
    MAD(2),
    MDD(3),
    ACC(4),
    EVA(5),
    Craft(6),
    Speed(7),
    Jump(8),
    MagicGuard(9),
    DarkSight(10),
    Booster(11),
    PowerGuard(12),
    MaxHP(13),
    MaxMP(14),
    Invincible(15),
    SoulArrow(16),
    Stun(17, true),
    Poison(18, true),
    Seal(19, true),
    Darkness(20, true),
    ComboCounter(21),
    WeaponCharge(22),
    DragonBlood(23),
    HolySymbol(24),
    MesoUp(25),
    ShadowPartner(26),
    PickPocket(27),
    MesoGuard(28),
    Thaw(29),
    Weakness(30, true),
    Curse(31, true),
    Slow(32),
    Morph(33),
    Regen(34),
    BasicStatUp(35),
    Stance(36),
    SharpEyes(37),
    MagicReflection(38),
    Attract(39, true),
    SpiritJavelin(40),
    Infinity(41),
    HolyShield(42),
    HamString(43),
    Blind(44),
    Concentration(45),
    BanMap(46),
    MaxLevelBuff(47),
    MesoUpByItem(48),
    Ghost(49),
    Barrier(50),
    ReverseInput(51, true),
    ItemUpByItem(52),
    RespectPImmune(53),
    RespectMImmune(54),
    DefenseAtt(55),
    DefenseState(56),
    IncEffectHPPotion(57),
    IncEffectMPPotion(58),
    DojangBerserk(59),
    DojangInvincible(60),
    Spark(61),
    DojangShield(62),
    SoulMasterFinal(63),
    WindBreakerFinal(64),
    ElementalReset(65),
    WindWalk(66),
    EventRate(67),
    ComboAbilityBuff(68),
    ComboDrain(69),
    ComboBarrier(70),
    BodyPressure(71),
    SmartKnockback(72),
    RepeatEffect(73),
    ExpBuffRate(74),
    StopPortion(75),
    StopMotion(76),
    Fear(77),
    EvanSlow(78),
    MagicShield(79),
    MagicResistance(80),
    SoulStone(81),
    Flying(82),
    Frozen(83),
    AssistCharge(84),
    MirrorImaging(85),
    SuddenDeath(86), // Owl Spirit
    NotDamaged(87),
    FinalCut(88),
    ThornsEffect(89),
    SwallowAttackDamage(90),
    MorewildDamageUp(91),
    Mine(92),
    EMHP(93),
    EMMP(94),
    EPAD(95),
    EPPD(96),
    EMDD(97),
    Guard(98),
    SafetyDamage(99),
    SafetyAbsorb(100),
    Cyclone(101),
    SwallowCritical(102),
    SwallowMaxMP(103),
    SwallowDefence(104),
    SwallowEvasion(105),
    Conversion(106),
    Revive(107),
    Sneak(108),
    Mechanic(109),
    Aura(110),
    DarkAura(111),
    BlueAura(112),
    YellowAura(113),
    SuperBody(114),
    MorewildMaxHP(115),
    Dice(116),
    BlessingArmor(117),
    DamR(118),
    TeleportMasteryOn(119),
    CombatOrders(120),
    Beholder(121),
    EnergyCharged(122),
    Dash_Speed(123),
    Dash_Jump(124),
    RideVehicle(125),
    PartyBooster(126),
    GuidedBullet(127),
    Undead(128, true),
    SummonBomb(129),
    // UNKNOWN:
    SUMMON(99999999),
    PUPPET(99999999);
    
    private final int shift;
    private final int mask;
    private final byte set;
    private final boolean disease;

    private SecondaryStat(int shift) {
        this(shift, false);
    }

    private SecondaryStat(int shift, boolean isDisease) {
        this.shift = shift;
        if (shift == 126 || shift == 127) {
            long stat = ((shift >> 32) & 0xffffffffL);
            if (stat == 0) {
                stat = (shift & 0xffffffffL);
            }
            this.mask = (int) stat;
        } else {
            this.mask = 1 << (shift >> 32);
        }
        this.set = (byte) (shift >> 5);
        this.disease = isDisease;
    }

    public int getShift() {
        return shift;
    }

    public int getMask() {
        return mask;
    }

    public boolean isDisease() {
        return disease;
    }

    public byte getSet() {
        return set;
    }

    public static SecondaryStat getByShift(int shift) {
        for (SecondaryStat buff : values()) {
            if (buff.getShift() == shift) {
                return buff;
            }
        }
        return null;
    }

    public boolean isMovementAffectingStat() {
        switch (this) {
            case Speed:
            case Jump:
            case Stun:
            case Weakness:
            case Slow:
            case Morph:
            case Ghost:
            case BasicStatUp:
            case Attract:
            case RideVehicle:   
            case Dash_Speed:
            case Dash_Jump:
            case BanMap:
            case Flying:
            case Frozen:
            case YellowAura:
                return true;
            default:
                return false;
        }
    }

    public boolean isNoValueStats() {
        switch (this) {
            case ShadowPartner:
            case DarkSight:
            case SoulArrow:
            case Morph:
            case DojangBerserk:
            case DojangInvincible:
            case WindWalk:
            case Flying:
            case Sneak:
            case MorewildDamageUp:
            case BlessingArmor:
                return true;
            default:
                return false;
        }
    }

    public boolean isSwallowBuffs() {
        switch (this) {
            case SwallowAttackDamage:
            case SwallowCritical:
            case SwallowMaxMP:
            case SwallowDefence:
            case SwallowEvasion:
                return true;
            default:
                return false;
        }
    }
}
