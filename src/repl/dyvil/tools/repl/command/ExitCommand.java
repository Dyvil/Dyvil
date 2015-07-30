package dyvil.tools.repl.command;

public class ExitCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "exit";
	}
	
	@Override
	public String getDescription()
	{
		return "Exits the current REPL instance";
	}
	
	@Override
	public void execute(String... args)
	{
		if (args.length == 0)
		{
			System.exit(0);
			return;
		}
		
		int code = 0;
		try
		{
			code = Integer.parseInt(args[0]);
		}
		catch (NumberFormatException ex)
		{
			System.out.println("Invalid Exit Code " + args[0]);
		}
		System.exit(code);
		return;
	}
	
}
