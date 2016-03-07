package org.tequilacat.tcatris;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ViewFlipper;

import org.tequilacat.tcatris.core.Debug;
import org.tequilacat.tcatris.core.GameList;
import org.tequilacat.tcatris.core.GameView;
import org.tequilacat.tcatris.core.Scoreboard;
import org.tequilacat.tcatris.core.Tetris;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

  private static final String SCOREBOARD_PARCEL_KEY = "scoreboard_parcel_key";

  private ViewFlipper _viewFlipper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    _viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);

    // fill game list
    GameList.init();
    Scoreboard.setState((Scoreboard) savedInstanceState.getParcelable(SCOREBOARD_PARCEL_KEY));

    final List<GameList.GameDescriptor> gameTypes = GameList.instance().getGameDescriptors();
    final ArrayAdapter<GameList.GameDescriptor> adapter = new ArrayAdapter<>(this,
      android.R.layout.simple_list_item_1, gameTypes);

    ListView gameListView = (ListView) findViewById(R.id.lvMainOptionList);
    gameListView.setAdapter(adapter);
    gameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Toast.makeText(GameSelectorActivity.this, gameTypes.get(position).getLabel(), Toast.LENGTH_SHORT).show();
        runGame(gameTypes.get(position));
      }
    });
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    // outState.putParcelable(SCOREBOARD_PARCEL_KEY, Scoreboard.instance());
  }

  private void runGame(GameList.GameDescriptor gameDescriptor) {
    //Intent intent = new Intent(this, GameActivity.class);
    //intent.putExtra(GameActivity.GAME_DESCRIPTOR, gameDescriptor.getId());
    //startActivity(intent);
/*
GameView gameView = (GameView) findViewById(R.id.gameView);
    String gameId = getIntent().getStringExtra(GAME_DESCRIPTOR);
    Tetris game = GameList.instance().createGame(GameList.instance().findDescriptor(gameId));
    gameView.setGame(game);
 */

    Tetris game = GameList.instance().createGame(gameDescriptor);
    GameView gameView = (GameView) findViewById(R.id.gameView);
    gameView.setGame(game);
    _viewFlipper.showNext();
  }

  /**
   * shows view specified by ID in the flipper if it's a child of the flipper
   * @param id
   */
  private void showFlipperViewById(int id) {
    for (int i = 0, n = _viewFlipper.getChildCount(); i < n; i++) {
      View child = _viewFlipper.getChildAt(i);

      if (child.getId() == id) {
        _viewFlipper.setDisplayedChild(i);
        break;
      }
    }
  }

  /**
   * on back button if current is gamelist we finish it by calling base,
   * if current is game we set paused mode
   *
   */
  @Override
  public void onBackPressed() {
    View currentView = _viewFlipper.getCurrentView();

    if(currentView instanceof GameView) {
      showScores();

    }else if(currentView.getId() == R.id.gameSelectorContainer) {
      // let the system process back button
      super.onBackPressed();

    }else if(currentView.getId() == R.id.scoreView ) {
      backFromScores();
    }
  }

  public void onScoreBackButtonClick(View btn) {
    backFromScores();
  }

  public void onScoreSelectGameButtonClick(View btn) {
    Debug.print("select game from score screen");
    showFlipperViewById(R.id.gameSelectorContainer);
  }


  private static java.text.DateFormat TIMESTAMP_FORMAT =
          DateFormat.getDateTimeInstance();
          //new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

  /**
   * Called on pause, or when game is lost.
   * Shows scores screen.
   */
  public void showScores() {
    View currentView = _viewFlipper.getCurrentView();

//    Date d = new Date();
//    d.setTime(0);
//    String s = TIMESTAMP_FORMAT.format(d);

    //TIMESTAMP_FORMAT.format()
    if(currentView instanceof GameView) {
      // fill scores with current score
      GameView gameView = (GameView) currentView;

      Button scoreBackButton = (Button) findViewById(R.id.scoreBackBtn);
      scoreBackButton.setText(getString(
                      gameView.getGame().getState() == Tetris.LOST ?
                              R.string.btn_play_again : R.string.btn_continue)
      );

      gameView.setPaused(true);
      showFlipperViewById(R.id.scoreView);
    }
  }

  private void backFromScores() {
    Debug.print("restart/continue after scores");

    GameView gameView = (GameView) findViewById(R.id.gameView);

    if(gameView.getGame().getState() == Tetris.LOST) {
      gameView.restartGame();
    }
    showFlipperViewById(R.id.gameView);
  }

}
