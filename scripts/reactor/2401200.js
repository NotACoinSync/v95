/* @Author Amity
 * 2401200.js: Chaos Horntail's Cave - Summons  Chaos Horntail.
*/

function act() {
    rm.changeMusic("Bgm14/HonTale");
    if (rm.getReactor().getMap().getMonsterById(8810130) == null) {
        rm.getReactor().getMap().spawnHorntailOnGroundBelow(new java.awt.Point(71,260));

        var eim = rm.getEventInstance();
        eim.restartEventTimer(60 * 60000);
    }
    rm.mapMessage(6, "From the depths of his cave, here comes Chaos Horntail!");
}
