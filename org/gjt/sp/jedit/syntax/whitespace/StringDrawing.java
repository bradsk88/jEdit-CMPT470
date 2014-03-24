package org.gjt.sp.jedit.syntax.whitespace;

import java.awt.Color;

public class StringDrawing {

	private String stringRep;
	private Color color;
	private int x;
	private int y;

	public StringDrawing(String stringRep, Color color, int x, int y) {
		super();
		this.stringRep = stringRep;
		this.color = color;
		this.x = x;
		this.y = y;
	}

	public String getStringRep() {
		return stringRep;
	}

	public Color getColor() {
		return color;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

}