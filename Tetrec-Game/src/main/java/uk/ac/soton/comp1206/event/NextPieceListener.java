package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * <p>NextPieceListener interface.</p>
 *
 * @author abdullamaghrabi
 * @version $Id: $Id
 */
public interface NextPieceListener {


    /**
     * <p>nextPiece.</p>
     *
     * @param nextPiece       a {@link uk.ac.soton.comp1206.game.GamePiece} object
     * @param followingPieces a {@link uk.ac.soton.comp1206.game.GamePiece} object
     */
    void nextPiece(GamePiece nextPiece, GamePiece followingPieces);

}
