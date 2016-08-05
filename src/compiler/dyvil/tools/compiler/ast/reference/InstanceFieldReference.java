package dyvil.tools.compiler.ast.reference;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Type;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class InstanceFieldReference extends AbstractFieldReference
{
	private final IValue receiver;

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
	public void write(ClassWriter writer) throws BytecodeException
	{
		if (!this.isUnique)
		{
			return;
		}

		writer.visitField(CACHE_FIELD_MODIFIERS, this.getRefFieldName(), "J", null, null);
	}

	@Override
	public void writeStaticInit(MethodWriter writer) throws BytecodeException
	{
		if (!this.isUnique)
		{
			return;
		}

		final String fieldName = this.field.getInternalName();
		final String fieldOriginClassName = this.getFieldOriginClassName();

		final String refFieldName = this.getRefFieldName();

		// Load the field class
		writer.visitLdcInsn(Type.getObjectType(fieldOriginClassName));
		// Load the field name
		writer.visitLdcInsn(fieldName);

		// Invoke the factory method
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/ref/ReferenceFactory", "getObjectFieldOffset",
		                       "(Ljava/lang/Class;Ljava/lang/String;)J", false);

		// Assign the reference field
		writer.visitFieldInsn(Opcodes.PUTSTATIC, this.className, refFieldName, "J");
	}

	@Override
	public void writeReference(MethodWriter writer) throws BytecodeException
	{
		// Write the receiver
		this.receiver.writeExpression(writer, null);

		final IType fieldType = this.field.getType();
		final String factoryMethodName = ReferenceType.LazyFields.getReferenceFactoryName(fieldType, "");
		final String factoryMethodType =
			"(Ljava/lang/Object;J)L" + ReferenceType.LazyFields.getInternalRef(fieldType, "") + ';';

		writer.visitFieldInsn(Opcodes.GETSTATIC, this.className, this.getRefFieldName(), "J");

		// Write a call to the access factory method
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/ref/ReferenceFactory", factoryMethodName, factoryMethodType,
		                       false);
	}
}
