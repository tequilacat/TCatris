package org.tequilacat.tcatris.games;

import android.graphics.Canvas;

import org.tequilacat.tcatris.core.GameScreenLayout;
import org.tequilacat.tcatris.core.Tetris;
import org.tequilacat.tcatris.core.Ui;

/**
 * Draws cells on field.
 * Created by avo on 29.02.2016.
 */
public abstract class AbstractFlatGamePainter {

  private GameScreenLayout _gameScreenLayout;

  /**
   * stores size and calculates all things dependent on cell size
   * @param gameScreenLayout screen layout
   */
  public void init(GameScreenLayout gameScreenLayout){
    _gameScreenLayout = gameScreenLayout;
  }

  protected GameScreenLayout getGameScreenLayout() {
    return _gameScreenLayout;
  }

//  public int getCellSize() {
//    return _cellSize;
//  }

  /**
   * draws cell of state on canvas
   * @param c
   * @param x
   * @param y
   * @param state
   * @param cellState
   */
  public abstract void paintCellPix(Canvas c, int x, int y, int state, Tetris.CellState cellState);


  /**
   * paints background of field if needed
   * @param g
   */
  public abstract void paintFieldBackground(Canvas g);

  /**
   * paints next shape in specified bounds
   * @param g
   * @param shape
   * @param nextFigX
   * @param nextFigY
   * @param nextFigWidth
   * @param nextFigHeight
   */
  public void paintNext(Canvas g, FlatShape shape, int nextFigX, int nextFigY, int nextFigWidth, int nextFigHeight) {
    Ui.fillRect(g, nextFigX, nextFigY, nextFigWidth, nextFigHeight, getFieldBackground());

    int cellSize = getGameScreenLayout().getCellSize();
    // find out max leftToCenter and rightToCenter
    int maxLeft = 0, maxUp = 0;
    for (int i = 0; i < shape.size(); i++) {
      int x = shape.getX(i), y = shape.getY(i);
      if (maxLeft < -x) maxLeft = -x;
      if (maxUp < y) maxUp = y;
    }

    // int nextX = myNextShapeWindow.x + 2, nextY = myNextShapeWindow.y + 2;
    for (int i = 0; i < shape.size(); i++) {
      int x = shape.getX(i), y = shape.getY(i);
      paintCellPix(g, nextFigX + (x - maxLeft) * cellSize,
        nextFigY + (maxUp - y) * cellSize,
        shape.getCellType(i), Tetris.CellState.FALLING);
    }
  }

  /**
   * Returns color of field.
   * Defaults to color of cell type #0, Tetris.getTypeColor(0),
   * may be overwritten in implementations
   * @return color of field
   */
  protected abstract int getFieldBackground();

}
