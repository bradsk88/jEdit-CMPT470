package org.gjt.sp.jedit.gui;

class AnimationThread extends Thread
{
	private boolean running = true;
	private long last;
	private final ScrollPositionAccepting panel;

	AnimationThread(ScrollPositionAccepting panel)
	{
		super("About box animation thread");
		setPriority(Thread.MIN_PRIORITY);
		this.panel = panel;
	}
	
	public void kill()
	{
		running = false;
	}

	public void run()
	{

		while (running)
		{
			panel.incrementScroll();


			if(last != 0)
			{
				long frameDelay =
					System.currentTimeMillis()
					- last;

				try
				{
					Thread.sleep(
						75
						- frameDelay);
				}
				catch(Exception e)
				{
				}
			}

			last = System.currentTimeMillis();
			panel.repaintInPlace();
		}
	}
}