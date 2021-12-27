package server.movement;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;

public class MovePath {

    public List<Elem> lElem = new LinkedList<Elem>();
    public Point startPosition, velocityPosition;

    public void decode(SeekableLittleEndianAccessor iPacket) {
        startPosition = iPacket.readPos();
        velocityPosition = iPacket.readPos();
        byte size = iPacket.readByte();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                Elem elem = new Elem();
                elem.decode(iPacket);
                lElem.add(elem);
            }
        }
    }

    public void encode(MaplePacketLittleEndianWriter oPacket) {
        oPacket.writePos(startPosition);
        oPacket.writePos(velocityPosition);
        oPacket.write(lElem.size());
        if (lElem.size() > 0) {
            for (Elem elem : lElem) {
                elem.encode(oPacket);
            }
        }
    }
}
