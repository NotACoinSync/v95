package net.server.guild;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class MapleGuildSummary implements Externalizable {

    private static final long serialVersionUID = 1560953511549329903L;
    private String name;
    private short logoBG;
    private byte logoBGColor;
    private short logo;
    private byte logoColor;
    private int allianceId;

    public MapleGuildSummary() {
        super();
    }

    public MapleGuildSummary(MapleGuild g) {
        this.name = g.getName();
        this.logoBG = (short) g.getLogoBG();
        this.logoBGColor = (byte) g.getLogoBGColor();
        this.logo = (short) g.getLogo();
        this.logoColor = (byte) g.getLogoColor();
        this.allianceId = g.getAllianceId();
    }

    public String getName() {
        return name;
    }

    public short getLogoBG() {
        return logoBG;
    }

    public byte getLogoBGColor() {
        return logoBGColor;
    }

    public short getLogo() {
        return logo;
    }

    public byte getLogoColor() {
        return logoColor;
    }

    public int getAllianceId() {
        return allianceId;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(name);
        out.writeShort(logoBG);
        out.writeByte(logoBGColor);
        out.writeShort(logo);
        out.writeByte(logoColor);
        out.writeInt(allianceId);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        name = (String) in.readObject();
        logoBG = in.readShort();
        logoBGColor = in.readByte();
        logo = in.readShort();
        logoColor = in.readByte();
        allianceId = in.readInt();
    }
}
