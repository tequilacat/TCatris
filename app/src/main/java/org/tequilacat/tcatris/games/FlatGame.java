// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package org.tequilacat.tcatris.games;

import android.graphics.Canvas;
import android.graphics.Rect;

import org.tequilacat.tcatris.core.Debug;
import org.tequilacat.tcatris.core.DynamicState;
import org.tequilacat.tcatris.core.GameDescriptor;
import org.tequilacat.tcatris.core.GameImpulse;
import org.tequilacat.tcatris.core.GameRunner;
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
  int myShapesThrown;
  private FlatShape myFallingShape;
  private FlatShape myNextShape;

  protected static final int MIN_CELL_SIZE = 8;

  private AbstractFlatGamePainter _fieldPainter;

  protected FlatGame(GameDescriptor descriptor, AbstractFlatGamePainter fieldPainter) {
    super(descriptor);
    _fieldPainter = fieldPainter;
  }

  /**
   * resets game to initial step
   */
  public void init() {
    setScore(0);
    myFallingShape = null;
    myNextShape = null;
    myShapesThrown = 0;
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
  public void addEffectiveImpulses(EnumSet<GameImpulse> actionSet) {
    FlatShape curShape = getCurrentShape();

    for (GameImpulse impulse : _CheckedImpulses) {
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

    for (int i = 0; i < aShape.size(); i++) {
      int x = aShape.getX(i), y = aShape.getY(i);
      // if( x<0 || y<0 || x>=getWidth() || y>=getHeight()
      if (x < 0 || y < 0 || x >= getWidth() // || y>=getHeight()
        || (y < getHeight() && field[y][x] != EMPTY)) {
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
    boolean dropped = false;
    if (tillBottom) {
      myFallingShape.moveBy(0, -1);
      while (isShapePlaceable(myFallingShape)) {
        dropped = true;
        myFallingShape.moveBy(0, -1);
      }
      myFallingShape.moveBy(0, 1);
    } else {
      // move one row down
      myFallingShape.moveBy(0, -1);
      if (!isShapePlaceable(myFallingShape)) {
        // revert
        myFallingShape.moveBy(0, 1);
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

  private void countStep() {
    setLevel(Math.min(myShapesThrown / 20, 10));
  }

  @Override
  protected boolean throwInNewShape() {
    myShapesThrown++;
    countStep();

    myFallingShape = myNextShape == null ? createNext() : myNextShape;
    myNextShape = createNext();
    myFallingShape.moveTo(getWidth() / 2, getHeight() - 1);
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

    int glassWidth = this.getWidth(), glassHeight = this.getHeight(),
      nextFigWidth = this.getMaxShapeWidth(), nextFigHeight = this.getMaxShapeHeight();

    int width = screenWidth - MARGIN - MARGIN - VERT_SPACING;
    int height = screenHeight - MARGIN - MARGIN;

    int cellSize = width / (glassWidth + nextFigWidth);

    if (cellSize * glassHeight > height) {
      cellSize = height / glassHeight;
    }

    int fieldX0 = MARGIN;
    int fieldY0 = MARGIN;

    int myFieldWidth = cellSize * glassWidth;
    int myFieldHeight = cellSize * glassHeight;


    // lay out next fig

    int myNextShapeX0 = MARGIN + myFieldWidth + VERT_SPACING;
    myNextShapeX0 += (screenWidth - MARGIN - myNextShapeX0 - cellSize * nextFigWidth) / 2;
    int myNextShapeY0 = MARGIN;

    //Debug.print("Cell Size: "+myCellSize+" , fieldWidth = "+myFieldWidth);


    setGameScreenLayout(new GameScreenLayout(cellSize,
        layoutParams.GameArea.left + fieldX0, layoutParams.GameArea.top + fieldY0,
        myFieldWidth, myFieldHeight,
        layoutParams.GameArea.left + myNextShapeX0, layoutParams.GameArea.top + myNextShapeY0,
        this.getMaxShapeWidth() * cellSize, this.getMaxShapeHeight() * cellSize));

    _fieldPainter.init(getGameScreenLayout());
  }

  /**
   * paints game field
   * @param g canvas to draw to
   * @param dynamicState props of current move state
   */
  @Override
  public void paintField(Canvas g, DynamicState dynamicState) {
    Rect fieldRect = getGameScreenLayout().getFieldRect();
    int cellSize = getGameScreenLayout().getCellSize();
    int pixY = fieldRect.bottom - cellSize;

    _fieldPainter.paintFieldBackground(g);

    for (int y = 0; y < getHeight(); y++) {
      int pixX = fieldRect.left;

      for (int x = 0; x < getWidth(); x++) {
        // settled
        _fieldPainter.paintCellPix(g, pixX, pixY, getCellValue(x, y),
          isSqueezable(x, y) ? CellState.SQUEEZED : CellState.SETTLED);
        pixX += cellSize;
      }

      pixY -= cellSize;
    }

    if (getState() == ACTIVE && !canSqueeze()) {
      FlatShape shape = getCurrentShape();

      int centerX0 = fieldRect.left + shape.getCenterX() * cellSize;
      int centerY0 = fieldRect.bottom - (shape.getCenterY() + 1) * cellSize;

      double dx = 0;
      float rotateFactor = 0; // 1 means 90', 0 means no rotate
      boolean isValid = true;
      boolean drawContour = false;

      int pos = GameRunner.DragType.HORIZONTAL.ordinal();

      if(dynamicState.isTracking(pos)) {
        dx = dynamicState.getValue(pos);
        drawContour = true;

        if(!dynamicState.isValid(pos)) {
          isValid = false;
          dx = 0;
        }
//        RectF rect = new RectF(centerX0, centerY0, centerX0+cellSize, centerY0 + cellSize);
        //g.drawArc(rect, 0, curValue*360, true, tmpPaint);
      }

      pos = GameRunner.DragType.ROTATE.ordinal();

      if(dynamicState.isTracking(pos)) {
        rotateFactor = dynamicState.getValue(pos);
        drawContour = true;

        if(!dynamicState.isValid(pos)) {
          isValid = false;
          rotateFactor = 0;
        }
      }

      if (drawContour) {
        // TODO only update shape contour when it's thrown in or rotated
        _fieldPainter.updateCurrentShapeContour(getCurrentShape());
        _fieldPainter.drawShapeContour(g,
            centerX0 + cellSize / 2 + (int) (dx * cellSize), centerY0 + cellSize / 2,
            isValid, rotateFactor * 90);
      }

      for (int i = 0; i < shape.size(); i++) {
        int x = shape.getX(i), y = shape.getY(i);
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
          _fieldPainter.paintCellPix(g, fieldRect.left + x * cellSize,
              fieldRect.bottom - (y + 1) * cellSize, shape.getCellType(i), CellState.FALLING);
        }
      }
    }
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

    // find out max leftToCenter and rightToCenter
    int maxLeft = 0, maxUp = 0;
    for (int i = 0; i < shape.size(); i++) {
      int x = shape.getX(i), y = shape.getY(i);
      if (maxLeft < -x) maxLeft = -x;
      if (maxUp < y) maxUp = y;
    }

    for (int i = 0; i < shape.size(); i++) {
      int x = shape.getX(i), y = shape.getY(i);
      _fieldPainter.paintCellPix(g, nextRect.left + (x - maxLeft) * cellSize,
        nextRect.top + (maxUp - y) * cellSize,
        shape.getCellType(i), CellState.FALLING);
    }
  }
}
