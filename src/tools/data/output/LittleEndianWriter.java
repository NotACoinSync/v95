package tools.data.output;

import java.awt.Point;

public interface LittleEndianWriter {

    /**
     * Write an array of bytes to the sequence.
     *
     * @param b The bytes to write.
     */
    public void write(byte b[]);

    /**
     * Write the number of zero bytes
     *
     * @param b The bytes to write.
     */
    public void writeZeroBytes(final int i);

    /**
     * Write a byte to the sequence.
     *
     * @param b The byte to write.
     */
    public void write(byte b);

    /**
     * Write a byte in integer form to the sequence.
     *
     * @param b The byte as an <code>Integer</code> to write.
     */
    public void write(int b);

    public void skip(int b);

    /**
     * Writes an integer to the sequence.
     *
     * @param i The integer to write.
     */
    public void writeInt(int i);

    /**
     * Write a short integer to the sequence.
     *
     * @param s The short integer to write.
     */
    public void writeShort(int s);

    /**
     * Write a long integer to the sequence.
     *
     * @param l The long integer to write.
     */
    public void writeLong(long l);

    /**
     * Write a double integer to the sequence.
     *
     * @param d The double integer to write.
     */
    public void writeDouble(double d);

    /**
     * Writes an ASCII string the the sequence.
     *
     * @param s The ASCII string to write.
     */
    void writeAsciiString(String s);

    /**
     * Writes a null-terminated ASCII string to the sequence.
     *
     * @param s The ASCII string to write.
     */
    void writeNullTerminatedAsciiString(String s, int size);

    /**
     * Writes a maple-convention ASCII string to the sequence.
     *
     * @param s The ASCII string to use maple-convention to write.
     */
    void writeMapleAsciiString(String s);

    /**
     * Writes a 2D 4 byte position information
     *
     * @param s The Point position to write.
     */
    void writePos(Point s);

    /**
     * Writes a boolean true ? 1 : 0
     *
     * @param b The boolean to write.
     */
    void writeBoolean(final boolean b);
}
