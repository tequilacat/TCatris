// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package org.tequilacat.tcatris.games;

import android.graphics.Canvas;
import android.graphics.Rect;

import org.tequilacat.tcatris.core.GameScreenLayout;
import org.tequilacat.tcatris.core.LayoutParameters;
import org.tequilacat.tcatris.core.Tetris;
import org.tequilacat.tcatris.core.Ui;

public abstract class FlatGame extends Tetris {
  // int myCellSize;

  public static final int EMPTY = 0;

  protected int myScore;
  private int myLevel;
  protected int field[][];
  int myShapesThrown;
  private FlatShape myFallingShape;
  private FlatShape myNextShape;

  protected static final int MIN_CELL_SIZE = 8;

  private AbstractFlatGamePainter _fieldPainter;

  protected FlatGame(AbstractFlatGamePainter fieldPainter) {
    _fieldPainter = fieldPainter;
  }

  /**
   * resets game to initial step
   */
  public void init() {
    myScore = 0;
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

  /**************************************************
   **************************************************/
  public int getCellValue(int i, int j) {
    return field[j][i];
  }

  /**************************************************
   **************************************************/
  public boolean rotateClockwise() {
    return tryRotate(1);
  }

  /**************************************************
   **************************************************/
  public boolean rotateAntiClockwise() {
    return tryRotate(-1);
  }

  /**************************************************
   **************************************************/
  private boolean tryRotate(int direction) {
    FlatShape moved = rotate(myFallingShape, direction);

    if (isShapePlaceable(moved)) { // isOk ,
      myFallingShape = moved;
      return true;
    } else { // if same shape (myFallingShape) rollback it
      if (myFallingShape == moved) {
        rotate(myFallingShape, -direction);
      }
      return false;
    }
  }


  /**************************************************
   **************************************************/
  public boolean moveLeft() {
    FlatShape moved = myFallingShape; // new FlatShape(myFallingShape);
    moved.moveBy(-1, 0);
    if (!isShapePlaceable(moved)) {
      // revert
      moved.moveBy(1, 0);
      return false;
    }
    return true;
  }

  /**************************************************
   **************************************************/
  public boolean moveRight() {
    FlatShape moved = myFallingShape;
    moved.moveBy(1, 0);
    if (!isShapePlaceable(moved)) {
      // revert
      moved.moveBy(-1, 0);
      return false;
    }
    return true;
  }

  /**************************************************
   **************************************************/
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

  /**************************************************
   **************************************************/
  protected FlatShape rotate(FlatShape shape, int dir) {
    shape = new FlatShape(shape);
    shape.rotate(dir);
    return shape;
  }

  /**************************************************
   **************************************************/
  public int getLevel() {
    return myLevel;
  }

  public int getScore() {
    return myScore;
  }

  /********************************
   * drops cur shape 1 level , or till bottom.
   *
   * @returns if really have dropped it any level.
   ******************************/
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

  /********************************
   * copies falling shape into field
   ********************************/
  protected boolean acquireFallenShape() {
    //    System.out.println("Acquire Fallen Shape");

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
    myLevel = myShapesThrown / 20;
    if (myLevel > 10)
      myLevel = 10;
  }

  /**
   */
  protected boolean throwInNewShape() {
    myShapesThrown++;
    countStep();
    myFallingShape = myNextShape;
    if (myFallingShape == null)
      myFallingShape = createNext();
    myNextShape = createNext();

    myFallingShape.moveTo(getWidth() / 2, getHeight() - 1);

    return isShapePlaceable(myFallingShape);
  }


  protected abstract boolean isSqueezable(int i, int j);

  protected abstract FlatShape createNext();


  @Override
  public void layout(LayoutParameters layoutParams) {
    //LayoutParameters layoutParams = new LayoutParameters();
    int screenWidth = layoutParams.GameArea.width(), screenHeight = layoutParams.GameArea.height();

    int glassWidth = this.getWidth(), glassHeight = this.getHeight(),
      nextFigWidth = this.getMaxShapeWidth(), nextFigHeight = this.getMaxShapeHeight();

    int width = screenWidth - layoutParams.MARGIN_LEFT - layoutParams.MARGIN_RIGHT - layoutParams.SPACING_VERT;
    int height = screenHeight - layoutParams.MARGIN_TOP - layoutParams.MARGIN_BOTTOM;

    int cellSize = width / (glassWidth + nextFigWidth);

    if (cellSize * glassHeight > height) {
      cellSize = height / glassHeight;
    }

    int fieldX0 = layoutParams.MARGIN_LEFT;
    int fieldY0 = layoutParams.MARGIN_TOP;

    int myFieldWidth = cellSize * glassWidth;
    int myFieldHeight = cellSize * glassHeight;


    // lay out next fig

    int myNextShapeX0 = layoutParams.MARGIN_LEFT + myFieldWidth + layoutParams.SPACING_VERT;
    myNextShapeX0 += (screenWidth - layoutParams.MARGIN_RIGHT - myNextShapeX0 - cellSize * nextFigWidth) / 2;
    int myNextShapeY0 = layoutParams.MARGIN_TOP;

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
   * @param g
   */
  @Override
  public void paintField(Canvas g) {
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
   * @param g
   * @param nextFigX
   * @param nextFigY
   * @param nextFigWidth
   * @param nextFigHeight
   */
  @Override
  public void paintNext(Canvas g, int nextFigX, int nextFigY, int nextFigWidth, int nextFigHeight) {
    Ui.fillRect(g, nextFigX, nextFigY, nextFigWidth, nextFigHeight, _fieldPainter.getFieldBackground());

    int cellSize = getGameScreenLayout().getCellSize();
    FlatShape shape = getNextShape();

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
      _fieldPainter.paintCellPix(g, nextFigX + (x - maxLeft) * cellSize,
        nextFigY + (maxUp - y) * cellSize,
        shape.getCellType(i), CellState.FALLING);
    }
  }
}
