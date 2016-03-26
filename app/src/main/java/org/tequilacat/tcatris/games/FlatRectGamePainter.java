package org.tequilacat.tcatris.games;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import org.tequilacat.tcatris.core.ABrickGame;
import org.tequilacat.tcatris.core.ColorCodes;
import org.tequilacat.tcatris.core.GameConstants;
import org.tequilacat.tcatris.core.GameScreenLayout;
import org.tequilacat.tcatris.core.Ui;
import org.tequilacat.tcatris.core.VisualResources;

/**
 * Implementation of painter drawing flat color squares
 */
public class FlatRectGamePainter extends AbstractFlatGamePainter {
  private Paint _cellPainter = new Paint();

  public FlatRectGamePainter() {
    _cellPainter.setStyle(Paint.Style.STROKE);
  }

  public static int getTypeColor(int cellType) {
    return ColorCodes.getDistinctColor(cellType - 1, ColorCodes.Lightness.Normal);
  }

  @Override
  public void paintCellPix(Canvas c, int x, int y, int state, ABrickGame.CellState cellState) {
    final int cellSize = getGameScreenLayout().getCellSize();
    int cellColor = getTypeColor(state);

    if (state == FlatGame.EMPTY) {
      // nothing

    } else if (cellState == ABrickGame.CellState.FALLING) {
      Ui.fillRect(c, x, y, cellSize - 1, cellSize - 1, cellColor);

    } else if (cellState == ABrickGame.CellState.SQUEEZED) {
      Ui.drawRect(c, x, y, cellSize - 1, cellSize - 1, cellColor);
      Ui.fillRect(c, x + 1, y + 1, cellSize - 3, cellSize - 3, VisualResources.Defaults.FIELD_BG_COLOR);

    } else if (cellState == ABrickGame.CellState.FALLEN_SHADOW) {
      int innerSize = cellSize * 6 / 10;
      int margin = (cellSize - innerSize) >> 1;// fast /2
      _cellPainter.setColor(ColorCodes.getDistinctColor(state - 1, ColorCodes.Lightness.Contrast));
      _cellPainter.setStrokeWidth(VisualResources.Defaults.FALLEN_SHADOW_STROKE_WIDTH);
      c.drawRect(x + margin, y + margin, x + margin + innerSize, y + margin + innerSize, _cellPainter);
      //Ui.drawRect(c, x + margin, y + margin, innerSize, innerSize, blockColor);

    } else if (cellState == ABrickGame.CellState.SETTLED) {
      Ui.fillRect(c, x, y, cellSize - 1, cellSize - 1, cellColor);

      int innerSize = cellSize * 6 / 10;
      int margin = (cellSize - innerSize) >> 1;// fast /2
      final int blockColor = ColorCodes.getDistinctColor(state - 1, ColorCodes.Lightness.Contrast);
      Ui.fillRect(c, x + margin, y + margin, innerSize, innerSize, blockColor);

//      _cellPainter.setColor(ColorCodes.black);
//      x += 3;
//      y += 3;
//      c.drawLine(x, y, x + cellSize - 8, y, _cellPainter);
//      c.drawLine(x, y, x, y + cellSize - 8, _cellPainter);
    }
  }

  @Override
  public void paintFieldBackground(Canvas g) {
    // do nothing or fill the rect

    final GameScreenLayout layout = getGameScreenLayout();
    final Rect fieldRect = layout.getFieldRect();
    final int // fieldWidth = fieldRect.width(), fieldHeight = fieldRect.height(),
        right = fieldRect.right, left = fieldRect.left,
        top = fieldRect.top, bottom = fieldRect.bottom;

    Ui.fillRect(g, layout.getFieldRect(), VisualResources.Defaults.FIELD_BG_COLOR);

    _cellPainter.setColor(VisualResources.Defaults.FIELD_LINE_COLOR);
    _cellPainter.setStrokeWidth(0);

    final int cellSize = getGameScreenLayout().getCellSize();

    for (int x = left + cellSize; x < right; x += cellSize) {
      g.drawLine(x, top, x, bottom, _cellPainter);
    }

    for (int y = top + cellSize; y < bottom; y += cellSize) {
      g.drawLine(left, y, right, y, _cellPainter);
    }
  }

  @Override
  protected void updateCurrentShapeContour(FlatShape fallingShape, Path shapeContourPath) {
//_shapeContourPath = new Path();
    int cx = fallingShape.getCenterX(), cy = fallingShape.getCenterY();
    int cellSize = (int)(getGameScreenLayout().getCellSize() * GameConstants.CONTOUR_FACTOR); // factor X 2
    int x0 = -cellSize >> 1, y0 = x0;

    for(int i = 0; i < fallingShape.size(); i++) {
      int cellX = (fallingShape.getX(i) - cx) * cellSize + x0;
      int cellY = (fallingShape.getY(i) - cy) * cellSize + y0;

      shapeContourPath.addRect(cellX, cellY, cellX + cellSize, cellY + cellSize, Path.Direction.CW);
    }
  }
}
