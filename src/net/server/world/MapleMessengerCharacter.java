package net.server.world;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import client.MapleCharacter;

public class MapleMessengerCharacter implements Externalizable {

    private static final long serialVersionUID = -3479245946457033058L;
    private String name;
    private MapleCharacter player;
    private int id, position;
    private int channel;
    private boolean online;

    public MapleMessengerCharacter() {
        super();
    }

    public MapleMessengerCharacter(MapleCharacter maplechar, int position) {
        this.player = maplechar;
        this.name = maplechar.getName();
        this.channel = maplechar.getClient().getChannel();
        this.id = maplechar.getId();
        this.online = true;
        this.position = position;
    }

    public int getId() {
        return id;
    }

    public MapleCharacter getCharacter() {
        return player;
    }

    public int getChannel() {
        return channel;
    }

    public String getName() {
        return name;
    }

    public boolean isOnline() {
        return online;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MapleMessengerCharacter other = (MapleMessengerCharacter) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(name);
        out.writeInt(id);
        out.writeByte(position);
        out.writeByte(channel);
        out.writeBoolean(online);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        name = (String) in.readObject();
        id = in.readInt();
        position = in.readByte();
        channel = in.readByte();
        online = in.readBoolean();
    }
}
