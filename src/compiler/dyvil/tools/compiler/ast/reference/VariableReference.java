package dyvil.tools.compiler.ast.reference;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.access.FieldAccess;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
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
		IVariable variable = this.getVariable();
		if (!variable.isReferenceCapturable())
		{
			markers.add(Markers.semanticError(position, "reference.parameter.capture", this.fieldAccess.getName()));

			// Return to avoid two errors
			return;
		}

		InstanceFieldReference.checkFinalAccess(variable, position, markers);
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
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
