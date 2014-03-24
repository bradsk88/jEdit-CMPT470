/*
 * Chunk.java - A syntax token with extra information required for painting it
 * on screen
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2001, 2002 Slava Pestov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.gjt.sp.jedit.syntax;

//{{{ Imports
import javax.swing.text.*;

import java.awt.font.*;
import java.awt.geom.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

import org.gjt.sp.jedit.Debug;
//}}}


import org.gjt.sp.jedit.syntax.whitespace.NormalStringDrawing;
import org.gjt.sp.jedit.syntax.whitespace.StringDrawing;
import org.gjt.sp.jedit.syntax.whitespace.WhiteSpaceStringDrawing;

import sun.font.StandardGlyphVector;

/**
 * A syntax token with extra information required for painting it
 * on screen.
 * @since jEdit 4.1pre1
 */
public class Chunk extends Token
{
	private static final int TAB_WIDTH = 8;
	private static final int SPACE_WIDTH = 1;

	//{{{ paintChunkList() method
	/**
	 * Paints a chunk list.
	 * @param chunks The chunk list
	 * @param gfx The graphics context
	 * @param x The x co-ordinate
	 * @param y The y co-ordinate
	 * @return The width of the painted text
	 * @since jEdit 4.2pre1
	 */
	public static float paintChunkList(Chunk chunks,
		Graphics2D gfx, float x, float y, boolean glyphVector)
	{
		return paintChunkList(chunks, gfx, x, y, glyphVector, ShowMarks.NONE);
	} // }}}

	public static float paintChunkList(Chunk chunks, Graphics2D gfx, float x,
			float y, boolean glyphVector, ShowMarks marks) {
		Rectangle clipRect = gfx.getClipBounds();

		float _x = 0.0f;

		while(chunks != null)
		{
			// only paint visible chunks
			if(x + _x + chunks.width > clipRect.x
				&& x + _x < clipRect.x + clipRect.width)
			{
				// Useful for debugging purposes
				if(Debug.CHUNK_PAINT_DEBUG)
				{
					gfx.draw(new Rectangle2D.Float(x + _x,y - 10,
						chunks.width,10));
				}

				if(chunks.accessable && chunks.visible)
				{
					gfx.setFont(chunks.style.getFont());
					gfx.setColor(chunks.style.getForegroundColor());

					if(glyphVector && chunks.gv != null)
						drawGlyphs(gfx, chunks.gv, (int) (x + _x), (int) y,
								marks);
					else if (chunks.str != null) {
						drawString(gfx, chunks.str, (int) (x + _x), (int) y,
								marks);
					} else {
						System.err.println("Was null?");
					}
				}
				if (isTabChunk(chunks) && marks == ShowMarks.WHITESPACE) {
					drawTab(gfx, (int) (x + _x), (int) y, chunks.width);
				}
			}

			_x += chunks.width;
			chunks = (Chunk)chunks.next;
		}

		return _x;
	} //}}}

	private static float drawTab(Graphics2D gfx, int i, int y, float width) {
		FontRenderContext context = gfx.getFontRenderContext();
		GlyphVector createGlyphVector = shrinkTabToFit(gfx, "------->", width,
				context);
		gfx.setColor(Color.red);
		gfx.drawGlyphVector(createGlyphVector, i, y);
		return getTotalWidth(createGlyphVector);
	}

	private static GlyphVector shrinkTabToFit(Graphics2D gfx, String string,
			float width2, FontRenderContext context) {
		GlyphVector createGlyphVector = gfx.getFont().createGlyphVector(
				context, string);
		float totalWidth = getTotalWidth(createGlyphVector);
		if (totalWidth > width2 + 2) {
			return shrinkTabToFit(gfx, string.substring(1, string.length()),
					width2, context);
		}
		return createGlyphVector;
	}

	private static boolean isTabChunk(Chunk chunks) {
		// I don't think this is the best check. But it works.
		return chunks.str == null && chunks.width >= 7;
	}

	private static void drawGlyphs(Graphics2D gfx, GlyphVector gv2, int x, int y, ShowMarks marks) {
		if (marks != ShowMarks.WHITESPACE) {
			gfx.drawGlyphVector(gv2, x, y);
			return;
		}
		drawGlyphsWithWhitespaceMarkers(gfx, gv2, x, y); 
	}

