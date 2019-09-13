package dyvilx.tools.compiler.ast.reference;

import dyvil.lang.Name;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.asm.Handle;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.access.ICall;
import dyvilx.tools.compiler.ast.expression.constant.WildcardValue;
import dyvilx.tools.compiler.ast.method.IMethod;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.ClassFormat;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.compiler.util.Util;
import dyvilx.tools.parsing.marker.MarkerList;

public class PropertyReference implements IReference
{
	private static final Handle BOOTSTRAP = new Handle(ClassFormat.H_INVOKESTATIC, "dyvil/ref/ReferenceFactory",
	                                                   "propertyRefMetafactory",
	                                                   "(Ljava/lang/invoke/MethodHandles$Lookup;" + "Ljava/lang/String;"
	                                                   + "Ljava/lang/invoke/MethodType;"
	                                                   + "Ljava/lang/invoke/MethodHandle;"
	                                                   + "Ljava/lang/invoke/MethodHandle;)"
	                                                   + "Ljava/lang/invoke/CallSite;");

	private final IValue  receiver;
	private final IMethod getterMethod;
	private       IMethod setterMethod;

	public PropertyReference(IValue receiver, IMethod method)
	{
		this.receiver = receiver;
		this.getterMethod = method;
	}

	@Override
	public void checkTypes(SourcePosition position, MarkerList markers, IContext context)
	{
		final Name getterName = this.getterMethod.getName();
		final Name setterName = Util.addEq(getterName);
		this.setterMethod = ICall.resolveMethod(context, this.receiver, setterName,
		                                        new ArgumentList(new WildcardValue(null)));

		if (this.setterMethod == null || this.setterMethod.getParameters().size() != 1)
		{
			markers.add(Markers.semanticError(position, "reference.property.no_setter", getterName));
		}
	}

	@Override
	public void writeReference(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		StringBuilder desc = new StringBuilder().append('(');

		if (this.receiver != null)
		{
			this.receiver.writeExpression(writer, null);

			this.receiver.getType().appendExtendedName(desc);
		}

		final IType methodType = this.getterMethod.getType();
		final String getterName = this.getterMethod.getInternalName();

		desc.append(')').append('L').append(ReferenceType.LazyFields.getInternalRef(methodType, "")).append(';');

		final Handle getterHandle = this.getterMethod.toHandle();
		final Handle setterHandle = this.setterMethod.toHandle();

		writer.visitLineNumber(lineNumber);
		writer.visitInvokeDynamicInsn(getterName, desc.toString(), BOOTSTRAP, getterHandle, setterHandle);
	}
}
