package dyvil.tools.compiler.ast.reference;

import dyvil.reflect.Modifiers;
import dyvil.tools.asm.Handle;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public class InstanceFieldReference implements IReference
{
	private static final Handle BOOTSTRAP = new Handle(ClassFormat.H_INVOKESTATIC, "dyvil/ref/ReferenceFactory",
	                                                   "instanceRefMetafactory",
	                                                   ClassFormat.BSM_HEAD + ClassFormat.BSM_TAIL);

	private final IField field;
	private final IValue receiver;

	public InstanceFieldReference(IValue receiver, IField field)
	{
		this.receiver = receiver;
		this.field = field;
	}

	public static void checkFinalAccess(IDataMember field, SourcePosition position, MarkerList markers)
	{
		if (field.hasModifier(Modifiers.FINAL))
		{
			markers.add(Markers.semanticError(position, "reference.field.final", field.getName()));
		}
	}

	@Override
	public void check(SourcePosition position, MarkerList markers, IContext context)
	{
		checkFinalAccess(this.field, position, markers);
	}

	@Override
	public void writeReference(MethodWriter writer) throws BytecodeException
	{
		// Write the receiver
		this.receiver.writeExpression(writer, null);

		final String internalClassName = this.field.getEnclosingClass().getInternalName();
		final IType fieldType = this.field.getType();

		final String desc =
			"(L" + internalClassName + ";)L" + ReferenceType.LazyFields.getInternalRef(fieldType, "unsafe/Unsafe") + ';';
		writer.visitInvokeDynamicInsn(this.field.getInternalName(), desc, BOOTSTRAP);
	}
}
