package org.tequilacat.tcatris.games;

import android.graphics.Rect;

/**
 * Implementation of FlatShape represented in rectangular coordinate system
 */
public class FlatRectShape extends FlatShape {

  private int[] myData;

  public FlatRectShape(int[] data) {
    myData = data;
  }

  @Override
  protected void copyFields(FlatShape fromOther) {
    super.copyFields(fromOther);
    FlatRectShape other = (FlatRectShape) fromOther;
    myData = new int[other.myData.length];
    System.arraycopy(other.myData, 0, myData, 0, other.myData.length);
  }

  @Override
  public int size() {
    return myData.length / 3;
  }

  @Override
  public int getCellType(int i) {
    return myData[(i << 1) + i + 2];
  }

  /**
   * changes cell type
   * @param i cell index
   * @param cellType new value for cell type
   */
  public void setCellType(int i, int cellType) {
    // quick *3
    myData[(i << 1) + i + 2] = cellType;
  }

  @Override
  public void getBounds(Rect bounds) {
    int minCol = 0, maxCol = 0, minRow = 0, maxRow = 0;

    for (int i = 0, len = this.size(); i < len; i++) {
      int col = this.getX(i), row = this.getY(i);

      if (i == 0) {
        minCol = maxCol = col;
        minRow = maxRow = row;
      }else {
        if (minCol > col) {
          minCol = col;
        }else if (maxCol < col) {
          maxCol = col;
        }

        if (minRow > row) {
          minRow = row;
        }else if(maxRow < row) {
          maxRow = row;
        }
      }
    }
    bounds.set(minCol, minRow, maxCol + 1, maxRow + 1);
  }

  @Override
  public FlatShape createCopy() {
    FlatRectShape copy = new FlatRectShape(null);
    copy.copyFields(this);
    return copy;
  }

  @Override
  public int getX(int i, int centerX, int centerY) {
    return myData[(i << 1) + i] + centerX;
  }

  @Override
  protected void rotate(int rotationDir) {
    if (rotationDir != 0) {

      for (int i = 0; i < myData.length; i += 3) {
        int x = myData[i], y = myData[i + 1];
        if (x != 0 || y != 0) {
          if (rotationDir > 0) {
            // clockwise
            myData[i] = -y;
            myData[i + 1] = x;
          } else {
            myData[i] = y;
            myData[i + 1] = -x;
          }
        }
      }
    }
  }

  /**
   * gets Y coord of i-th cell assuming shape is centered at centerX, centerY
   * @param i
   * @param centerX
   * @param centerY
   * @return
   */
  @Override
  public int getY(int i, int centerX, int centerY) {
    // quick *3
    return myData[(i << 1) + i + 1] + centerY;
  }

  @Override
  public boolean signatureEquals(Object shapeSignature) {
    boolean equals = false;

    if (shapeSignature != null && shapeSignature instanceof int[] &&
        ((int[]) shapeSignature).length == myData.length) {
      final int[] sigArray = (int[]) shapeSignature;
      equals = true;

      for (int i = 0, len = sigArray.length; i < len; i++) {
        if (sigArray[i] != myData[i]) {
          equals = false;
          break;
        }
        i++;
        if (sigArray[i] != myData[i]) {
          equals = false;
          break;
        }
        i++; // skip color - does not matter for shape
      }
    }

    return equals;
  }

  @Override
  public Object generateSignature() {
    return myData.clone();
  }
}
