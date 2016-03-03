// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package org.tequilacat.tcatris.games;

import android.graphics.Canvas;

import org.tequilacat.tcatris.core.GameScreenLayout;
import org.tequilacat.tcatris.core.Tetris;
import org.tequilacat.tcatris.core.GameView;
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


  /********************************
   *********************************/
  @Override
  public void layout(int screenWidth, int screenHeight) {

    FlatGame game = this; // (FlatGame)getGame();
    int glassWidth = game.getWidth(), glassHeight = game.getHeight(),
      nextFigWidth = game.getMaxShapeWidth(), nextFigHeight = game.getMaxShapeHeight();

    int width = screenWidth - GameView.MARGIN_LEFT - GameView.MARGIN_RIGHT - GameView.SPACING_VERT;
    int height = screenHeight - GameView.MARGIN_TOP - GameView.MARGIN_BOTTOM;

    int cellSize = width / (glassWidth + nextFigWidth);

    if (cellSize * glassHeight > height) {
      cellSize = height / glassHeight;
    }


    // if there's no space for icon and text horizontally, try :
    // if big canvas, provide enough space via
/*
    // assume text size as quarter of smaller dimension
    int numInfoSize = (screenWidth < screenHeight ? screenWidth : screenHeight) / 4;
//    Font font = Font.getDefaultFont();
//    int numInfoSize = font.stringWidth("0000");

    boolean displayIconVertically = false;
    int iconSize = (GameView.PlayerIcon == null) ? 0 : GameView.PlayerIcon.getWidth();
    if (iconSize + numInfoSize > screenWidth - GameView.MARGIN_LEFT - cellSize * glassWidth) {
      //Debug.print("Cell size = "+ myCellSize +" does not fit");
      cellSize = (screenWidth - GameView.MARGIN_LEFT - (iconSize + numInfoSize)) / glassWidth;

      if (cellSize >= MIN_CELL_SIZE) {
        //Debug.print("Can't fit icon and 0000, decrease to fit both");
      } else {
        // decrease to fit
        //Debug.print("Can't fit icon and number, decrease to fit number");

        displayIconVertically = true;
       // int cellSize = (screenWidth - GameView.MARGIN_LEFT - numInfoSize) / glassWidth;
//                if(cellSize < myCellSize){
//                    myCellSize = cellSize;
//                }
      }
    }
*/
//        Debug.print("===== Always display vertically");
//        displayIconVertically = true;

    int fieldX0 = GameView.MARGIN_LEFT;
    int fieldY0 = GameView.MARGIN_TOP;

    int myFieldWidth = cellSize * glassWidth;
    int myFieldHeight = cellSize * glassHeight;


    // lay out next fig

    int myNextShapeX0 = GameView.MARGIN_LEFT + myFieldWidth + GameView.SPACING_VERT;
    myNextShapeX0 += (screenWidth - GameView.MARGIN_RIGHT - myNextShapeX0 - cellSize * nextFigWidth) / 2;
    int myNextShapeY0 = GameView.MARGIN_TOP;

    //Debug.print("Cell Size: "+myCellSize+" , fieldWidth = "+myFieldWidth);


    setGameScreenLayout(new GameScreenLayout(cellSize,
      fieldX0, fieldY0, myFieldWidth, myFieldHeight,
      myNextShapeX0, myNextShapeY0,
      game.getMaxShapeWidth() * cellSize, game.getMaxShapeHeight() * cellSize));

    _fieldPainter.init(getGameScreenLayout());
  }

  /**
   * paints game field
   * @param g
   * @param fieldPixHeight
   */
  @Override
  public void paintField(Canvas g, int fieldPixHeight) {
    int cellSize = getGameScreenLayout().getCellSize();
    int pixX, pixY = fieldPixHeight - cellSize;

    _fieldPainter.paintFieldBackground(g);

    for (int y = 0; y < getHeight(); y++) {
      pixX = 0;

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
          _fieldPainter.paintCellPix(g, x * cellSize, fieldPixHeight - (y + 1) * cellSize, shape.getCellType(i), CellState.FALLING);
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
