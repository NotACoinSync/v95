importPackage(Packages.tools);
importPackage(Packages.server.life);

var ids = [2043022, 2044713, 2044613, 2044513, 2044420, 2044320, 2044220, 2044120, 2044028, 2043813, 2043713, 2043313, 2043220, 2043120, 2043022, 2041068, 2041069, 2040943, 2040833, 2040834, 2040755, 2040756, 2040629, 2040542, 2040543, 2040429, 2040333, 2040045, 2040046, 2101207, 2070018, 2070016, 4001129, 1082223, 2020020, 2020020, 2020020, 2020020, 1102041, 2049100, 4031543, 3010069, 3010092, 3010080, 3010111, 3010085, 3010106, 3010008, 3010007, 3010010, 3010009, 3010017, 3010018, 3010018, 3010018, 3010018, 3010011, 3012011, 3010092, 3010071, 3010063, 3010000, 3010000, 3010000, 3010004, 3010003];
var status = 0;

function start() {
    if (status == 0) {
        cm.SendNext("#eHi, I am Extreme Custom Gachapon of #bNull#e. I give Chairs and Pink Scrolls, and some random items.You will need to have this item to use me. you will need #r4#n of #v[4030002]# and #r2#n of #v[4001129]#.");
        if (cm.haveItem(4030002, 4)) {
            if (cm.haveItem(4001129, 2)) {
            } else {
                cm.sendOk("You don't have the required items, come back when you have them.");
                cm.dispose();
            }
        }
    } else if (status == 1) {
        cm.gainItem(4030002, -4);
        cm.gainItem(4001129, -2);
        cm.processGachapon(ids, true);
        cm.SendOk("#eHope you satisfied with the item you got, Come back again soon. Thanks for playing #bNull");
        cm.dispose();
    }
}

