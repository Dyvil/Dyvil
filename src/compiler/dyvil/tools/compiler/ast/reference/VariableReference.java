package dyvil.tools.compiler.ast.reference;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.access.FieldAccess;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

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
	public void check(ICodePosition position, MarkerList markers, IContext context)
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
