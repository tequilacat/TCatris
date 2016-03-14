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
 * Created by avo on 29.02.2016.
 */
public class FlatRectGamePainter extends AbstractFlatGamePainter {
  private Paint _cellPainter = new Paint();

  private final boolean _paintFieldBg = true;

  // TODO make distinctive colors bright ones and thus distinguishable from their darker variants
  private static final int[] _distinctiveColors = new int[]{
          ColorCodes.lightGray,
          ColorCodes.red, ColorCodes.blue, ColorCodes.magenta, ColorCodes.orange, ColorCodes.green,
          ColorCodes.darkRed, ColorCodes.darkGreen, ColorCodes.blue, ColorCodes.cyan, ColorCodes.purple, ColorCodes.orange, ColorCodes.lightBrown
  };

  private static final int[] _darkerColors;

  static {
    _darkerColors = new int[_distinctiveColors.length];

    for(int i = 0; i < _darkerColors.length; i++){
      _darkerColors[i] = ColorCodes.darken(_distinctiveColors[i], 0.2f);
    }
  }

  public static int getTypeColor(int cellType) {
    return _distinctiveColors[cellType];
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

    } else { // settled
      Ui.fillRect(c, x, y, cellSize - 1, cellSize - 1, cellColor);

      int innerSize = cellSize * 6 / 10;
      int margin = (cellSize - innerSize) >> 1;// fast /2
      Ui.fillRect(c, x+margin, y+margin, innerSize, innerSize, _darkerColors[state]);

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
