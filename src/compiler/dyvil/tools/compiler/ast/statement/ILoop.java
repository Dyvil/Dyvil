package dyvil.tools.compiler.ast.statement;

import jdk.internal.org.objectweb.asm.Label;

public interface ILoop
{
	public Label getContinueLabel();
	
	public Label getBreakLabel();
}
