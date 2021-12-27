package net.server.guild;

import tools.packets.WvsContext;

public enum MapleGuildResponse {
    NOT_IN_CHANNEL(0x2a),
    ALREADY_IN_GUILD(0x28),
    NOT_IN_GUILD(0x2d);

    private int value;

    private MapleGuildResponse(int val) {
        value = val;
    }

    public final byte[] getPacket() {
        return WvsContext.GuildResult.genericGuildMessage((byte) value);
    }
}
