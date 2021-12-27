package tools.data.input;

import java.awt.Point;

public interface LittleEndianAccessor {

    byte readByte();

    boolean readBoolean();

    char readChar();

    short readShort();

    int readInt();

    Point readPos();

    long readLong();

    void skip(int num);

    byte[] read(int num);

    float readFloat();

    double readDouble();

    String readAsciiString(int n);

    String readNullTerminatedAsciiString();

    String readMapleAsciiString();

    long getBytesRead();

    long available();
}