	private static void drawGlyphsWithWhitespaceMarkers(Graphics2D gfx,
			GlyphVector gv2, int x, int y) {
		Font font = gv2.getFont();
		FontRenderContext context = gv2.getFontRenderContext();
		float useX = x;
		for (int i = 0; i < gv2.getNumGlyphs(); i++) {
			int glyphCode = gv2.getGlyphCode(i);
			if (isSpaceGlyph(glyphCode)) {
				int[] ints = { 66 };
				useX += drawGlyphs(gfx, font, context, ints, Color.red, useX, y);
				continue;
			}
			int[] ints = { glyphCode };
			useX += drawGlyphs(gfx, font, context, ints, Color.black, useX, y);
			continue;
		}
	}

	private static boolean isSpaceGlyph(int glyphCode) {
		return glyphCode == 3;
	}

	private static float drawGlyphs(Graphics2D gfx, Font font,
			FontRenderContext context, int[] ints, Color red, float x, float y) {
		gfx.setColor(red);
		StandardGlyphVector newGV = new StandardGlyphVector(font, ints, context);
		gfx.drawGlyphVector(newGV, x, y);
		return getTotalWidth(newGV);
	}

	private static float getTotalWidth(GlyphVector createGlyphVector) {
		float answer = 0;
		for (int i = 0; i < createGlyphVector.getNumGlyphs(); i++) {
			GlyphMetrics metrics = createGlyphVector.getGlyphMetrics(i);
			answer += metrics.getBounds2D().getWidth();
			answer += metrics.getLSB();
			answer += metrics.getRSB();
		}
		return answer;
	}

	private static void drawString(Graphics2D gfx, String str2, int x, int y, ShowMarks marks) {
		if (marks != ShowMarks.WHITESPACE) {
			gfx.drawString(str2, x, y); 
			return;
		}
		
		Collection<StringDrawing> stringComponents = getComponents(x, y,
				str2); 
		for (StringDrawing i : stringComponents) {
			gfx.setColor(i.getColor());
			gfx.drawString(i.getStringRep(), i.getX(), i.getY());
		}
	}

	private static Collection<StringDrawing> getComponents(int xOffsetIn,
			int yOffsetIn, String str2) {

		int xOffset = xOffsetIn;
		int yOffset = yOffsetIn;

		Collection<StringDrawing> collection = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		for (char i : str2.toCharArray()) {
			if (i == '\t' || i == ' ') {
				if (sb.length() > 0) {
					collection.add(new NormalStringDrawing(xOffset, yOffset, sb
							.toString()));
					xOffset += sb.length();
					sb.setLength(0);
				}
			}

			if (i == '\t') {
				collection.add(WhiteSpaceStringDrawing.tab(xOffset, yOffset));
				xOffset += TAB_WIDTH;
				continue;
			}
			if (i == ' ') {
				collection.add(WhiteSpaceStringDrawing.space(xOffset, yOffset));
				xOffset += SPACE_WIDTH;
				continue;
			}
			sb.append(i);
		}
		return collection;
	}

	//{{{ paintChunkBackgrounds() method
	/**
	 * Paints the background highlights of a chunk list.
	 * @param chunks The chunk list
	 * @param gfx The graphics context
	 * @param x The x co-ordinate
	 * @param y The y co-ordinate
	 * @return The width of the painted backgrounds
	 * @since jEdit 4.2pre1
	 */
	public static float paintChunkBackgrounds(Chunk chunks,
		Graphics2D gfx, float x, float y)
	{
		Rectangle clipRect = gfx.getClipBounds();

		float _x = 0.0f;

		FontMetrics forBackground = gfx.getFontMetrics();

		int ascent = forBackground.getAscent();
		int height = forBackground.getHeight();

		while(chunks != null)
		{
			// only paint visible chunks
			if(x + _x + chunks.width > clipRect.x
				&& x + _x < clipRect.x + clipRect.width)
			{
				if(chunks.accessable)
				{
					//{{{ Paint token background color if necessary
					Color bgColor = chunks.background;
					if(bgColor != null)
					{
						gfx.setColor(bgColor);

						gfx.fill(new Rectangle2D.Float(
							x + _x,y - ascent,
							_x + chunks.width - _x,
							height));
					} //}}}
				}
			}

			_x += chunks.width;
			chunks = (Chunk)chunks.next;
		}

		return _x;
	} //}}}

