package net.server.channel.handlers;

import client.MapleClient;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.packets.WvsContext.FriendResult.Info;

public final class AccountMoreInfoHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        byte type = slea.readByte();
        switch (type) {
            case Info.First:
                System.out.println("First: " + slea.toString());
                break;
            case Info.LoadRequest:
                // Need to send data to update shit
                break;
            case Info.LoadResult:
                System.out.println("LoadResult: " + slea.toString());
                break;
            case Info.SaveRequest:
                System.out.println("SaveRequest: " + slea.toString());
                break;
            case Info.SaveResult:
                System.out.println("SaveResult: " + slea.toString());
                break;
        }
    }
}
