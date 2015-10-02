package dyvil.tools.compiler.ast.statement;

import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class AppliedStatementList extends StatementList
{
	public AppliedStatementList()
	{
	}
	
	public AppliedStatementList(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		super.resolve(markers, context);
		
		return super.withType(type, typeContext, markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		// Values are resolved in withType
		return this;
	}
}