	//{{{ offsetToX() method
	/**
	 * Converts an offset in a chunk list into an x co-ordinate.
	 * @param chunks The chunk list
	 * @param offset The offset
	 * @since jEdit 4.1pre1
	 */
	public static float offsetToX(Chunk chunks, int offset)
	{
		if(chunks != null && offset < chunks.offset)
		{
			throw new ArrayIndexOutOfBoundsException(offset + " < "
				+ chunks.offset);
		}

		float x = 0.0f;

		while(chunks != null)
		{
			if(chunks.accessable && offset < chunks.offset + chunks.length)
				return x + chunks.offsetToX(offset - chunks.offset);

			x += chunks.width;
			chunks = (Chunk)chunks.next;
		}

		return x;
	} //}}}

	//{{{ xToOffset() method
	/**
	 * Converts an x co-ordinate in a chunk list into an offset.
	 * @param chunks The chunk list
	 * @param x The x co-ordinate
	 * @param round Round up to next letter if past the middle of a letter?
	 * @return The offset within the line, or -1 if the x co-ordinate is too
	 * far to the right
	 * @since jEdit 4.1pre1
	 */
	public static int xToOffset(Chunk chunks, float x, boolean round)
	{
		float _x = 0.0f;

		while(chunks != null)
		{
			if(chunks.accessable && x < _x + chunks.width)
				return chunks.xToOffset(x - _x,round);

			_x += chunks.width;
			chunks = (Chunk)chunks.next;
		}

		return -1;
	} //}}}

	//{{{ Instance variables
	public boolean accessable;
	public boolean visible;
	public boolean initialized;

	// set up after init()
	public SyntaxStyle style;
	// this is either style.getBackgroundColor() or
	// styles[defaultID].getBackgroundColor()
	public Color background;
	public float width;
	public GlyphVector gv;
	public String str;
	//}}}

	//{{{ Chunk constructor
	public Chunk(float width, int offset, ParserRuleSet rules)
	{
		super(Token.NULL,offset,0,rules);
		this.width = width;
	} //}}}

	//{{{ Chunk constructor
	public Chunk(byte id, int offset, int length, ParserRuleSet rules,
		SyntaxStyle[] styles, byte defaultID)
	{
		super(id,offset,length,rules);
		accessable = true;
		style = styles[id];
		background = style.getBackgroundColor();
		if(background == null)
			background = styles[defaultID].getBackgroundColor();
	} //}}}

	//{{{ getPositions() method
	public final float[] getPositions()
	{
		if(gv == null)
			return null;

		if(positions == null)
			positions = gv.getGlyphPositions(0,length,null);

		return positions;
	} //}}}

	//{{{ offsetToX() method
	public final float offsetToX(int offset)
	{
		if(!visible)
			return 0.0f;
		else
			return getPositions()[offset * 2];
	} //}}}

	//{{{ xToOffset() method
	public final int xToOffset(float x, boolean round)
	{
		if(!visible)
		{
			if(round && width - x < x)
				return offset + length;
			else
				return offset;
		}
		else
		{
			float[] pos = getPositions();

			for(int i = 0; i < length; i++)
			{
				float glyphX = pos[i*2];
				float nextX = (i == length - 1
					? width : pos[i*2+2]);

				if(nextX > x)
				{
					if(!round || nextX - x > x - glyphX)
						return offset + i;
					else
						return offset + i + 1;
				}
			}
		}

		// wtf?
		return -1;
	} //}}}

	//{{{ init() method
	public void init(Segment seg, TabExpander expander, float x,
		FontRenderContext fontRenderContext)
	{
		initialized = true;

		if(!accessable)
		{
			// do nothing
		}
		else if(length == 1 && seg.array[seg.offset + offset] == '\t')
		{
			visible = false;
			float newX = expander.nextTabStop(x,offset + length);
			width = newX - x;
		}
		else
		{
			visible = true;

			str = new String(seg.array,seg.offset + offset,length);
			
			Rectangle2D logicalBounds;
			gv = style.getFont().createGlyphVector(
				fontRenderContext, str);
			logicalBounds = gv.getLogicalBounds();

			width = (float)logicalBounds.getWidth();
		}
	} //}}}

	//{{{ Private members
	private float[] positions;
	//}}}
}
