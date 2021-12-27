package client.command.gm;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import server.shops.MapleShopFactory;

public class CommandGmShop extends Command {

    public CommandGmShop() {
        super("gmshop", "Open GM Shop", "!GMShop", null);
        setGMLevel(PlayerGMRank.GM);
    }

    @Override
    public boolean execute(MapleClient c, String commandLabel, String[] args) {
        MapleShopFactory.getInstance().getShop(1337).sendShop(c);
        return false;
    }
}
