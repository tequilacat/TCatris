// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package org.tequilacat.tcatris.games;

// Referenced classes of package flat:
//            FlatGame, Shape

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import org.tequilacat.tcatris.core.GameScreenLayout;
import org.tequilacat.tcatris.core.TetrisCanvas;

public class Columns extends FlatGame {

  boolean mySqueezable[];
  int myLastScores;
  private static final int NCOLORS = 5;
  boolean cellsToSqueeze[][];
  boolean squeezable;

  // default is xixit
  // "columns" same but shapes are vertical
  // "trix" same but shapes are rotated

  private int myGameType;
  private static final int FIGTYPE_VERT = 0;
  private static final int FIGTYPE_HORZ = 1;
  private static final int FIGTYPE_ROTATE = 2;

  /**************************************************
   * expect:
   * rotate=color (if not, rotate =
   **************************************************/
  @Override
  protected void configure(String specSettings) {
    // expect : fig=vert, fig=horz, fig=rotateable
//        Debug.print("fig type: '"+  +"'");
    String figType = specSettings;
    if ("horz".equals(figType)) {
      myGameType = FIGTYPE_HORZ;
    } else if ("vert".equals(figType)) {
      myGameType = FIGTYPE_VERT;
    } else {
      myGameType = FIGTYPE_ROTATE;
    }
  }

  /**************************************
   * creates new (mutable) shape
   **************************************/
  @Override
  public FlatShape createNext() {
    int c1 = 1 + getRandomInt(NCOLORS);
    int c2 = 1 + getRandomInt(NCOLORS);
    int c3 = 1 + getRandomInt(NCOLORS);
    // return new ColumnShape(c1, c2, c3);
    FlatShape fs = new FlatShape(new int[]{0, -1, c1, 0, 0, c2, 0, 1, c3});
    if (myGameType == FIGTYPE_HORZ) {
      fs.rotate(1);
    }
    return fs;
  }

  /**************************************************
   **************************************************/
  @Override
  protected FlatShape rotate(FlatShape shape, int dir) {
    if (myGameType == FIGTYPE_ROTATE) {
      shape = super.rotate(shape, dir);
    } else {
      dir = (dir > 0) ? -1 : 1;
      int i = (dir > 0) ? 0 : shape.size() - 1, count = shape.size(),
        newPos = i + dir, firstValue = shape.getCellType(i);

      while (--count > 0) {
        shape.setCellType(i, shape.getCellType(newPos));

        newPos += dir;
        i += dir;
      }
      shape.setCellType(i, firstValue);
    }
    return shape;
  }

  /**************************************
   * @return if cell is squeezable
   **************************************/
  protected boolean isSqueezable(int x, int y) {
    return (cellsToSqueeze != null) && canSqueeze() && cellsToSqueeze[y][x];
  }

  /************************************************
   ************************************************/
  @Override
  public boolean computeCanSqueeze() {
    squeezable = false;
    myLastScores = 0;

    if (cellsToSqueeze == null)
      cellsToSqueeze = new boolean[getHeight()][getWidth()];

    for (int y = 0; y < getHeight(); y++) { // scan rows
      for (int x = 0; x < getWidth(); x++) {// scan cells in rows
        cellsToSqueeze[y][x] = false;
      }
    }

    // left to right
    for (int y = 0; y < getHeight(); y++) {
      myLastScores += runLength(0, y, 1, 0);
    }

    // bottom to top
    for (int x = 0; x < getWidth(); x++) {
      myLastScores += runLength(x, 0, 0, 1);
    }

    // diagonals
    int x = 2, yPos = 0;
    do {
      int xPos = (x < getWidth()) ? x : (getWidth() - 1);
      myLastScores += runLength(xPos, yPos, -1, 1);
      myLastScores += runLength(xPos, getHeight() - 1 - yPos, -1, -1);

      x++;
      yPos = (x < getWidth()) ? 0 : (x - getWidth() + 1);
    } while (yPos + 2 < getHeight());

    myLastScored = myLastScores;
    return squeezable;
  }

  // runs along direction, marks all 3&more cells as toRemove.

