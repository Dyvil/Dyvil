package dyvil.tools.compiler.ast.statement;

import jdk.internal.org.objectweb.asm.Label;

public interface ILoop
{
	public Label getStartLabel();
	
	public Label getEndLabel();
}
