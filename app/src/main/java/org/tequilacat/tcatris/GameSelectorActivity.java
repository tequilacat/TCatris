package org.tequilacat.tcatris;

import android.os.Bundle;
import android.app.Activity;

import org.tequilacat.tcatris.core.GameList;
import org.tequilacat.tcatris.core.TetrisCanvas;

public class GameSelectorActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GameList gameList = new GameList();
    //TetrisCanvas gameView = new TetrisCanvas(this, gameList);
    //setContentView(gameView);
    setContentView(R.layout.activity_blank);
  }

}