  /************************************************
   ************************************************/
  private int runLength(int x0, int y0, int dx, int dy) {
    int scores = 0;

    int x = x0, y = y0;
    int prevCol = -1;
    int width = getWidth(), height = getHeight();
    int rlen = 0;

    for (; x > -1 && y > -1 && x < width && y < height; x += dx, y += dy) {
//				diag = "field["+ y +"]["+ x +"]";

      int curCol = field[y][x];
      if (curCol != prevCol) {
        if (rlen >= 3) {
          squeezable = true;
          scores += rlen;
          int xx = x - dx, yy = y - dy;
          while (rlen-- > 0) {
//							diag = "field["+ y +"]["+ x +"], ";
//							diag += "cellsToSqueeze["+ yy +"]["+ xx +"]";

            cellsToSqueeze[yy][xx] = true;
            xx -= dx;
            yy -= dy;
          }
        }
        rlen = 1;
        prevCol = curCol;
      } else {
        if (curCol != EMPTY) {
          rlen++;
        }
      }
    }

    if (rlen >= 3) {
      squeezable = true;
      scores += rlen;
      int xx = x - dx, yy = y - dy;
      while (rlen-- > 0) {
//							diag = "field["+ y +"]["+ x +"], ";
//							diag += "cellsToSqueeze["+ yy +"]["+ xx +"]";

        cellsToSqueeze[yy][xx] = true;
        xx -= dx;
        yy -= dy;
      }
    }
    return scores;
  }

  /****************************************
   * @returns if squeeze leads to next squeeze
   ****************************************/
  @Override
  public boolean squeeze() {
    long start = System.currentTimeMillis();
    //Debug.print("Increase scores by "+ myLastScores +"");
    myScore += myLastScores;
    myLastScores = 0;

    for (int x = 0; x < getWidth(); x++) {// scan cells in rows
      // foreach col, remove
      int curY = 0, fromY = 0;

      while (curY < getHeight()) {
        if (fromY >= getHeight()) { // copy to cell from outside
          field[curY][x] = EMPTY;
          cellsToSqueeze[curY][x] = false;
          curY++;
        } else
          //  fromY is readable, test
          if (cellsToSqueeze[fromY][x]) { // cur cell is squeezable
//					Debug.print("  row "+ x +", SQUEEZE: "+ curY +" / "+ fromY +" : "+dbgGetCol(x));
            fromY++;
          } else {
            if (curY != fromY) {
              String s = "  " + field[fromY][x] + "[ @" + fromY + "]  -> " + field[curY][x] + "[ @" + curY + "]";
              field[curY][x] = field[fromY][x];
              cellsToSqueeze[curY][x] = false;
//						Debug.print(s+"  : "+dbgGetCol(x));
            }
            curY++;
            fromY++;
          }
      }
    }

    return true;
  }


  /************************************************
   ************************************************/
  private String dbgGetCol(int x) {
    String s = "";
    for (int y = 0; y < getHeight(); y++) {
      s += "" + field[y][x];
    }
    return s;
  }


  /********************************
   * rotates and checks
   ******************************/
  /* public boolean moveShape(Shape theShape, int newX, int newY, int rotationDir){
//		Debug.print(" at : "+ newX +" , "+ newY +"");
		ColumnShape shape = (ColumnShape)theShape;

		if(newX < 0 || newX >= getWidth()) return false;
		if(newY < 1)  return false; // shape out of bounds
		
		if((newY >= getHeight() || getCellValue(newX, newY)==0 )
			&& (newY-1 >= getHeight() || getCellValue(newX, newY-1)==0 )
			&& (newY+1 >= getHeight() || getCellValue(newX, newY+1)==0) ){
				
				shape.moveTo(newX, newY, rotationDir);
				return true;
		}
		
		return false;
	} */


  //////////////
  private static final int N_FIGURES = 5;
  private Bitmap myShapesImage = null;

  /**************************************************
   **************************************************/
//  public Image getShapesImage() {
//    return myShapesImage;
//  }

  public void layout(int screenWidth, int screenHeight) {
    super.layout(screenWidth, screenHeight);
    createBigImage(myCellSize);
  }

  /**************************************************
   **************************************************/
//    private int darkerColor(int color){
//        int red =   ( (color&0xff0000) >> 24) * 7 / 10;
//        int green = ( (color&0xff00) >> 16  ) * 7 / 10;
//        int blue =  ( color & 0xff          ) * 7 / 10;
//         
//        return (red & 0xff) << 24
//            +(green&0xff) << 16 + (blue & 0xff);
//    }

