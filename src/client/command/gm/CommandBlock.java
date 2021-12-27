package client.command.gm;

import client.MapleCharacter;
import client.MapleClient;
import client.MessageType;
import client.PlayerGMRank;
import client.command.Command;
import net.channel.ChannelServer;
import tools.packets.PacketHelper;
import tools.ObjectParser;
import tools.StringUtil;
import tools.packets.Field;

public class CommandBlock extends Command {

    public CommandBlock() {
        super("Block", "Tempban a target", "!Block <target> <type> <duration>", null);
        setGMLevel(PlayerGMRank.GM);
    }

    @Override
    public boolean execute(MapleClient c, String commandLabel, String[] args) {
        if (args.length > 2) {
            String victim = args[0];
            int type = ObjectParser.isInt(args[1]); // reason
            int duration = ObjectParser.isInt(args[2]);
            String description = StringUtil.joinStringFrom(args, 2);
            String reason = c.getPlayer().getName() + " ";
            MapleCharacter target = ChannelServer.getInstance().getCharacterByName(victim);
            String readableTargetName = victim;
            if (target != null) {
                readableTargetName = MapleCharacter.makeMapleReadable(target.getName());
                String ip = target.getClient().getSession().remoteAddress().toString().split(":")[0];
                reason += readableTargetName + " (IP: " + ip + ")";
                if (duration == -1) {
                    target.ban(description + " " + reason);
                } else {
                    target.block(type, duration, description);
                    target.sendPolice(duration, reason, 6000);
                }
                c.announce(Field.AdminResult.getGMEffect((byte) 4, (byte) 0));
            } else if (duration != -1) {
                MapleCharacter.block(victim, type, duration, description);
                c.announce(Field.AdminResult.getGMEffect((byte) 4, (byte) 0));
            } else {
                c.announce(Field.AdminResult.getGMEffect((byte) 6, (byte) 1));
            }
        } else {
            c.getPlayer().dropMessage(MessageType.ERROR, getUsage());
        }
        return false;
    }
}
