package dyvil.tools.compiler.ast.statement;

public interface ILoop
{
	public Label getContinueLabel();
	
	public Label getBreakLabel();
}
