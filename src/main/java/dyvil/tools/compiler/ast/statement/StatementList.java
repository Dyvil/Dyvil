package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.CompilerState;
import dyvil.tools.compiler.ast.expression.ValueList;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class StatementList extends ValueList implements IStatement
{
	public StatementList(ICodePosition position)
	{
		super(position);
	}
	
	public void addStatement(IStatement statement)
	{
		this.values.add(statement);
	}
	
	@Override
	public IValue applyState(CompilerState state, IContext context)
	{
		if (state == CompilerState.FOLD_CONSTANTS)
		{
			if (this.values.size() == 1)
			{
				return this.values.get(0);
			}
		}
		this.values.replaceAll(v -> v.applyState(state, context));
		return this;
	}
}
