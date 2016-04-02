package org.tequilacat.tcatris.games;

import android.graphics.Canvas;

import org.tequilacat.tcatris.core.GameScreenLayout;
import org.tequilacat.tcatris.core.VisualResources;

/**
 * rectangular game with all 2d rect services but without painting exact pixels and field
 */
public abstract class AbstractRectGamePainter extends AbstractFlatGamePainter {
  protected int _cachedCellSize;

  @Override
  public void init(GameScreenLayout gameScreenLayout, FlatGame game) {
    super.init(gameScreenLayout, game);
    _cachedCellSize = gameScreenLayout.getFieldRect().width() / game.getWidth();
    gameScreenLayout.roundFieldRect(_cachedCellSize, _cachedCellSize);
  }

  @Override
  public int getCenterX(int col, int row, FieldId cellField) {
    final int x;

    if(cellField == FieldId.NextField) {
      x = _cachedNextFieldCenterX + _cachedCellSize * col;
    }else {
      x = _cachedFieldRect.left + _cachedCellSize * col + (_cachedCellSize >> 1);
    }

    return x;
  }

  @Override
  public int getCenterY(int col, int row, FieldId cellField) {
    final int y;

    if(cellField == FieldId.NextField) {
      y = _cachedNextFieldCenterY + _cachedCellSize * row;
    }else {
      y = _cachedFieldRect.top + _cachedCellSize * row + (_cachedCellSize >> 1);
    }

    return y;
  }

  public void drawShapeContour(Canvas c, FlatGame game, boolean isValid, float dxFactor, float rotateFactor) {
    drawTransformedShapeContour(c, game, isValid, dxFactor * _cachedCellSize, rotateFactor * 90);
  }
}
