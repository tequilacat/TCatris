package org.tequilacat.tcatris;

import android.app.Activity;
import android.os.Bundle;

import org.tequilacat.tcatris.core.GameList;
import org.tequilacat.tcatris.core.GameView;
import org.tequilacat.tcatris.core.Tetris;

public class GameActivity extends Activity {

  public static final String GAME_DESCRIPTOR = "game_descriptor";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_game);
    // find
    GameView gameView = (GameView) findViewById(R.id.gameView);
    String gameId = getIntent().getStringExtra(GAME_DESCRIPTOR);
    Tetris game = GameList.instance().createGame(GameList.instance().findDescriptor(gameId));
    gameView.setGame(game);
  }
}
