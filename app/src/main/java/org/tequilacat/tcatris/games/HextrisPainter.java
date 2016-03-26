package org.tequilacat.tcatris.games;

import android.graphics.Canvas;
import android.graphics.Path;

import org.tequilacat.tcatris.core.ABrickGame;

/**
 * Paints hexagonal hextris
 */
public class HextrisPainter extends AbstractFlatGamePainter {
  @Override
  public void paintCellPix(Canvas c, int x, int y, int state, ABrickGame.CellState cellState) {

  }

  @Override
  public void paintFieldBackground(Canvas g) {

  }

  @Override
  protected void updateCurrentShapeContour(FlatShape shape, Path shapeContourPath) {

  }
}
