package net.server.handlers.DamageMeter;

public class AddDamageInfo {

    public static int TotalDamage = 0;
    public static int MaxAverageDamage = 0;
    public static int AverageDamage = 0;
    public static boolean Activate = true;

    private AddDamageInfo(int TotalDamage, int AverageDamage) {
        this.TotalDamage = TotalDamage;
        this.AverageDamage = AverageDamage;
    }

    public static int getTotalDamage() {
        return TotalDamage;
    }

    public static void setTotalDamage(int Damage) {
        TotalDamage = Damage;
    }

    public static int getMaxAverageDamage() {
        return MaxAverageDamage;
    }

    public static void setMaxAverageDamage(int Damage) {
        MaxAverageDamage = Damage;
    }

    public static int getAverageDamage() {
        return AverageDamage;
    }

    public static void setAverageDamage(int Damage) {
        AverageDamage = Damage;
    }

    public static boolean getActivate() {
        return Activate;
    }

    public void setActivate(boolean Activated) {
        this.Activate = Activated;
    }

}
