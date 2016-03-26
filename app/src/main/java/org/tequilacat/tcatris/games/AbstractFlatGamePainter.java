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

  private Path _shapeContourPath = new Path();
  private Object _shapeSignature = null;
  private Paint _shapeContourPaint = new Paint();

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

  public void drawShapeContour(Canvas c, int centerX, int centerY, boolean isValid, float rotateByDegrees) {
    _shapeContourPaint.setColor(isValid ?
        VisualResources.Defaults.DYN_SHAPE_STROKE_VALID : VisualResources.Defaults.DYN_SHAPE_STROKE_INVALID);

    if (!_shapeContourPath.isEmpty()) {
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
   * Modifies shapeContourPath to represent given shape
   * @param shape
   * @param shapeContourPath
   */
  protected abstract void updateCurrentShapeContour(FlatShape shape, Path shapeContourPath);


  /**
   * creates shape contour if needed (if current shape geometry differs)
   */
  public void updateCurrentShapeContour(FlatShape fallingShape) {

    if (fallingShape == null) {
      //Debug.print("updateCurrentShapeContour: nullify");
//      _shapeContourPath = null;
      _shapeContourPath.reset();

    } else if(!fallingShape.signatureEquals(_shapeSignature)) {
      //Debug.print("updateCurrentShapeContour: recreate path");
      _shapeSignature = fallingShape.generateSignature();
      _shapeContourPath.reset();
      updateCurrentShapeContour(fallingShape, _shapeContourPath);
    }
  }

}
