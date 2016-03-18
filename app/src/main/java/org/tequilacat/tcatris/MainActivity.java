package org.tequilacat.tcatris;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TwoLineListItem;
import android.widget.ViewFlipper;

import org.tequilacat.tcatris.core.Debug;
import org.tequilacat.tcatris.core.GameDescriptor;
import org.tequilacat.tcatris.core.GameList;
import org.tequilacat.tcatris.core.GameView;
import org.tequilacat.tcatris.core.Scoreboard;
import org.tequilacat.tcatris.core.Tetris;
import org.tequilacat.tcatris.core.VisualResources;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private static final String SCOREBOARD_PARCEL_KEY = "scoreboard_parcel_key";
  private static final String SCORE_PREFBANK_NAME = "scores";
  private static final String COMMONPREFS_PREFBANK_NAME = "commonprefs";

  private ViewFlipper _viewFlipper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // set preferences from XML
    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

    // the sound vol buttons will control sound of FX
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    VisualResources.Defaults = new VisualResources(getResources());

    setContentView(R.layout.activity_main);

    Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
    setSupportActionBar(myToolbar);

    _viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);

    // fill game list
    GameList.init();
    // init scores from saved preferences
    SharedPreferences prefs = getSharedPreferences(SCORE_PREFBANK_NAME, MODE_PRIVATE);
    Scoreboard.setState(prefs.getString(SCOREBOARD_PARCEL_KEY, null));

    final List<GameDescriptor> gameTypes = GameList.instance().getGameDescriptors();
    final ArrayAdapter<GameDescriptor> adapter = new ArrayAdapter<>(this,
      android.R.layout.simple_list_item_1, gameTypes);

    ListView gameListView = (ListView) findViewById(R.id.lvMainOptionList);
    gameListView.setAdapter(adapter);
    gameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        runGame(gameTypes.get(position));
      }
    });
  }

  /**
   * stores game scores for all games
   */
  @Override
  protected void onPause() {
    super.onPause();

    SharedPreferences.Editor prefEditor = getSharedPreferences(SCORE_PREFBANK_NAME, MODE_PRIVATE).edit();
    prefEditor.putString(SCOREBOARD_PARCEL_KEY, Scoreboard.getState());
    prefEditor.apply();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    boolean res;

    if(item.getItemId() == R.id.mnu_settings) {
      showSettings();
      res = true;

    }else if(item.getItemId() == R.id.mnu_about) {
      Toast toast = Toast.makeText(this, BuildConfig.VERSION_NAME, Toast.LENGTH_LONG);
      toast.show();
      res = true;

    }else {
      res = super.onOptionsItemSelected(item);
    }

    return res;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.game_menu, menu);
    return true;
  }

  private void runGame(GameDescriptor gameDescriptor) {
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
      // showSettings();

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

    if(currentView instanceof GameView) {
      // fill scores with current score
      GameView gameView = (GameView) currentView;
      final Scoreboard.GameScores gs = Scoreboard.instance().getGameScores(
          gameView.getGame().getDescriptor().getId());

      ListView scoreListView = (ListView) findViewById(R.id.lvScoreList);

      ArrayAdapter<Scoreboard.ScoreEntry> adapter = new ArrayAdapter<Scoreboard.ScoreEntry>(
          this, android.R.layout.simple_list_item_2,
          android.R.id.text1, gs.getEntries()) {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
          View view = super.getView(position, convertView, parent);
          TextView text1 = (TextView) view.findViewById(android.R.id.text1);
          TextView text2 = (TextView) view.findViewById(android.R.id.text2);

          Scoreboard.ScoreEntry item = getItem(position);
          text1.setText(Integer.valueOf(item.getScore()).toString());
          text2.setText(gs.isCurrent(item) ? getContext().getString(R.string.current_score)
              : TIMESTAMP_FORMAT.format(new Date(item.getTime())));
          return view;
        }
      };
      scoreListView.setAdapter(adapter);

      // if current score did not make into roaster show message
      TextView scoreTooLowText = (TextView) findViewById(R.id.scoreOutOfRoasterText);
      scoreTooLowText.setText(gs.containsCurrentScore() ? null :
          String.format(getString(R.string.current_score_too_low), gameView.getGame().getScore()));

      // create back button
      Button scoreBackButton = (Button) findViewById(R.id.scoreBackBtn);
      scoreBackButton.setText(getString(
          gameView.getGame().getState() == Tetris.LOST ?
            R.string.btn_play_again : R.string.btn_continue)
      );

      // create score view title
      TextView scoreViewTitle = (TextView) findViewById(R.id.scoreViewTitle);
      scoreViewTitle.setText(String.format(getString(R.string.top_scores),
        gameView.getGame().getDescriptor().getLabel()));

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

  private void showSettings() {
    Intent intent = new Intent();
    intent.setClass(MainActivity.this, SettingsActivity.class);
    startActivityForResult(intent, 0);
  }
}
