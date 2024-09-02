package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlock;

/**
 * An interface responsible about the right click of the interface
 *
 * @author abdullamaghrabi
 * @version $Id: $Id
 */
public interface RightClickedListener {

    /**
     * <p>rightClick.</p>
     *
     * @param x a int
     * @param y a int
     */
    void rightClick(int x, int y);
}
