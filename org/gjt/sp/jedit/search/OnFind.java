package org.gjt.sp.jedit.search;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.search.SearchMatcher.Match;
import org.gjt.sp.jedit.textarea.Selection.Range;

public abstract class OnFind
{

	public static final OnFind DO_NOTHING = new OnFind()
	{

		public void found(Match match, Buffer buffer, int caratPosition)
		{
			// Do nothing
		}
	};

	public abstract void found(Match match, Buffer buffer, int caratPosition) throws Exception;

}
