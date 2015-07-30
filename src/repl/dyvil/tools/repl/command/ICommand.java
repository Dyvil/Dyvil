package dyvil.tools.repl.command;

public interface ICommand
{
	public String getName();
	
	public String getDescription();
	
	public void execute(String... args);
}
