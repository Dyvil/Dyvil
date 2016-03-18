package dyvil.tools.compiler.ast.reference;

import dyvil.reflect.Modifiers;
import dyvil.tools.asm.Opcodes;
import dyvil.tools.asm.Type;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
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
			markers.add(Markers.semanticError(position, "reference.field.final", field.getName()));
		}
	}

	@Override
	public void check(ICodePosition position, MarkerList markers, IContext context)
	{
		checkFinalAccess(this.field, position, markers);
	}

	@Override
	public void writeReference(MethodWriter writer) throws BytecodeException
	{
		// Write the receiver
		this.receiver.writeExpression(writer, null);

		final IType fieldType = this.field.getType();
		final String fieldClassName = this.field.getEnclosingClass().getInternalName();
		final String fieldName = this.field.getName().qualified;
		final String factoryMethodName = Types.getReferenceFactoryName(fieldType, "");
		final String factoryMethodType =
				"(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;)L" + Types.getInternalRef(fieldType, "") + ';';

		writer.visitLdcInsn(Type.getObjectType(fieldClassName));
		writer.visitLdcInsn(fieldName);

		// Write a call to the access factory method
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/ref/ReferenceFactory", factoryMethodName, factoryMethodType,
		                       false);
	}
}
