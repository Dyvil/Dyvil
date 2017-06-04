package dyvil.tools.compiler.ast.statement.loop;

import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.statement.IStatement;
import dyvil.source.position.SourcePosition;

public interface IForStatement extends IStatement, ILoop
{
	@Override
	SourcePosition getPosition();

	@Override
	void setPosition(SourcePosition position);

	IVariable getVariable();

	void setVariable(IVariable variable);

	@Override
	IValue getAction();

	@Override
	void setAction(IValue action);
}
