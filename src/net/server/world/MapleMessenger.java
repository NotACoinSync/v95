package net.server.world;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class MapleMessenger implements Externalizable {

    private static final long serialVersionUID = 3629344026903799481L;
    private int id;
    private List<MapleMessengerCharacter> members = new ArrayList<MapleMessengerCharacter>(3);
    private boolean[] pos = new boolean[3];

    public MapleMessenger() {
        super();
    }

    public MapleMessenger(int id, MapleMessengerCharacter chrfor) {
        this.id = id;
        for (int i = 0; i < 3; i++) {
            pos[i] = false;
        }
        addMember(chrfor, chrfor.getPosition());
    }

    public int getId() {
        return id;
    }

    public Collection<MapleMessengerCharacter> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public void addMember(MapleMessengerCharacter member, int position) {
        members.add(member);
        member.setPosition(position);
        pos[position] = true;
    }

    public void removeMember(MapleMessengerCharacter member) {
        int position = member.getPosition();
        pos[position] = false;
        members.remove(member);
    }

    public int getLowestPosition() {
        for (byte i = 0; i < 3; i++) {
            if (!pos[i]) {
                return i;
            }
        }
        return -1;
    }

    public int getPositionByName(String name) {
        for (MapleMessengerCharacter messengerchar : members) {
            if (messengerchar.getName().equals(name)) {
                return messengerchar.getPosition();
            }
        }
        return -1;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(id);
        out.writeByte(members.size());
        for (MapleMessengerCharacter mmc : members) {
            mmc.writeExternal(out);
        }
        for (int i = 0; i < 3; i++) {
            out.writeBoolean(pos[i]);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = in.readInt();
        int memberSize = in.readByte();
        for (int i = 0; i < memberSize; i++) {
            MapleMessengerCharacter mmc = new MapleMessengerCharacter();
            mmc.readExternal(in);
            members.add(mmc);
        }
        for (int i = 0; i < 3; i++) {
            pos[i] = in.readBoolean();
        }
    }
}
