package dyvilx.tools.compiler.ast.reference;

import dyvilx.tools.asm.Handle;
import dyvilx.tools.asm.Type;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.field.IField;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public class StaticFieldReference implements IReference
{
	private static final Handle BOOTSTRAP = new Handle(ClassFormat.H_INVOKESTATIC, "dyvil/ref/ReferenceFactory",
	                                                   "staticRefMetafactory",
	                                                   ClassFormat.BSM_HEAD + "Ljava/lang/Class;"
		                                                   + ClassFormat.BSM_TAIL);

	private IField field;

	public StaticFieldReference(IField field)
	{
		this.field = field;
	}

	@Override
	public void check(SourcePosition position, MarkerList markers, IContext context)
	{
		InstanceFieldReference.checkFinalAccess(this.field, position, markers);
	}

	@Override
	public void writeReference(MethodWriter writer) throws BytecodeException
	{
		final String internalClassName = this.field.getEnclosingClass().getInternalName();
		final IType fieldType = this.field.getType();

		final String desc = "()L" + ReferenceType.LazyFields.getInternalRef(fieldType, "unsafe/Unsafe") + ';';
		writer.visitInvokeDynamicInsn(this.field.getInternalName(), desc, BOOTSTRAP,
		                              Type.getObjectType(internalClassName));
	}
}
