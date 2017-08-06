package dyvilx.tools.repl;

import dyvilx.tools.repl.input.InputManager;

import java.io.IOException;

public final class Main
{
	protected static final long SLEEP_TIME = 4L;

	public static void main(String[] args) throws Exception
	{
		final DyvilREPL instance = new DyvilREPL(System.out, System.err);
		final InputManager inputManager = new InputManager(System.out, System.in);

		instance.launch(args);

		do
		{
			if (!readAndProcess(instance, inputManager))
			{
				break;
			}

			// Wait to make sure the output isn't messed up in IDE consoles.
			try
			{
				Thread.sleep(SLEEP_TIME);
			}
			catch (InterruptedException ignored)
			{
			}
		}
		while (true);

		instance.shutdown();
	}

	private static boolean readAndProcess(DyvilREPL repl, InputManager inputManager)
	{
		System.out.print("> ");

		final String currentCode;
		try
		{
			currentCode = inputManager.readInput();
			if (currentCode == null)
			{
				return false;
			}
		}
		catch (IOException ignored)
		{
			return false;
		}

		repl.processInput(currentCode);
		return true;
	}
}
