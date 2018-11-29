package dyvilx.tools.repl;

import dyvilx.tools.repl.input.InputManager;

public final class Main
{
	protected static final long SLEEP_TIME = 4L;

	public static void main(String[] args) throws Exception
	{
		final DyvilREPL instance = new DyvilREPL(System.out, System.err);

		instance.launch(args);

		final InputManager inputManager = new InputManager();
		while (readAndProcess(instance, inputManager))
		{
			// Wait to make sure the output isn't messed up in IDE consoles.
			try
			{
				Thread.sleep(SLEEP_TIME);
			}
			catch (InterruptedException ignored)
			{
			}
		}

		instance.shutdown();
	}

	private static boolean readAndProcess(DyvilREPL repl, InputManager inputManager)
	{
		final String currentCode;
		currentCode = inputManager.readInput();
		if (currentCode == null)
		{
			return false;
		}

		repl.processInput(currentCode);
		return true;
	}
}
