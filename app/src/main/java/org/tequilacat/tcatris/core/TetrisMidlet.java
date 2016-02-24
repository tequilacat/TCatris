package org.tequilacat.tcatris.core;


import java.io.*;
import java.util.Vector;

/*import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import flat.*;
*/

public class TetrisMidlet extends MIDlet {

  public static TetrisMidlet instance;

  TetrisCanvas myGameDisplay;

  private static String myState;

  private static RecordStore myRS = null;
  private static Vector myGameStateVector;
  //    private static Object[] myGameStateVector = null;
  private static final String RMS_NAME = "tetris_rms";

  private Vector myGameDescriptors = new Vector();

  /**************************************************
   **************************************************/
  public TetrisMidlet() {
    instance = this;

    // read from /games.txt
    // into StringBuffer

    try {
      Reader reader = new InputStreamReader(getClass().getResourceAsStream("/games.txt"));
      StringBuffer stb = new StringBuffer();
      //char[] chunk = new char[1000];
      //int readCount;
      //while((readCount = reader.read(chunk)) > 0){
      int inChar;
      while ((inChar = reader.read()) != -1) {
        if (inChar == '\n') {
          String mayBeDesc = stb.toString();
          if (mayBeDesc.startsWith("game=")) {
            // in form:
            // game=flat.ClassicGame:Tetris:10:15:2:4

            // sep1 after classname
            int sep1 = mayBeDesc.indexOf(':', 5), sep2 = mayBeDesc.indexOf(':', sep1 + 1);

            // class name
            myGameDescriptors.addElement(mayBeDesc.substring(5, sep1));
            // game name
            myGameDescriptors.addElement(mayBeDesc.substring(sep1 + 1, sep2));
            // game params
            myGameDescriptors.addElement(mayBeDesc.substring(sep2 + 1));


            //Debug.print("ADD Game: '"+ myGameDescriptors.elementAt() +"'");
          }
          stb.setLength(0);
        } else {
          stb.append((char) inChar);
        }
      }
    } catch (Exception e) {
      Debug.print("Error reading game definitions: " + e);
    }
  }

  /**************************************************
   **************************************************/
  public void populateStartGameMenu() {
    for (int i = 1; i < myGameDescriptors.size(); i += 3) {
      Ui.addItem((String) myGameDescriptors.elementAt(i));
    }
//        Ui.addItem("Columns");
//        Ui.addItem("Tetris");
  }


  /**************************************************
   * Creates game for given index, inits from last saved props
   *************************************************/
  public Tetris createGame(int index) {
    Tetris game = null;
    try {
      String gameClass = (String) myGameDescriptors.elementAt(index * 3),
        gameName = (String) myGameDescriptors.elementAt(index * 3 + 1),
        gameDesc = (String) myGameDescriptors.elementAt(index * 3 + 2);
      //Debug.print("Create game: class = "+gameClass+", '"+ gameName +"', -> "+gameDesc);
      game = (Tetris) Class.forName(gameClass).newInstance();
      game.init(index, gameName, gameDesc, getGameData(gameName));

    } catch (Exception e) {
      Debug.print("Error creating game: " + e);
    }

    return game;
  }

  /**************************************************
   **************************************************/
  // private byte[] getGameData(int index){
  private byte[] getGameData(String gameId) {
    for (int i = 0; i < myGameStateVector.size(); i += 2) {
      if (myGameStateVector.elementAt(i).equals(gameId)) {
        return (byte[]) myGameStateVector.elementAt(i + 1);
      }
    }
    return null;
    //return (index + 1 >= myGameStateVector.length) ? null : ((byte[])myGameStateVector[index+1]);
  }


  /**************************************************
   **************************************************/
  public static void storeGameData(byte[] data, String gameId) {
    System.out.println("Store game data '" + gameId + "': bytes are " + data.length);
    for (int i = 0; i < myGameStateVector.size(); i += 2) {
      if (myGameStateVector.elementAt(i).equals(gameId)) {
        myGameStateVector.setElementAt(data, i + 1);
        return;
      }
    }
    // here only if we add game data 1st time!
    myGameStateVector.addElement(gameId);
    myGameStateVector.addElement(data);

//        myGameStateVector[gameIndex+1] = data;
  }


  /**************************************************
   * MIDLET LYFECYCLE
   **************************************************/
  public void startApp() throws MIDletStateChangeException {
//        Columns.GAME_INDEX = 0;
//        ClassicGame.GAME_INDEX = 1;

    if (myGameDisplay != null) {
      return;
    }
    TetrisCanvas.display = Display.getDisplay(this);

    readStoredData();

    myGameDisplay = new TetrisCanvas();
    TetrisCanvas.display.setCurrent(myGameDisplay);
  }

  /**************************************************
   * MIDLET LYFECYCLE
   **************************************************/
  public void pauseApp() {
    // don't reset if game active
    if (myGameDisplay != null) {
      return;
    }

    if (myGameDisplay != null)
      myGameDisplay.stopGame();

    TetrisCanvas.PlayerIcon = null;
    TetrisCanvas.WinnerIcon = null;
    TetrisCanvas.LevelIcon = null;

    TetrisCanvas.display = null;
    myGameDisplay = null;

    saveStoredData(true);
  }

