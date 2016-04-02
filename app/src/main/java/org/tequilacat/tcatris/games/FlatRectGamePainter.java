package org.tequilacat.tcatris.games;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import org.tequilacat.tcatris.core.ABrickGame;
import org.tequilacat.tcatris.core.AbstractGamePainter;
import org.tequilacat.tcatris.core.ColorCodes;
import org.tequilacat.tcatris.core.GameConstants;
import org.tequilacat.tcatris.core.GameScreenLayout;
import org.tequilacat.tcatris.core.Ui;
import org.tequilacat.tcatris.core.VisualResources;

/**
 * Implementation of painter drawing flat color squares
 */
public class FlatRectGamePainter extends AbstractRectGamePainter {
  private Paint _cellPainter = new Paint();
  private int _frameStrokeWidth;

  public FlatRectGamePainter() {
    _cellPainter.setStyle(Paint.Style.STROKE);
  }

  @Override
  public void init(GameScreenLayout gameScreenLayout, ABrickGame game) {
    super.init(gameScreenLayout, game);
    _frameStrokeWidth = _cachedCellSize / 30; // 1/15 of side
    // make sure it's odd, no less than 3
    if (_frameStrokeWidth < 3) {
      _frameStrokeWidth = 3;
    }
    _frameStrokeWidth |= 1; // make sure it's odd
  }

  private void paintSquare(Canvas c, int centerX, int centerY, int cellSide, int state) {
    int cellColor = getTypeColor(state), darker = getTypeColor(state, ColorCodes.Lightness.Darker),
        lighter = getTypeColor(state, ColorCodes.Lightness.Lighter);

    int intHalf = cellSide >> 1;
    int x = centerX - intHalf;
    int y = centerY - intHalf;
    Ui.fillRect(c, x, y, cellSide, cellSide, cellColor);

    x += (_frameStrokeWidth >> 1);
    y += (_frameStrokeWidth >> 1);
    cellSide -= _frameStrokeWidth;

    // shadows
    _cellPainter.setStrokeWidth(_frameStrokeWidth);
    _cellPainter.setColor(lighter);
    c.drawLine(x, y, x + cellSide, y, _cellPainter);
    c.drawLine(x, y, x, y + cellSide, _cellPainter);

    _cellPainter.setColor(darker);
    c.drawLine(x + cellSide, y, x + cellSide, y + cellSide, _cellPainter);
    c.drawLine(x, y + cellSide, x + cellSide, y + cellSide, _cellPainter);
  }

  @Override
  public void paintCellPix(Canvas c, final int centerX, final int centerY, int state, ABrickGame.CellState cellState) {
    final int cellSize = _cachedCellSize, halfCellSize = cellSize >> 1;
    int x = centerX - halfCellSize;
    int y = centerY - halfCellSize;

    if (state == ABrickGame.EMPTY_CELL_TYPE) {
      // nothing
      _cellPainter.setStrokeWidth(_frameStrokeWidth);
      _cellPainter.setColor(_colorPalette.EMPTY_CELL_FRAME_COLOR);
      int innerHalf = (cellSize * 8 / 10) / 2;
      c.drawRect(centerX - innerHalf, centerY - innerHalf,
          centerX + innerHalf, centerY + innerHalf, _cellPainter);

    } else if (cellState == ABrickGame.CellState.FALLING) {
      paintSquare(c, centerX, centerY, cellSize, state);

    } else if (cellState == ABrickGame.CellState.SQUEEZED) {
      paintSquare(c, centerX, centerY, cellSize >> 1, state);

    } else if (cellState == ABrickGame.CellState.FALLEN_SHADOW) {

      int innerSize = cellSize * 8 / 10;
      int margin = (cellSize - innerSize) >> 1;// fast /2
      _cellPainter.setColor(AbstractGamePainter.getTypeColor(state, ColorCodes.Lightness.Contrast));
      _cellPainter.setStrokeWidth(VisualResources.Defaults.FALLEN_SHADOW_STROKE_WIDTH);
      c.drawRect(x + margin, y + margin, x + margin + innerSize, y + margin + innerSize, _cellPainter);

    } else if (cellState == ABrickGame.CellState.SETTLED) {
      paintSquare(c, centerX, centerY, cellSize, state);

      int innerSize = cellSize * 6 / 10;
      int margin = (cellSize - innerSize) >> 1;// fast /2
      final int blockColor = AbstractGamePainter.getTypeColor(state, ColorCodes.Lightness.Contrast);
      Ui.fillRect(c, x + margin, y + margin, innerSize, innerSize, blockColor);
    }
  }

  @Override
  public void paintField(Canvas g, FlatGame game) {
    // do nothing or fill the rect

    final GameScreenLayout layout = getGameScreenLayout();
    final Rect fieldRect = layout.getFieldRect();
//    final int // fieldWidth = fieldRect.width(), fieldHeight = fieldRect.height(),
//        right = fieldRect.right, left = fieldRect.left,
//        top = fieldRect.top, bottom = fieldRect.bottom;

    Ui.fillRect(g, layout.getFieldRect(), _colorPalette.FIELD_BG_COLOR);
    final int cellSize = _cachedCellSize;

    /*
    _cellPainter.setColor(VisualResources.Defaults.FIELD_LINE_COLOR);
    _cellPainter.setStrokeWidth(0);
    for (int x = left + cellSize; x < right; x += cellSize) {
      g.drawLine(x, top, x, bottom, _cellPainter);
    }

    for (int y = top + cellSize; y < bottom; y += cellSize) {
      g.drawLine(left, y, right, y, _cellPainter);
    }
  */

    // paint field contents here
    int pixY = fieldRect.top + (_cachedCellSize >> 1);
    int gameRows = game.getHeight(), gameCols = game.getWidth();

    for (int y = 0; y < gameRows; y++) {
      int pixX = fieldRect.left + (_cachedCellSize >> 1);

      for (int x = 0; x < gameCols; x++) {
        // settled
        int cellValue = game.getCellValue(x, y);
        paintCellPix(g, pixX, pixY, cellValue,
            game.isSqueezable(x, y) ? ABrickGame.CellState.SQUEEZED : ABrickGame.CellState.SETTLED);
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
