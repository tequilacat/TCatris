package org.tequilacat.tcatris.core;

public class Color {
  /* public int r=0,g=0,b=0;
	
	public Color(int r, int g, int b){
		this.r = r; this.b = b; this.g = g;
	}
		
	public int intValue(){
		return (r << 16) | (g << 8) | b;
	} */

  public static int intValue(int r, int g, int b) {
    return (r << 16) | (g << 8) | b;
  }

  public final static int white = Color.intValue(255, 255, 255);
  public final static int lightGray = Color.intValue(192, 192, 192);
  public final static int gray = Color.intValue(128, 128, 128);
  public final static int darkGray = Color.intValue(64, 64, 64);
  public final static int black = Color.intValue(0, 0, 0);

  public final static int red = Color.intValue(255, 0, 0);
  public final static int pink = Color.intValue(255, 175, 175);
  public final static int orange = Color.intValue(255, 200, 0);
  public final static int yellow = Color.intValue(255, 255, 0);
  public final static int green = Color.intValue(0, 255, 0);
  public final static int magenta = Color.intValue(255, 0, 255);
  public final static int purple = Color.intValue(0x80, 00, 0x80);
  public final static int cyan = Color.intValue(0, 255, 255);
  public final static int blue = Color.intValue(0, 0, 255);
  public final static int lightBlue = Color.intValue(183, 228, 255);

  public final static int brown = Color.intValue(128, 64, 0);
  public final static int lightBrown = Color.intValue(221, 111, 0);

  public final static int darkGreen = Color.intValue(0, 0x80, 0);
  public final static int darkYellow = Color.intValue(0x80, 0x80, 0);
  public final static int darkRed = Color.intValue(0x80, 0, 0);
  public final static int darkCyan = Color.intValue(0, 0x80, 0x80);
}

