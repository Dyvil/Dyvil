package dyvilx.tools.compiler.ast.reference;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;

public class VariableReference implements IReference
{
	private IDataMember variable;

	public VariableReference(IDataMember variable)
	{
		this.variable = variable;
	}

	public IDataMember getVariable()
	{
		return this.variable;
	}

	@Override
	public void check(SourcePosition position, MarkerList markers, IContext context)
	{
		InstanceFieldReference.checkFinalAccess(this.variable, position, markers);
	}

	@Override
	public void writeReference(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		// Assumes that the variable was properly converted to a Reference Variable
		this.variable.writeGetRaw(writer, null, lineNumber);
	}
}
