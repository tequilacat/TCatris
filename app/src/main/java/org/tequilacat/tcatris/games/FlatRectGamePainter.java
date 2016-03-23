package org.tequilacat.tcatris.games;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import org.tequilacat.tcatris.core.ColorCodes;
import org.tequilacat.tcatris.core.GameScreenLayout;
import org.tequilacat.tcatris.core.Tetris;
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

  private final boolean _paintFieldBg = true;

  public static int getTypeColor(int cellType) {
    return ColorCodes.getDistinctColor(cellType - 1, ColorCodes.Lightness.Normal);
        //_distinctiveColors[cellType];
  }

  @Override
  public int getFieldBackground() {
    return VisualResources.Defaults.FIELD_BG_COLOR;
  }

  @Override
  public void paintCellPix(Canvas c, int x, int y, int state, Tetris.CellState cellState) {
    final int cellSize = getGameScreenLayout().getCellSize();
    int cellColor = getTypeColor(state);

    if (state == FlatGame.EMPTY) {
      // nothing

    } else if (cellState == Tetris.CellState.FALLING) {
      Ui.fillRect(c, x, y, cellSize - 1, cellSize - 1, cellColor);

    } else if (cellState == Tetris.CellState.SQUEEZED) {
      Ui.drawRect(c, x, y, cellSize - 1, cellSize - 1, cellColor);
      Ui.fillRect(c, x + 1, y + 1, cellSize - 3, cellSize - 3, getFieldBackground());

    } else if (cellState == Tetris.CellState.FALLEN_SHADOW) {
      int innerSize = cellSize * 6 / 10;
      int margin = (cellSize - innerSize) >> 1;// fast /2
      _cellPainter.setColor(ColorCodes.getDistinctColor(state - 1, ColorCodes.Lightness.Contrast));
      _cellPainter.setStrokeWidth(VisualResources.Defaults.FALLEN_SHADOW_STROKE_WIDTH);
      c.drawRect(x + margin, y + margin, x + margin + innerSize, y + margin + innerSize, _cellPainter);
      //Ui.drawRect(c, x + margin, y + margin, innerSize, innerSize, blockColor);

    } else if (cellState == Tetris.CellState.SETTLED) {
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
    if (_paintFieldBg) {
      final GameScreenLayout layout = getGameScreenLayout();
      final Rect fieldRect = layout.getFieldRect();
      final int fieldWidth = fieldRect.width(), fieldHeight = fieldRect.height(),
        right = fieldRect.right, left = fieldRect.left,
        top = fieldRect.top, bottom = fieldRect.bottom;
      //final int fieldWidth = getGameScreenLayout().getFieldRect().width(),
      //        fieldHeight = getGameScreenLayout().getFieldRect().height();

      Ui.fillRect(g, layout.getFieldRect(), getFieldBackground());

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

  }
}
