// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package org.tequilacat.tcatris.games;

import android.graphics.Canvas;
import android.graphics.Rect;

import org.tequilacat.tcatris.core.DragAxis;
import org.tequilacat.tcatris.core.DynamicState;
import org.tequilacat.tcatris.core.GameDescriptor;
import org.tequilacat.tcatris.core.GameImpulse;
import org.tequilacat.tcatris.core.GameScreenLayout;
import org.tequilacat.tcatris.core.LayoutParameters;
import org.tequilacat.tcatris.core.Tetris;
import org.tequilacat.tcatris.core.Ui;
import org.tequilacat.tcatris.core.VisualResources;

import java.util.EnumSet;

public abstract class FlatGame extends Tetris {
  // int myCellSize;

  public static final int EMPTY = 0;

  protected int field[][];
  int _shapesThrown;
  private FlatShape myFallingShape;
  private FlatShape myNextShape;

  private AbstractFlatGamePainter _fieldPainter;

  protected FlatGame(GameDescriptor descriptor, AbstractFlatGamePainter fieldPainter) {
    super(descriptor);
    _fieldPainter = fieldPainter;
  }

  protected AbstractFlatGamePainter getGamePainter() {
    return _fieldPainter;
  }

  /**
   * resets game to initial step
   */
  public void init() {
    setScore(0);
    myFallingShape = null;
    myNextShape = null;
    _shapesThrown = 0;
    countStep();
    field = new int[getHeight()][getWidth()];
    for (int i = 0; i < getHeight(); i++) {
      for (int j = 0; j < getWidth(); j++)
        field[i][j] = EMPTY;

    }

  }

  private static EnumSet<GameImpulse> _CheckedImpulses = EnumSet.of(GameImpulse.MOVE_LEFT, GameImpulse.MOVE_RIGHT,
    GameImpulse.ROTATE_CW, GameImpulse.ROTATE_CCW);

  @Override
  public EnumSet<GameImpulse> getSupportedImpulses() {
    return _CheckedImpulses;
  }

  @Override
  public void addEffectiveImpulses(EnumSet<GameImpulse> actionSet) {
    FlatShape curShape = getCurrentShape();

    for (GameImpulse impulse : getSupportedImpulses()) {
      if(!actionSet.contains(impulse)) {
        FlatShape transformed = curShape.transformed(impulse);
        if (transformed != null && isShapePlaceable(transformed)) {
          actionSet.add(impulse);
        }
      }
    }
  }

  @Override
  public boolean doAction(GameImpulse impulse) {
    boolean moved = isEffective(impulse) && getCurrentShape().transform(impulse);

    if(moved){
      checkEffectiveImpulses();
    }

    return moved;
  }

  public int getCellValue(int i, int j) {
    return field[j][i];
  }

  /**
   * checks whether the given shape fits the field
   * @param aShape
   * @return
   */
  private boolean isShapePlaceable(FlatShape aShape) {
    // check for not out of bounds, not not over existing
    boolean canPlace = true;
    int rowCount = getHeight(), colCount = getWidth();

    for (int i = 0; i < aShape.size(); i++) {
      int x = aShape.getX(i), y = aShape.getY(i);
      // if( x<0 || y<0 || x>=getWidth() || y>=getHeight()
      if (x < 0 || x >= colCount // || y>=getHeight()
        || y >= rowCount
        || (y >= 0 && field[y][x] != EMPTY)) {
        canPlace = false;
        break;
      }
    }

    return canPlace;
  }

  /********************************
   * drops cur shape 1 level , or till bottom.
   *
   * @returns if really have dropped it any level.
   ******************************/
  @Override
  protected boolean dropCurrent(boolean tillBottom) {
    int downStep = 1;
    boolean dropped = false;

    if (tillBottom) {
      myFallingShape.moveBy(0, downStep);

      while (isShapePlaceable(myFallingShape)) {
        dropped = true;
        myFallingShape.moveBy(0, downStep);
      }
      myFallingShape.moveBy(0, -downStep);

    } else {
      // move one row down
      myFallingShape.moveBy(0, downStep);

      if (!isShapePlaceable(myFallingShape)) {
        // revert
        myFallingShape.moveBy(0, -downStep);
      } else {
        dropped = true;
      }
    }
    return dropped;
  }