  /**************************************************
   **************************************************/
  private void createBigImage(int cellSize) {
    myShapesImage = Bitmap.createBitmap(cellSize * 3, cellSize * N_FIGURES, Bitmap.Config.ARGB_8888);

    Canvas c = new Canvas(myShapesImage);
    c.drawColor(getFieldBackground());
    Paint p = new Paint();
    p.setStyle(Paint.Style.FILL);

    for (int i = 0; i < N_FIGURES; i++) {
      drawShape(c, p, cellSize, 0, i * cellSize, i + 1, FIGCELL_FALLING);
      c.translate(cellSize, 0);
      drawShape(c, p, cellSize, cellSize, i * cellSize, i + 1, FIGCELL_SQUEEZED);
      c.translate(cellSize, 0);
      drawShape(c, p, cellSize, cellSize + cellSize, i * cellSize, i + 1, FIGCELL_SETTLED);
      c.translate(-cellSize * 2, cellSize);
    }
  }

  /**************************************************
   **************************************************/
  private void drawShape(Canvas g, Paint p, int cellSize, int x, int y, int shapeIndex, int state) {
    p.setColor(getTypeColor(shapeIndex));
    p.setStyle(Paint.Style.STROKE);

    if (shapeIndex == 1) { // square
      if (state == FIGCELL_FALLING || state != FIGCELL_SQUEEZED) {
        p.setStyle(Paint.Style.FILL);
        g.drawRect(0, 0, cellSize - 1, cellSize - 1, p);
      } else if (state == FIGCELL_SQUEEZED) {
        g.drawRect(0, 0, cellSize - 2, cellSize - 2, p);
      } else { // settled
      }
    } else if (shapeIndex == 2) {
      if (state == FIGCELL_FALLING || state != FIGCELL_SQUEEZED) {
        for (int yy = 0; yy < cellSize - 1; yy++) {
          g.drawLine(yy / 2, yy, (cellSize - 1) - yy / 2, yy, p);
        }
      } else if (state == FIGCELL_SQUEEZED) {
        g.drawLine(0, 0, cellSize - 1, 0, p);
        g.drawLine(0, 0, cellSize / 2, cellSize - 2, p);
        g.drawLine(cellSize - 1, 0, cellSize / 2, cellSize - 2, p);
      } else { // settled
      }
    } else if (shapeIndex == 3) {
      if (state == FIGCELL_FALLING || state != FIGCELL_SQUEEZED) {
        for (int yy = 0; yy < cellSize - 1; yy++) {
          g.drawLine(0, yy, yy, yy, p);
        }
      } else if (state == FIGCELL_SQUEEZED) {
        g.drawLine(0, 0, cellSize - 2, cellSize - 2, p);
        g.drawLine(0, 0, 0, cellSize - 2, p);
        g.drawLine(cellSize - 2, cellSize - 2, 0, cellSize - 2, p);
      } else { // settled
      }
    } else if (shapeIndex == 4) {
      RectF oval = new RectF(0, 0, cellSize - 1, cellSize - 1);

      if (state == FIGCELL_FALLING || state != FIGCELL_SQUEEZED) {
        // fill
        p.setStyle(Paint.Style.FILL);
        g.drawOval(oval, p);
      } else if (state == FIGCELL_SQUEEZED) {
        g.drawOval(oval, p);
      } else { // settled
      }
    } else if (shapeIndex == 5) {
      if (state == FIGCELL_FALLING || state != FIGCELL_SQUEEZED) {
        for (int yy = 0; yy < cellSize - 1; yy++) {
          g.drawLine(yy / 2, (cellSize - 1) - yy, (cellSize - 1) - yy / 2, cellSize - 1 - yy, p);
        }
      } else if (state == FIGCELL_SQUEEZED) {
        g.drawLine(1, cellSize - 1, cellSize / 2, 1, p);
        g.drawLine(1, cellSize - 1, cellSize - 1, cellSize - 1, p);
        g.drawLine(cellSize - 1, cellSize - 1, cellSize / 2, 1, p);
      } else { // settled
      }
    }
  }

  private Paint _cellPainter = new Paint();

  /**************************************************
   **************************************************/
  @Override
  protected void paintCellPix(Canvas g, int x, int y, int cellType, int cellState) {
    if (cellType > 0) {
      int cx = cellState * myCellSize, cy = (cellType - 1) * myCellSize;
      Rect src = new Rect(x, y, cx + myCellSize, cy + myCellSize);
      //g.setClip(x, y, myCellSize, myCellSize);
      //int imgX = x - cellState * myCellSize, imgY = y - (cellType - 1) * myCellSize;
      RectF dst = new RectF(x, y, x + myCellSize, y + myCellSize);
      g.drawBitmap(myShapesImage, src, dst, _cellPainter);
    }
  }
}
