package org.tequilacat.tcatris.games;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import org.tequilacat.tcatris.core.ABrickGame;
import org.tequilacat.tcatris.core.AbstractGamePainter;
import org.tequilacat.tcatris.core.GameConstants;
import org.tequilacat.tcatris.core.GameScreenLayout;
import org.tequilacat.tcatris.core.Ui;
import org.tequilacat.tcatris.core.VisualResources;

/**
 * Implementation of painter drawing flat color squares
 */
public class FlatRectGamePainter extends AbstractRectGamePainter {
  private Paint _cellPainter = new Paint();

  public FlatRectGamePainter() {
    _cellPainter.setStyle(Paint.Style.STROKE);
  }

  @Override
  public void paintCellPix(Canvas c, int x, int y, int state, ABrickGame.CellState cellState) {
    final int cellSize = _cachedCellSize, halfCellSize = cellSize >> 1;
    x -= halfCellSize;
    y -= halfCellSize;
    int cellColor = getTypeColor(state);

    if (state == ABrickGame.EMPTY_CELL_TYPE) {
      // nothing

    } else if (cellState == ABrickGame.CellState.FALLING) {
      Ui.fillRect(c, x, y, cellSize - 1, cellSize - 1, cellColor);

    } else if (cellState == ABrickGame.CellState.SQUEEZED) {
      Ui.drawRect(c, x, y, cellSize - 1, cellSize - 1, cellColor);
      Ui.fillRect(c, x + 1, y + 1, cellSize - 3, cellSize - 3, _colorPalette.FIELD_BG_COLOR);

    } else if (cellState == ABrickGame.CellState.FALLEN_SHADOW) {
      int innerSize = cellSize * 6 / 10;
      int margin = (cellSize - innerSize) >> 1;// fast /2
      _cellPainter.setColor(AbstractGamePainter.getTypeColor(state, true));
      _cellPainter.setStrokeWidth(VisualResources.Defaults.FALLEN_SHADOW_STROKE_WIDTH);
      c.drawRect(x + margin, y + margin, x + margin + innerSize, y + margin + innerSize, _cellPainter);
      //Ui.drawRect(c, x + margin, y + margin, innerSize, innerSize, blockColor);

    } else if (cellState == ABrickGame.CellState.SETTLED) {
      Ui.fillRect(c, x, y, cellSize - 1, cellSize - 1, cellColor);

      int innerSize = cellSize * 6 / 10;
      int margin = (cellSize - innerSize) >> 1;// fast /2
      final int blockColor = AbstractGamePainter.getTypeColor(state, true);
      Ui.fillRect(c, x + margin, y + margin, innerSize, innerSize, blockColor);
    }
  }

  @Override
  public void paintField(Canvas g, FlatGame game) {
    // do nothing or fill the rect

    final GameScreenLayout layout = getGameScreenLayout();
    final Rect fieldRect = layout.getFieldRect();
    final int // fieldWidth = fieldRect.width(), fieldHeight = fieldRect.height(),
        right = fieldRect.right, left = fieldRect.left,
        top = fieldRect.top, bottom = fieldRect.bottom;

    Ui.fillRect(g, layout.getFieldRect(), _colorPalette.FIELD_BG_COLOR);

    _cellPainter.setColor(VisualResources.Defaults.FIELD_LINE_COLOR);
    _cellPainter.setStrokeWidth(0);

    final int cellSize = _cachedCellSize;// getGameScreenLayout().getCellSize();

    for (int x = left + cellSize; x < right; x += cellSize) {
      g.drawLine(x, top, x, bottom, _cellPainter);
    }

    for (int y = top + cellSize; y < bottom; y += cellSize) {
      g.drawLine(left, y, right, y, _cellPainter);
    }

    // paint field contents here
    int pixY = fieldRect.top + (_cachedCellSize >> 1);
    int gameRows = game.getHeight(), gameCols = game.getWidth();

    for (int y = 0; y < gameRows; y++) {
      int pixX = fieldRect.left + (_cachedCellSize >> 1);

      for (int x = 0; x < gameCols; x++) {
        // settled
        int cellValue = game.getCellValue(x, y);

        if (cellValue != ABrickGame.EMPTY_CELL_TYPE) {
          paintCellPix(g, pixX, pixY, cellValue,
              game.isSqueezable(x, y) ? ABrickGame.CellState.SQUEEZED : ABrickGame.CellState.SETTLED);
        }
        pixX += cellSize;
      }

      pixY += cellSize;
    }
  }

  @Override
  protected void updateCurrentShapeContour(FlatShape fallingShape, Path shapeContourPath) {
    int cx = fallingShape.getCenterX(), cy = fallingShape.getCenterY();
    int cellSize = (int) (_cachedCellSize * GameConstants.CONTOUR_FACTOR);
    //int x0 = -cellSize >> 1, y0 = x0;
    int delta = cellSize >> 1;

    for (int i = 0; i < fallingShape.size(); i++) {
      int cellX = (fallingShape.getX(i) - cx) * cellSize - delta;
      int cellY = (fallingShape.getY(i) - cy) * cellSize - delta;

      shapeContourPath.addRect(cellX, cellY, cellX + cellSize, cellY + cellSize, Path.Direction.CW);
    }
  }
}
