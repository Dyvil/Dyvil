package dyvil.tools.compiler.ast.reference;

import dyvil.tools.asm.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class VariableReference implements IReference
{
	private final IVariable variable;

	public VariableReference(IVariable variable)
	{
		this.variable = variable;
	}

	@Override
	public void check(ICodePosition position, MarkerList markers)
	{
		if (!this.variable.isReferenceCapturable())
		{
			markers.add(I18n.createError(position, "reference.parameter.capture", this.variable.getName()));

			// Return to avoid two errors
			return;
		}

		InstanceFieldReference.checkFinalAccess(this.variable, position, markers);
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.variable.setReferenceType();
	}

	@Override
	public void writeReference(MethodWriter writer) throws BytecodeException
	{
		// Assumes that the variable was properly converted to a Reference Variable
		writer.writeVarInsn(Opcodes.ALOAD, this.variable.getLocalIndex());
	}
}
