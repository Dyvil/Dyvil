package dyvilx.tools.compiler.ast.reference;

import dyvil.reflect.Opcodes;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.access.FieldAccess;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public class VariableReference implements IReference
{
	private final FieldAccess fieldAccess;

	public VariableReference(FieldAccess fieldAccess)
	{
		this.fieldAccess = fieldAccess;
	}

	private IVariable getVariable()
	{
		return (IVariable) this.fieldAccess.getField();
	}

	@Override
	public void check(SourcePosition position, MarkerList markers, IContext context)
	{
		InstanceFieldReference.checkFinalAccess(this.fieldAccess.getField(), position, markers);
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.getVariable().setReferenceType();
	}

	@Override
	public void writeReference(MethodWriter writer) throws BytecodeException
	{
		// Assumes that the variable was properly converted to a Reference Variable
		writer.visitVarInsn(Opcodes.ALOAD, this.getVariable().getLocalIndex());
	}
}
