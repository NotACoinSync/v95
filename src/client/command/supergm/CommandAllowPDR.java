package client.command.supergm;

import client.MapleClient;
import client.PlayerGMRank;
import client.command.Command;
import constants.ServerConstants;
import net.channel.ChannelServer;
import net.server.channel.Channel;
import tools.packets.WvsContext;

public class CommandAllowPDR extends Command {

    public CommandAllowPDR() {
        super("AllowPDR", "", "", null);
        setGMLevel(PlayerGMRank.SUPERGM);
    }

    @Override
    public boolean execute(MapleClient c, String commandLabel, String[] args) {
        for (Channel ch : ChannelServer.getInstance().getChannels()) {
            ch.setServerMessage("It's Potion Discount Rate Event again! Now with " + ServerConstants.PotionDiscountRate + " %! BUY NOW MAPLERS OR NEVER!!!!");

        }
        c.announce(WvsContext.SetPotionDiscountRate(ServerConstants.PotionDiscountRate));
        return false;
    }
}
