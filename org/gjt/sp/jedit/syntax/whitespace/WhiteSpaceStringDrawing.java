package org.gjt.sp.jedit.syntax.whitespace;

import java.awt.Color;

import org.gjt.sp.jedit.syntax.Chunk;

public class WhiteSpaceStringDrawing {

	public static StringDrawing tab(int xOffset, int yOffset) {
		return new StringDrawing("  > ", Color.red, xOffset, yOffset);
	}

	public static StringDrawing space(int xOffset, int yOffset) {
		return new StringDrawing("_", Color.red, xOffset, yOffset);
	}

}