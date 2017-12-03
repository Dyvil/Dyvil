package dyvilx.tools.gensrc.ast.directive;

import dyvil.annotation.internal.NonNull;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.statement.StatementList;
import dyvilx.tools.compiler.ast.statement.VariableStatement;

public class VarDirective extends VariableStatement
{
	public VarDirective(IVariable variable)
	{
		super(variable);
	}

	public void setBlock(StatementList block)
	{
		this.variable.setValue(FuncDirective.convertBlock(block));
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		buffer.append('#');
		super.toString(indent, buffer);
		// TODO
	}
}
