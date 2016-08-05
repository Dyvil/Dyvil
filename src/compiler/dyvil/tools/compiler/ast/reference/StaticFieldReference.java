package dyvil.tools.compiler.ast.reference;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.asm.Type;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class StaticFieldReference extends AbstractFieldReference
{
	private String refFieldType;

	public StaticFieldReference(IField field)
	{
		this.field = field;
	}

	@Override
	public void check(ICodePosition position, MarkerList markers, IContext context)
	{
		InstanceFieldReference.checkFinalAccess(this.field, position, markers);
	}

	// IClassCompilable callback implementations

	protected String getRefFieldType()
	{
		if (this.refFieldType != null)
		{
			return this.refFieldType;
		}

		return this.refFieldType = 'L' + ReferenceType.LazyFields.getInternalRef(this.field.getType(), "") + ';';
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		if (!this.isUnique)
		{
			return;
		}

		String refFieldName = this.getRefFieldName();
		String refFieldType = this.getRefFieldType();
		writer.visitField(CACHE_FIELD_MODIFIERS, refFieldName, refFieldType, null, null);
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
		final String refFieldType = this.getRefFieldType();

		final String factoryMethodName = ReferenceType.LazyFields
			                                 .getReferenceFactoryName(this.field.getType(), "Static");
		final String factoryMethodType = "(Ljava/lang/Class;Ljava/lang/String;)" + refFieldType;

		// Load the field class
		writer.visitLdcInsn(Type.getObjectType(fieldOriginClassName));
		// Load the field name
		writer.visitLdcInsn(fieldName);

		// Invoke the factory method
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/ref/ReferenceFactory", factoryMethodName, factoryMethodType,
		                       false);

		// Assign the reference field
		writer.visitFieldInsn(Opcodes.PUTSTATIC, this.className, refFieldName, refFieldType);
	}

	// Reference getter implementation

	@Override
	public void writeReference(MethodWriter writer) throws BytecodeException
	{
		if (this.field.hasModifier(Modifiers.STATIC))
		{
			writer.visitFieldInsn(Opcodes.GETSTATIC, this.className, this.getRefFieldName(), this.getRefFieldType());
		}
	}
}
