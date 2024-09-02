package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlockCoordinate;

import java.util.Set;

/**
 * This an interface to handle the animation of the lines cleared
 *
 * @author abdullamaghrabi
 * @version $Id: $Id
 */
public interface LineClearedListener {


    /**
     * This listner handles the event one the line is cleared
     *
     * @param blocksCoordinate an instance of the set of GameBlockCoordinates
     */
    void lineCleared(Set<GameBlockCoordinate> blocksCoordinate);
}
