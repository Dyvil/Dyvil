package dyvil.tools.compiler.ast.reference;

import dyvil.reflect.Modifiers;
import dyvil.tools.asm.Opcodes;
import dyvil.tools.asm.Type;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.MarkerMessages;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class InstanceFieldReference implements IReference
{
	private final IValue receiver;
	private final IField field;

	public InstanceFieldReference(IValue receiver, IField field)
	{
		this.receiver = receiver;
		this.field = field;
	}

	public static void checkFinalAccess(IDataMember field, ICodePosition position, MarkerList markers)
	{
		if (field.hasModifier(Modifiers.FINAL))
		{
			markers.add(MarkerMessages.createError(position, "reference.field.final", field.getName()));
		}
	}

	@Override
	public void check(ICodePosition position, MarkerList markers)
	{
		checkFinalAccess(this.field, position, markers);
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
	}

	@Override
	public void writeReference(MethodWriter writer) throws BytecodeException
	{
		// Write the receiver
		this.receiver.writeExpression(writer, null);

		final String fieldClassName = this.field.getTheClass().getInternalName();
		final String fieldName = this.field.getName().qualified;
		final String factoryMethodName = Types.getAccessFactoryName(this.field.getType(), false);
		final String factoryMethodType = "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;)L" + Types
				.getInternalRef(this.field.getType(), "") + ';';

		writer.writeLDC(Type.getObjectType(fieldClassName));
		writer.writeLDC(fieldName);

		// Write a call to the access factory method
		writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/runtime/ReferenceFactory", factoryMethodName,
		                       factoryMethodType, false);
	}
}
