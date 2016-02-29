package org.tequilacat.tcatris.games;

import android.graphics.Canvas;
import android.graphics.Paint;

import org.tequilacat.tcatris.core.ColorCodes;
import org.tequilacat.tcatris.core.Tetris;
import org.tequilacat.tcatris.core.Ui;

/**
 * Created by avo on 29.02.2016.
 */
public class FlatRectGamePainter extends AbstractFlatGamePainter {
  private Paint _cellPainter = new Paint();

  @Override
  public void paintCellPix(Canvas c, int x, int y, int state, Tetris.CellState cellState) {
    final int cellSize = getGameScreenLayout().getCellSize();

    int cellColor = Tetris.getTypeColor(state);

    if (state == FlatGame.EMPTY || cellState == Tetris.CellState.FALLING) {
      Ui.fillRect(c, x, y, cellSize - 1, cellSize - 1, cellColor);
      //Debug.print("Falling:");
    } else if (cellState == Tetris.CellState.SQUEEZED) {
      Ui.drawRect(c, x, y, cellSize - 1, cellSize - 1, cellColor);
      Ui.fillRect(c, x + 1, y + 1, cellSize - 3, cellSize - 3, getFieldBackground());
    } else { // settled
      Ui.fillRect(c, x, y, cellSize - 1, cellSize - 1, cellColor);

      _cellPainter.setColor(ColorCodes.black);
      x += 3;
      y += 3;
      c.drawLine(x, y, x + cellSize - 8, y, _cellPainter);
      c.drawLine(x, y, x, y + cellSize - 8, _cellPainter);
    }
  }
}
