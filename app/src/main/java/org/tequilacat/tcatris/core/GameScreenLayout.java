package org.tequilacat.tcatris.core;

/**
 * Created by avo on 24.02.2016.
 */
public class GameScreenLayout {
  private final boolean myDisplayIconsVertically;
  private final int myFieldX, myFieldY, myFieldW, myFieldH;
  private final int myNextFigX, myNextFigY, myNextFigW, myNextFigH;

  /****************
   */
  public GameScreenLayout(int fX, int fY, int fWidth, int fHeight,
                          int nextX, int nextY, int nextW, int nextH, boolean verticalIconLayout) {

    myDisplayIconsVertically = verticalIconLayout;
    myFieldX = fX;
    myFieldY = fY;
    myFieldW = fWidth;
    myFieldH = fHeight;

    myNextFigX = nextX;
    myNextFigY = nextY;
    myNextFigW = nextW;
    myNextFigH = nextH;
  }

  public final int getNextFigX() {
    return myNextFigX;
  }

  public final int getNextFigY() {
    return myNextFigY;
  }

  public final int getNextFigWidth() {
    return myNextFigW;
  }

  public final int getNextFigHeight() {
    return myNextFigH;
  }

  public final int getGlassClipX() {
    return myFieldX;
  }

  public final int getGlassClipY() {
    return myFieldY;
  }

  public final int getGlassClipWidth() {
    return myFieldW;
  }

  public final int getGlassClipHeight() {
    return myFieldH;
  }
}
