package dyvil.tools.compiler.ast.reference;

import dyvil.tools.asm.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class ArrayReference implements IReference
{
	private IValue array;
	private IValue index;

	public ArrayReference(IValue array, IValue index)
	{
		this.array = array;
		this.index = index;
	}

	@Override
	public void check(ICodePosition position, MarkerList markers)
	{
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
	}

	@Override
	public void writeReference(MethodWriter writer) throws BytecodeException
	{
		this.array.writeExpression(writer, null);
		this.index.writeExpression(writer, Types.INT);

		final IType elementType = this.array.getType().getElementType();

		final String refKeyword = Types.getTypeRefKeyword(elementType);
		final String factoryMethodName = "new" + refKeyword + "ArrayRef";
		final String factoryMethodType = getFactoryMethodType(elementType, refKeyword);

		writer.writeInvokeInsn(Opcodes.INVOKESTATIC, "dyvil/ref/ReferenceFactory", factoryMethodName,
		                       factoryMethodType, false);
	}

	private static String getFactoryMethodType(IType elementType, String refKeyword)
	{
		final StringBuilder factoryMethodType = new StringBuilder().append('(');
		if (elementType.isPrimitive())
		{
			factoryMethodType.append('[').append(elementType.getExtendedName());
		}
		else
		{
			factoryMethodType.append("[Ljava/lang/Object;");
		}

		factoryMethodType.append("I)Ldyvil/ref/").append(refKeyword).append("Ref;");
		return factoryMethodType.toString();
	}
}
