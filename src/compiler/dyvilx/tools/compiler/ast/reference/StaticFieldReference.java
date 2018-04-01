package dyvilx.tools.compiler.ast.reference;

import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Handle;
import dyvilx.tools.asm.Type;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.field.IField;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;

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
	public void writeReference(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		final String internalClassName = this.field.getEnclosingClass().getInternalName();

		final String refClassName = ReferenceType.LazyFields.getInternalRef(this.field.getType(), "unsafe/Unsafe");
		final String desc = "()L" + refClassName + ';';

		writer.visitInvokeDynamicInsn(this.field.getInternalName(), desc, BOOTSTRAP,
		                              Type.getObjectType(internalClassName));
	}
}
