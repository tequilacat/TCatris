package org.tequilacat.tcatris.games;

import org.tequilacat.tcatris.core.GameDescriptor;

/**
 * Hextris game
 */
public class HextrisGame extends AbstractRotationGame {

  public HextrisGame(GameDescriptor descriptor) {
    super(descriptor, new HextrisPainter(), HextrisShape.getStaticShapes());
  }

//  @Override
//  protected FlatShape createNext() {
//    return HextrisShape.getStaticShapes()[1].createCopy();
//  }

  @Override
  protected boolean isSqueezable(int i, int j) {
    // TODO implement isSqueezable
    return false;
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
    // TODO implement computeCanSqueeze
    return false;
  }

  @Override
  protected boolean squeeze() {
    // TODO implement squeeze
    return false;
  }
}
