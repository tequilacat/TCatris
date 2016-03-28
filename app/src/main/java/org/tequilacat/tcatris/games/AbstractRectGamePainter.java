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

  public void drawShapeContour(Canvas c, FlatGame game, boolean isValid, float dx, float rotateByDegrees) {

    if (!getShapeContourPath().isEmpty()) {
      getShapeContourPaint().setColor(isValid ?
          VisualResources.Defaults.DYN_SHAPE_STROKE_VALID : VisualResources.Defaults.DYN_SHAPE_STROKE_INVALID);

      c.save();
      c.clipRect(_cachedFieldRect);

      FlatShape shape = game.getCurrentShape();
      int cx = shape.getCenterX(), cy = shape.getCenterY();
      c.translate(getCenterX(cx, cy, FieldId.GameField) + (int) (dx * _cachedCellSize),
          getCenterY(cx, cy, FieldId.GameField));

      if (rotateByDegrees != 0) {
        c.rotate(rotateByDegrees);
      }
      c.drawPath(getShapeContourPath(), getShapeContourPaint());
      c.restore();
    }
  }
}
