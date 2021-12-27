package net.server.channel.handlers;

import java.awt.Point;
import net.AbstractMaplePacketHandler;
import server.maps.objects.AnimatedMapleMapObject;
import server.movement.Elem;
import server.movement.MovePath;

public abstract class AbstractMovementPacketHandler extends AbstractMaplePacketHandler {

    protected void updatePosition(MovePath path, AnimatedMapleMapObject target, int yoffset) {
        for (Elem elem : path.lElem) {
            if (elem.x != 0 && elem.y != 0) {
                target.setPosition(new Point(elem.x, elem.y));
            }
            target.setStance(elem.bMoveAction);
        }
    }
}
