package dyvil.tools.compiler.ast.reference;

import dyvil.tools.asm.Handle;
import dyvil.tools.compiler.ast.access.ICall;
import dyvil.tools.compiler.ast.constant.WildcardValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

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
	public void checkTypes(ICodePosition position, MarkerList markers, IContext context)
	{
		final Name getterName = this.getterMethod.getName();
		final Name setterName = Util.addEq(getterName);
		this.setterMethod = ICall.resolveMethod(context, this.receiver, setterName,
		                                        new SingleArgument(new WildcardValue(null)));

		if (this.setterMethod == null || this.setterMethod.getParameterList().size() != 1)
		{
			markers.add(Markers.semanticError(position, "reference.property.no_setter", getterName));
		}
	}

	@Override
	public void writeReference(MethodWriter writer) throws BytecodeException
	{
		StringBuilder desc = new StringBuilder().append('(');

		if (this.receiver != null)
		{
			this.receiver.writeExpression(writer, null);

			this.receiver.getType().appendExtendedName(desc);
		}

		final IType methodType = this.getterMethod.getType();
		final String getterName = this.getterMethod.getName().qualified;

		desc.append(')').append('L').append(ReferenceType.LazyFields.getInternalRef(methodType, "")).append(';');

		final Handle getterHandle = this.getterMethod.toHandle();
		final Handle setterHandle = this.setterMethod.toHandle();
		writer.visitInvokeDynamicInsn(getterName, desc.toString(), BOOTSTRAP, getterHandle, setterHandle);
	}
}