  /**************************************************
   * MIDLET LYFECYCLE
   **************************************************/
  public void destroyApp(boolean flag) {
    if (myGameDisplay != null)
      myGameDisplay.stopGame();
    TetrisCanvas.display = null;
    myGameDisplay = null;
    //notifyDestroyed();
    saveStoredData(true);
  }

  /**************************************************
   * MIDLET LYFECYCLE
   **************************************************/
  public void exitMidlet() {
    destroyApp(true);
    notifyDestroyed();
  }


  /**************************************************
   **************************************************/
  private static void readStoredData() {
    myState = "\n";

    if (myGameStateVector == null) {
      myGameStateVector = new Vector();

      try {
        if (myRS == null) {
          myRS = RecordStore.openRecordStore(RMS_NAME, true);
        }

//                Debug.print("Read RMS: #recs = "+myRS.getNumRecords()+" ["+myRS.getNextRecordID()+"]");
        // check record
        for (int i = 1; i <= myRS.getNumRecords(); i++) {
//                    Debug.print("Get record #" + i);

          byte[] data = myRS.getRecord(i);
//                    Debug.print("  done");
          if (i == 1) {
            if (data != null) {
              DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
              myState = dis.readUTF();
            }
          } else {
            //
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            myGameStateVector.addElement(dis.readUTF());
            //Debug.print("Read "+myGameStateVector.lastElement()+" game: "+dis.available()+" bytes");
            data = new byte[dis.available()];
            dis.read(data);
            myGameStateVector.addElement(data);

          }
        }


      } catch (Exception e) {
        Debug.print("Error reading RMS " + e);
        myRS = null;
        //myGameStateVector = new Vector();
      }
    }
  }

  /**************************************************
   **************************************************/
  private static void saveStoredData(boolean closeOnSave) {
    //Debug.print("myState: "+myState + ".");

    try {

      if (myRS != null) {

        byte[] data;

        int nRecord = 1;
        for (int i = -2; i < myGameStateVector.size(); i += 2) {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          DataOutputStream dos = new DataOutputStream(baos);

//                    Debug.print("Write @"+i);

          if (i == -2) { /// trick to save myState
            dos.writeUTF(myState);
          } else {
            dos.writeUTF((String) myGameStateVector.elementAt(i));
            dos.write((byte[]) myGameStateVector.elementAt(i + 1));
//                        Debug.print("Write game " + myGameStateVector.elementAt(i)
//                            +" : "+((byte[]) myGameStateVector.elementAt(i+1)).length+" bytes");
          }
          data = baos.toByteArray();

          if (nRecord <= myRS.getNumRecords()) {
            myRS.setRecord(nRecord, data, 0, data.length);
          } else {
            myRS.addRecord(data, 0, data.length);
          }
          nRecord++;
        }

        if (closeOnSave) {
          try {
            myRS.closeRecordStore();
          } finally {
            myRS = null;
          }
        }
      }
    } catch (Exception e) {
      Debug.print("Error writing RMS: " + e);
//            e.printStackTrace();
    }
  }


  public static final String TOGGLE_BOOLEAN_VALUE = "t";

  /**************************************************
   * props:
   * \n PROPNAME  \n NAME = VAL
   **************************************************/
  public static void setProperty(String prop, String value) {
//		Debug.print("------- set '"+ prop +"' = '"+ value +"'");

    String propMarker = "\n" + prop + '=';
    int index = myState.indexOf(propMarker);
    if (index == -1) {
      if (value != null) {
//                Debug.print("Append '"+ prop +"' = '"+ value +"'");
        myState = myState + propMarker.substring(1) + value + '\n';
      }
    } else {
      if (value == TOGGLE_BOOLEAN_VALUE) value = null;
//            Debug.print("Change '"+ prop +"' = '"+ value +"'");

      int next = myState.indexOf("\n", index + 1);
//			if(next == -1) next = myState.length();

      if (value == null) {
        // myState = myState.substring(0, index-1) + myState.substring(next);
        myState = myState.substring(0, index) + myState.substring(next);
      } else {
        myState = myState.substring(0, index + propMarker.length()) + value + myState.substring(next);
      }
    }
  }

  /**************************************************
   **************************************************/
//	public static int getIntProperty(String prop, int defaultValue){
//		return safeInt(getProperty(prop), defaultValue);
//	}

  /**************************************************
   **************************************************/
  public static int safeInt(String intString, int defaultValue) {
    if (intString != null && !"".equals(intString)) {
      try {
        defaultValue = Integer.parseInt(intString);
      } catch (Exception e) {
      }
    }
    return defaultValue;
  }

  /**************************************************
   **************************************************/
  public static String getProperty(String prop) {
    return getProperty(prop, myState);
  }

  /**************************************************
   **************************************************/
  public static String getProperty(String prop, String propertyString) {
    String propMarker = "\n" + prop + "=";
    String val = null;
    int index = propertyString.indexOf(propMarker);

//        System.out.println("Index "+ index +" of '"+ propMarker +"' in '"+ propertyString +"'");
    if (index != -1) {
      index += propMarker.length();
      int next = propertyString.indexOf("\n", index);
      val = propertyString.substring(index, next);
    }
    return val;
  }

}
