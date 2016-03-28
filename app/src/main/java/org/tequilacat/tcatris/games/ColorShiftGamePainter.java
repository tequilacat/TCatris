package org.tequilacat.tcatris.games;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import org.tequilacat.tcatris.core.DragAxis;
import org.tequilacat.tcatris.core.DragSensitivity;
import org.tequilacat.tcatris.core.DynamicState;
import org.tequilacat.tcatris.core.GameScreenLayout;
import org.tequilacat.tcatris.core.VisualResources;

/**
 * Extends flat painter but draws falling shape differently, displaying state of active color shift
 */
public class ColorShiftGamePainter extends FlatRectGamePainter {
  private static final Paint _shiftedCellFill = new Paint();
  private static final Paint _shiftedCellStroke = new Paint();
  private static final Path _arrowPath = new Path();

  public ColorShiftGamePainter() {
    _shiftedCellFill.setStyle(Paint.Style.FILL);

    _shiftedCellStroke.setStyle(Paint.Style.STROKE);
    _shiftedCellStroke.setColor(VisualResources.Defaults.FIELD_LINE_COLOR);
  }

/*
int cellSize = getGameScreenLayout().getCellSize();
    float x0 = -cellSize / 2, y0 = -cellSize / 2;
    _shiftedCellPath.reset();// = new Path();
    _shiftedCellPath.moveTo(x0, y0);
    _shiftedCellPath.lineTo(x0 + cellSize, y0);
    _shiftedCellPath.lineTo(x0 + cellSize * 1.2f, 0);
    _shiftedCellPath.lineTo(x0 + cellSize, y0 + cellSize);
    _shiftedCellPath.lineTo(x0, y0 + cellSize);
    _shiftedCellPath.close();
*/

  @Override
  public void init(GameScreenLayout gameScreenLayout) {
    super.init(gameScreenLayout);

    _arrowPath.reset();
    _arrowPath.moveTo(0.3f * _cachedCellSize, 0);
    _arrowPath.lineTo(-0.2f * _cachedCellSize, -0.3f * _cachedCellSize);
    _arrowPath.lineTo(-0.2f * _cachedCellSize, 0.3f * _cachedCellSize);
    _arrowPath.close();

    // set proportional line width
    _shiftedCellStroke.setStrokeWidth(_cachedCellSize / 20);
  }

  @Override
  public void paintFallingShape(Canvas c, FlatGame game, int finalFallingShapeY, DynamicState dynamicState) {
    super.paintFallingShape(c, game, finalFallingShapeY, dynamicState);

    ColorShiftGame colorGame = (ColorShiftGame) game;

    if (colorGame.isColorShifting()) {
      //if(false) {
      float value = dynamicState.getValue(DragAxis.ROTATE.ordinal());

      if (Math.abs(value) >= DragSensitivity.COLORSHIFT.MIN) {
        drawShiftedShape(c, colorGame.getCurrentShape(),
            colorGame.getGameType() == ColorShiftGame.ColorGameType.SHIFT_VERTICALLY, value);
      }
    }
  }

  // Used in drawShiftedShape
  private Rect _shapeBounds = new Rect();


  private void drawShiftedShape(Canvas c, FlatShape fallingShape, boolean isVerticalShift, float value) {
    final int cellSize = _cachedCellSize;
    // get shape bounds
    fallingShape.getBounds(_shapeBounds);
    final Rect fieldRect = _cachedFieldRect;

    // cell coords
    int shapeX = _shapeBounds.left * cellSize + fieldRect.left;
    int shapeY = _shapeBounds.top * cellSize + fieldRect.top;
    int shapeW = _shapeBounds.width() * cellSize;
    int shapeH = _shapeBounds.height() * cellSize;


    // Debug.print("Fill " + shapeX + "x" + shapeY + " [" + shapeW + ", " + shapeH + "]");

    c.save();
    c.clipRect(shapeX, shapeY, shapeX + shapeW, shapeY + shapeH);

    // whether the forward shift should be done from 0 to size-1
    boolean isShapeCoDirected = fallingShape.getX(0) < fallingShape.getX(fallingShape.size() - 1)
        || fallingShape.getY(0) < fallingShape.getY(fallingShape.size() - 1);

    // find from which we display
    boolean from0 = isShapeCoDirected != (value > 0);
    boolean isForward = value > 0;
    int angle;
    float dx, dy;

    if (isVerticalShift) {
      angle = isForward ? 90 : -90;
      dx = 0;
      dy = isForward ? cellSize : -cellSize;
    } else {
      angle = isForward ? 0 : 180;
      dx = isForward ? cellSize : -cellSize;
      dy = 0;
    }

    float absValue = Math.abs(value);
    //float markerRadius = cellSize / 4;

    //float centerX = 0, centerY = 0;// always assigned in cycle,

    for (int i = 0; i <= fallingShape.size(); i++) {
      int cellIndex = from0 ? i : (fallingShape.size() - 1 - i);

      // fix extra last cell
      if (cellIndex < 0) {
        cellIndex = fallingShape.size() - 1;
      } else if (cellIndex >= fallingShape.size()) {
        cellIndex = 0;
      }

      _shiftedCellFill.setColor(FlatRectGamePainter.getTypeColor(fallingShape.getCellType(cellIndex)));

      if (i == 0) {
        int x = fallingShape.getX(cellIndex), y = fallingShape.getY(cellIndex);
        // translate to center of the cell
        c.translate(fieldRect.left + x * cellSize + cellSize / 2 + dx * absValue,
            fieldRect.top + y * cellSize + cellSize / 2 + dy * absValue);

//        centerX = fieldRect.left + x * cellSize + cellSize / 2 + dx * absValue;
//        centerY = fieldRect.top + y * cellSize + cellSize / 2 + dy * absValue;

      } else {
        c.translate(-dx, -dy);

//        centerX -= dx;
//        centerY -= dy;
      }

      // small rect in center
//      c.drawRect(centerX, centerY, centerX + markerRadius, centerY + markerRadius, _shiftedCellFill);
//      c.drawRect(centerX, centerY, centerX + markerRadius, centerY + markerRadius, _shiftedCellStroke);

      // simple offset rects fully covering
//      c.drawRect(-cellSize / 2, -cellSize / 2, cellSize / 2, cellSize / 2, _shiftedCellFill);
//      c.drawRect(-cellSize / 2, -cellSize / 2, cellSize / 2, cellSize / 2, _shiftedCellStroke);


      // arrow-like offset rectangular shapes
      if (angle != 0) {
        c.save();
        c.rotate(angle);
      }

//      c.drawPath(_shiftedCellPath, _shiftedCellFill);
//      c.drawPath(_shiftedCellPath, _shiftedCellStroke);
      c.drawPath(_arrowPath, _shiftedCellFill);
      c.drawPath(_arrowPath, _shiftedCellStroke);

      if (angle != 0) {
        c.restore();
      }
    }

    c.restore();
  }

}
