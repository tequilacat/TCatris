package org.tequilacat.tcatris;

import android.os.Bundle;
import android.app.Activity;
import android.widget.ListView;

import org.tequilacat.tcatris.core.GameList;

public class GameSelectorActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    GameList gameList = new GameList();
    ListView gameListView = (ListView) findViewById(R.id.lvMainOptionList);

    //
    //TetrisCanvas gameView = new TetrisCanvas(this, gameList);
    //setContentView(gameView);
    setContentView(R.layout.activity_gameselector);
  }

}
