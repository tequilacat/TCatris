package org.tequilacat.tcatris.games;

import android.graphics.Rect;

import org.tequilacat.tcatris.core.ABrickGame;
import org.tequilacat.tcatris.core.Dimensions;
import org.tequilacat.tcatris.core.GameDescriptor;
import org.tequilacat.tcatris.core.GameImpulse;

/**
 * Common methods used in rotation games
 */
public abstract class AbstractRotationGame extends FlatGame {
  private final FlatShape[] _originalShapes;
  private Dimensions _maxNextShapeDimension;

  protected AbstractRotationGame(GameDescriptor descriptor, AbstractFlatGamePainter fieldPainter,
                                 FlatShape[] staticShapes) {
    super(descriptor, fieldPainter);
    _originalShapes = staticShapes;
    _maxNextShapeDimension = computeMaxNextShapeDimensions(_originalShapes);
  }

  /**
   * @param shapes array of possible shapes
   * @return max dimensions of specified shapes in cells
   */
  private Dimensions computeMaxNextShapeDimensions(FlatShape[] shapes) {
    Rect bounds = new Rect();
    int _maxNextWidth = 0;
    int _maxNextHeight = 0;

    for (FlatShape shape : shapes) {
      shape.getBounds(bounds);
      int w = bounds.width(), h = bounds.height();

      if (_maxNextWidth < w) {
        _maxNextWidth = w;
      }

      if (_maxNextHeight < h) {
        _maxNextHeight = h;
      }
    }

    return new Dimensions(_maxNextWidth, _maxNextHeight);
  }

  @Override
  protected FlatShape createNext() {
    return _originalShapes[getRandomInt(_originalShapes.length)].createCopy();
  }

  @Override
  protected int getMaxNextWidth() {
    return _maxNextShapeDimension.width;
  }

  @Override
  protected int getMaxNextHeight() {
    return _maxNextShapeDimension.height;
  }

  @Override
  public ABrickGame.ImpulseSemantics getImpulseSemantics(GameImpulse impulse) {
    switch (impulse){
      case MOVE_LEFT: return ABrickGame.ImpulseSemantics.MOVE_LEFT;
      case MOVE_RIGHT: return ABrickGame.ImpulseSemantics.MOVE_RIGHT;
      case ROTATE_CW: return ABrickGame.ImpulseSemantics.ROTATE_CW;
      case ROTATE_CCW: return ABrickGame.ImpulseSemantics.ROTATE_CCW;
      default: return null;
    }
  }
}