  @Override
  protected boolean acquireFallenShape() {
    FlatShape shape = getCurrentShape();

    for (int i = 0; i < myFallingShape.size(); i++) {
      int x = shape.getX(i), y = shape.getY(i);
      if (x < 0 || y < 0 || x >= getWidth() || y >= getHeight()) {
        return false;
      }
      field[y][x] = shape.getCellType(i);
    }

    return true;
  }


  public FlatShape getNextShape() {
    return myNextShape;
  }

  public FlatShape getCurrentShape() {
    return myFallingShape;
  }

  /**
   * Called on each thrown shape to update stats like level, speed etc
   */
  private void countStep() {
    setLevel(Math.min(1 + _shapesThrown / 20, 10));
  }

  @Override
  protected boolean throwInNewShape() {
    _shapesThrown++;
    countStep();

    myFallingShape = myNextShape == null ? createNext() : myNextShape;
    myNextShape = createNext();
    myFallingShape.moveTo(getWidth() / 2, 0);
    return isShapePlaceable(myFallingShape);
  }


  protected abstract boolean isSqueezable(int i, int j);

  protected abstract FlatShape createNext();

  @Override
  public void layout(LayoutParameters layoutParams) {

    int MARGIN = VisualResources.Defaults.MARGIN_SIZE;
    int VERT_SPACING = MARGIN;

    //LayoutParameters layoutParams = new LayoutParameters();
    int screenWidth = layoutParams.GameArea.width(), screenHeight = layoutParams.GameArea.height();

    int glassWidth = this.getWidth(), glassHeight = this.getHeight();

    int nextFigWidth = this.getMaxNextWidth(), nextFigHeight = this.getMaxNextHeight();

    int nextFigMargin = VERT_SPACING;
    int width = screenWidth - MARGIN - MARGIN - VERT_SPACING - nextFigMargin * 2;
    int height = screenHeight - MARGIN - MARGIN;
    int cellSize = width / (glassWidth + nextFigWidth);

    if (cellSize * glassHeight > height) {
      cellSize = height / glassHeight;
    }

    int fieldX0 = MARGIN;
    int fieldY0 = MARGIN;

    int fieldWidth = cellSize * glassWidth;
    int fieldHeight = cellSize * glassHeight;


    // lay out next fig
    ///layoutParams.GameArea.left
    int nextShapeX = layoutParams.GameArea.left + MARGIN + fieldWidth + VERT_SPACING;
    int nextShapeWidth = nextFigMargin * 2 + nextFigWidth * cellSize;
    int nextShapeY = layoutParams.GameArea.top + MARGIN;
    int nextShapeHeight = nextFigMargin * 2 + nextFigHeight * cellSize;

    setGameScreenLayout(new GameScreenLayout(cellSize,
      layoutParams.GameArea.left + fieldX0, layoutParams.GameArea.top + fieldY0,
      fieldWidth, fieldHeight,
      nextShapeX, nextShapeY, nextShapeWidth, nextShapeHeight));
//        layoutParams.GameArea.left + myNextShapeX0, layoutParams.GameArea.top + myNextShapeY0,
//        this.getMaxShapeWidth() * cellSize, this.getMaxShapeHeight() * cellSize));

    _fieldPainter.init(getGameScreenLayout());
  }

  protected abstract int getMaxNextWidth();
  protected abstract int getMaxNextHeight();

