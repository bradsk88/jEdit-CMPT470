package org.gjt.sp.jedit.syntax.whitespace;

import java.awt.Color;

import org.gjt.sp.jedit.syntax.Chunk;

public class NormalStringDrawing extends StringDrawing {

	public NormalStringDrawing(int xOffset, int yOffset, String string) {
		super(string, Color.black, xOffset, yOffset);
	}

}