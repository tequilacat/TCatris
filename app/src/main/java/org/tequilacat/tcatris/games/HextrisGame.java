package org.tequilacat.tcatris.games;

import org.tequilacat.tcatris.core.GameDescriptor;

/**
 * Hextris game
 */
public class HextrisGame extends AbstractRotationGame {
  private final boolean[][] _cellsToSqueeze;

  public HextrisGame(GameDescriptor descriptor) {
    super(descriptor, new HextrisPainter(), HextrisShape.getStaticShapes());

    _cellsToSqueeze = new boolean[getHeight()][getWidth()];
  }

//  @Override
//  protected FlatShape createNext() {
//    return HextrisShape.getStaticShapes()[1].createCopy();
//  }

  @Override
  protected boolean isSqueezable(int col, int row) {
    return canSqueeze() && _cellsToSqueeze[row][col];
  }

  @Override
  protected void estimateSides(float[] threeSides) {
    // find field W and H according to hex size
    int hhCount = getHeight() * 2 + 1;
    // float hw = hh / (float) Math.sin(Math.PI / 3);
    //float dx = hw*1.5
    // normd to fieldwidth = 1
    float hw = 1 / (getWidth() * 1.5f + 0.5f);
    // hh = hw*sin60
    // h = (getHeight()*2 + 1) * (hw*sin60)
    float hh = hw * (float) Math.sin(Math.PI / 3);
    threeSides[0] = (getHeight() * 2 + 1) * hh;
    // hardcode dimensions
    // assume maxW = 3 (3-axis symmetrical shape), maxH = 4
    // w * 3 = dx*2 + hw*2
    threeSides[1] = hw * 3 + hw * 2;
    threeSides[2] = 4 * hh * 2;// 2halfhide * 4
  }

  @Override
  protected boolean computeCanSqueeze() {
    int cols = getWidth(), rows = getHeight();

    for (int y = 0; y < rows; y++) { // scan rows
      for (int x = 0; x < cols; x++) {// scan cells in rows
        _cellsToSqueeze[y][x] = false;
      }
    }

    // whether last row odd cells are all filled
    boolean prevOddColsFilled = false;
    boolean hasRemoved = false;

    for (int y = 0; y < rows; y++) { // scan rows
      boolean evenCol = true;
      boolean allOdds = true, allEvens = true;

      for (int x = 0; x < cols; x++) {// scan cells in rows
        if (field[y][x] == FlatGame.EMPTY) {
          if(evenCol){
            allEvens=false;
          }else {
            allOdds = false;
          }
          if (!allOdds && !allEvens) {
            break;
          }
        }

        evenCol = !evenCol;
      }

      // now analyze prev
      if(allEvens) {
        if (prevOddColsFilled || allOdds) {
          hasRemoved = true;
          evenCol = true;

          // marc current evens
          for (int x = 0; x < cols; x++) {
            if (evenCol) {
              _cellsToSqueeze[y][x] = true;
            } else {
              if (prevOddColsFilled) {
                _cellsToSqueeze[y - 1][x] = true;
              }
              if (allOdds) {
                _cellsToSqueeze[y][x] = true;
              }
            }
            evenCol = !evenCol;
          }
        }
      }

      prevOddColsFilled = allOdds;
    }

    return hasRemoved;
  }

  @Override
  protected boolean squeeze() {
    for (int x = 0; x < getWidth(); x++) {// scan cells in rows
      int dst = getHeight() - 1, src = dst;
      while (src >= 0) {
        if (_cellsToSqueeze[src][x]) {
          src--;
        } else {
          if (src < dst) {
            field[dst][x] = field[src][x];
          }
          src--;
          dst--;
        }
      }
      while (dst >= 0) {
        field[dst][x] = EMPTY;
        dst--;
      }
    }
    return false;
  }
}