  /**
   * paints game field
   * @param g canvas to draw to
   * @param dynamicState props of current move state
   */
  @Override
  public void paintField(Canvas g, DynamicState dynamicState) {
    Rect fieldRect = getGameScreenLayout().getFieldRect();
    int cellSize = getGameScreenLayout().getCellSize();
    int pixY = fieldRect.top;

    _fieldPainter.paintFieldBackground(g);

    for (int y = 0; y < getHeight(); y++) {
      int pixX = fieldRect.left;

      for (int x = 0; x < getWidth(); x++) {
        // settled
        _fieldPainter.paintCellPix(g, pixX, pixY, getCellValue(x, y),
          isSqueezable(x, y) ? CellState.SQUEEZED : CellState.SETTLED);
        pixX += cellSize;
      }

      pixY += cellSize;
    }

    if (getState() == ACTIVE && !canSqueeze()) {
      FlatShape shape = getCurrentShape();

      int centerX0 = fieldRect.left + shape.getCenterX() * cellSize;
      int centerY0 = fieldRect.top + shape.getCenterY() * cellSize;

      double dx = 0;
      float rotateFactor = 0; // 1 means 90', 0 means no rotate
      boolean isValid = true;
      boolean drawContour = false;

      DragAxis moveAxis = getMovementAxis();

      if(moveAxis!=null) {
        int pos = moveAxis.ordinal();

        if (dynamicState.isTracking(pos)) {
          dx = dynamicState.getValue(pos);
          drawContour = true;

          if (!dynamicState.isValid(pos)) {
            isValid = false;
            dx = 0;
          }
//        RectF rect = new RectF(centerX0, centerY0, centerX0+cellSize, centerY0 + cellSize);
          //g.drawArc(rect, 0, curValue*360, true, tmpPaint);
        }
      }

      DragAxis rotationAxis = getRotationAxis();

      if(rotationAxis!=null) {
        int pos = DragAxis.ROTATE.ordinal();

        if (dynamicState.isTracking(pos)) {
          rotateFactor = dynamicState.getValue(pos);
          drawContour = true;

          if (!dynamicState.isValid(pos)) {
            isValid = false;
            rotateFactor = 0;
          }
        }
      }

      if (drawContour) {
        // TODO only update shape contour when it's thrown in or rotated
        _fieldPainter.updateCurrentShapeContour(getCurrentShape());
        _fieldPainter.drawShapeContour(g,
            centerX0 + cellSize / 2 + (int) (dx * cellSize), centerY0 + cellSize / 2,
            isValid, rotateFactor * 90);
      }

      paintFallingShape(g, dynamicState);
    }
  }

  /**
   * @param c
   * @param dynamicState current state of shape offset along supported axis
   */
  protected void paintFallingShape(Canvas c, DynamicState dynamicState) {
    FlatShape shape = getCurrentShape();
    int cols = getWidth(), rows = getHeight();
    Rect fieldRect = getGameScreenLayout().getFieldRect();
    int cellSize = getGameScreenLayout().getCellSize();

    for (int i = 0, len = shape.size(); i < len; i++) {
      int x = shape.getX(i), y = shape.getY(i);
      if (x >= 0 && x < cols && y >= 0 && y < rows) {
        _fieldPainter.paintCellPix(c, fieldRect.left + x * cellSize,
            fieldRect.top + y * cellSize, shape.getCellType(i), CellState.FALLING);
      }
    }
  }

  /**
   *
   * @return axis used to move shape along
   */
  public DragAxis getMovementAxis() {
    return DragAxis.HORIZONTAL;
  }

  /**
   *
   * @return rotation axis
   */
  public DragAxis getRotationAxis() {
    return DragAxis.ROTATE;
  }

  /**
   * paints next figure in given rect
   * @param g canwas to draw to
   */
  @Override
  public void paintNext(Canvas g) {
    Rect nextRect = getGameScreenLayout().getNextShapeRect();
    Ui.fillRect(g, nextRect, _fieldPainter.getFieldBackground());

    int cellSize = getGameScreenLayout().getCellSize();
    FlatShape shape = getNextShape();
    Rect bounds = new Rect();
    shape.getBounds(bounds);

    int x0 = nextRect.left + (nextRect.width() - bounds.width() * cellSize) / 2;
    int y0 = nextRect.top + (nextRect.height() - bounds.height() * cellSize) / 2;

    for (int i = 0; i < shape.size(); i++) {
      _fieldPainter.paintCellPix(g,
        x0 + (-bounds.left + (shape.getX(i) - shape.getCenterX())) * cellSize,
        y0 + (-bounds.top + (shape.getY(i) - shape.getCenterY())) * cellSize,
        shape.getCellType(i), CellState.FALLING);
    }

  }

  @Override
  public GameImpulse getAxisImpulse(DragAxis axis, boolean positiveDirection) {
    GameImpulse impulse;

    if(axis == DragAxis.HORIZONTAL) {
      impulse = positiveDirection ? GameImpulse.MOVE_RIGHT : GameImpulse.MOVE_LEFT;
    }else if(axis == DragAxis.ROTATE) {
      impulse = positiveDirection ? GameImpulse.ROTATE_CW : GameImpulse.ROTATE_CCW;
    }else {
      impulse = null;
    }

    return impulse;
  }
}
