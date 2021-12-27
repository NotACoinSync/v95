package server.maps;

import java.awt.Point;

/**
 *
 *
 * @since Jun 17, 2016
 */
public interface Foothold {

    public int getID();

    public Point getStartPos();

    public Point getEndPos();

    public int getType();
}
