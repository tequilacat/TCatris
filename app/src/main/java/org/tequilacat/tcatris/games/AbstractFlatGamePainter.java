package org.tequilacat.tcatris.games;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;

import org.tequilacat.tcatris.core.ABrickGame;
import org.tequilacat.tcatris.core.GameScreenLayout;
import org.tequilacat.tcatris.core.VisualResources;

/**
 * Draws cells on field.
 * Created by avo on 29.02.2016.
 */
public abstract class AbstractFlatGamePainter {

  private GameScreenLayout _gameScreenLayout;

  private Path _shapeContourPath;
  private Object _shapeSignature = null;
  private Paint _shapeContourPaint = new Paint();

  private static final float CONTOUR_FACTOR = 1.1f;

  public AbstractFlatGamePainter(){
    float strokeWidth = VisualResources.Defaults.DYN_SHAPE_STROKE_WIDTH;

    _shapeContourPaint.setStrokeWidth(strokeWidth);
    _shapeContourPaint.setStyle(Paint.Style.STROKE);
    _shapeContourPaint.setPathEffect(new DashPathEffect(new float[]{
        strokeWidth * 7, strokeWidth * 3}, 0));
  }

  /**
   * stores size and calculates all things dependent on cell size
   * @param gameScreenLayout screen view_scores
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
  public abstract void paintCellPix(Canvas c, int x, int y, int state, ABrickGame.CellState cellState);


  /**
   * paints background of field if needed
   * @param g
   */
  public abstract void paintFieldBackground(Canvas g);

  /**
   * Returns color of field.
   * Defaults to color of cell type #0, ABrickGame.getTypeColor(0),
   * may be overwritten in implementations
   * @return color of field
   */
  protected abstract int getFieldBackground();

  public void drawShapeContour(Canvas c, int centerX, int centerY, boolean isValid, float rotateByDegrees) {
    _shapeContourPaint.setColor(isValid ?
        VisualResources.Defaults.DYN_SHAPE_STROKE_VALID : VisualResources.Defaults.DYN_SHAPE_STROKE_INVALID);

    if (_shapeContourPath != null) {
      c.save();
      c.clipRect(getGameScreenLayout().getFieldRect());
      c.translate(centerX, centerY);
      if (rotateByDegrees != 0) {
        c.rotate(rotateByDegrees);
      }
      c.drawPath(_shapeContourPath, _shapeContourPaint);
      c.restore();
    }
  }

  /**
   * creates shape contour if needed (if current shape geometry differs
   */
  protected Path updateCurrentShapeContour(FlatShape fallingShape) {
    if (fallingShape == null) {
      //Debug.print("updateCurrentShapeContour: nullify");
      _shapeContourPath = null;

    } else if(!fallingShape.signatureEquals(_shapeSignature)) {
      //Debug.print("updateCurrentShapeContour: recreate path");
      _shapeSignature = fallingShape.generateSignature();
      _shapeContourPath = new Path();
      int cx = fallingShape.getCenterX(), cy = fallingShape.getCenterY();
      int cellSize = (int)(getGameScreenLayout().getCellSize() * CONTOUR_FACTOR); // factor X 2
      int x0 = -cellSize >> 1, y0 = x0;

      for(int i = 0; i < fallingShape.size(); i++) {
        int cellX = (fallingShape.getX(i) - cx) * cellSize + x0;
        int cellY = (fallingShape.getY(i) - cy) * cellSize + y0;

        _shapeContourPath.addRect(cellX, cellY, cellX + cellSize, cellY + cellSize, Path.Direction.CW);
      }
    }

    return _shapeContourPath;
  }

}
