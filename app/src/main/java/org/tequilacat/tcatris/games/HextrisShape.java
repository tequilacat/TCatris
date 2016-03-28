package org.tequilacat.tcatris.games;

import android.graphics.Rect;

import org.tequilacat.tcatris.core.Debug;

/**
 * Hexagonal shape
 */
public class HextrisShape extends FlatShape {
  /**
   * ids of cell locations around center
   */
  private final byte[] _cellIndexes;

  /** ordinal index of shape defining its color */
  private int _shapeColor;

  private static final int CELLCOUNT = 4;

  private static final FlatShape[] _staticShapes;
  private static final byte[] _rotateCw;
  private static final byte[] _rotateCcw;

  private static final int[] _xOffsets = new int[]{
      0, 0, -1, -1, 0, 1, 1, 1, 0, -1, -2, -2, -2, -1, 0, 1, 2, 2, 2};

  private static final int[] _yOddOffsets = new int[]{
      0, 1, 1, 0, -1, 0, 1, 2, 2, 2, 1, 0, -1, -1, -2, -1, -1, 0, 1};

  private static final int[] _yEvenOffsets = new int[]{
      0, 1, 0, -1, -1, -1, 0, 1, 2, 1, 1, 0, -1, -2, -2, -2, -1, 0, 1};

  /**
   */
  private static byte[] toBytes(String encoded) {
    byte[] arr = new byte[encoded.length()];
    for (int i = 0; i < arr.length; i++) {
      arr[i] = (byte) (encoded.charAt(i) - 'A');
    }
    return arr;
  }

  static {
    String[] encodedShapes = new String[] {
        "EABC", // palka
        "NEAB", "PEAB", // kocherga
        "BCDE", // around
        "EABC", "EABG",
        "EAGH", "EACJ",
        "AEFG", "ACEG"
    };

    _staticShapes = new FlatShape[encodedShapes.length];
    //                    ABCDEFGHIJKLMNOPQRS
    _rotateCw =  toBytes("ACDEFGBJKLMNOPQRSHI");
    _rotateCcw = toBytes("AGBCDEFRSHIJKLMNOPQ");

    for (int i = 0; i < encodedShapes.length; i++) {
      _staticShapes[i] = new HextrisShape(i + 1, toBytes(encodedShapes[i]));
    }
  }

  private HextrisShape(int index, byte[] cellIndexes) {
    _shapeColor = index;
    _cellIndexes = cellIndexes;
  }

  @Override
  public FlatShape createCopy() {
    HextrisShape newShape = new HextrisShape(_shapeColor, _cellIndexes.clone());
    newShape.copyFields(this);
    return newShape;
  }

  @Override
  public int size() {
    return CELLCOUNT;
  }

  @Override
  public void getBounds(Rect bounds) {
    // TODO real computation of hex shape bounds
    bounds.set(-1, -1, 2, 2);
  }

  @Override
  protected void rotate(int rotationDir) {
    for (int i = 0; i < CELLCOUNT; i++) {
      _cellIndexes[i] = (rotationDir > 0 ? _rotateCw : _rotateCcw)[_cellIndexes[i]];
    }
  }

  @Override
  public int getX(int i, int centerX, int centerY) {
    return centerX + _xOffsets[_cellIndexes[i]];
  }

  @Override
  public int getY(int i, int centerX, int centerY) {
    return centerY + (((centerX & 1) == 0) ? _yEvenOffsets : _yOddOffsets)[_cellIndexes[i]];
  }

  @Override
  public int getCellType(int i) {
    return _shapeColor;
  }

  @Override
  public boolean signatureEquals(Object shapeSignature) {
    boolean isEqual = false;

    if (shapeSignature instanceof int[] && ((int[]) shapeSignature).length == CELLCOUNT) {
      int[] signatureArray = (int[]) shapeSignature;
      isEqual = true;

      for (int i = 0; i < CELLCOUNT; i++) {
        if (signatureArray[i] != _cellIndexes[i]) {
          isEqual = false;
          break;
        }
      }
    }

    return isEqual;
  }

  @Override
  public Object generateSignature() {
    return _cellIndexes.clone();
  }

  public static FlatShape[] getStaticShapes() {
    return _staticShapes;
  }
}
