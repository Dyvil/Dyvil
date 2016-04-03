package dyvil.tools.compiler.ast.expression;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class NilExpr implements IValue
{
	public static final class LazyFields
	{
		public static final IClass NIL_CONVERTIBLE_CLASS = Package.dyvilLangLiteral.resolveClass("NilConvertible");

		private LazyFields()
		{
			// no instances
		}
	}

	protected ICodePosition position;

	// Metadata
	private IType requiredType;

	private Name    methodName;
	private IMethod method;

	public NilExpr()
	{
	}

	public NilExpr(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public int valueTag()
	{
		return NIL;
	}

	@Override
	public boolean isPrimitive()
	{
		return false;
	}

	@Override
	public boolean isResolved()
	{
		return this.requiredType != null && this.requiredType.isResolved();
	}

	@Override
	public IType getType()
	{
		return this.requiredType == null ? dyvil.tools.compiler.ast.type.builtin.Types.UNKNOWN : this.requiredType;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type.isArrayType())
		{
			this.requiredType = type;
			this.methodName = Names.apply;
			return this;
		}

		final IAnnotation annotation = type.getTheClass().getAnnotation(LazyFields.NIL_CONVERTIBLE_CLASS);
		if (annotation != null)
		{
			this.methodName = LiteralConversion.getMethodName(annotation);
			this.requiredType = type;
			return this;
		}

		if (type != Types.UNKNOWN)
		{
			this.requiredType = Types.UNKNOWN;
			markers.add(Markers.semantic(this.position, "nil.type", type));
		}
		return this;
	}

	@Override
	public boolean isType(IType type)
	{
		return type.isArrayType() || type.getTheClass().getAnnotation(LazyFields.NIL_CONVERTIBLE_CLASS) != null;
	}

	@Override
	public int getTypeMatch(IType type)
	{
		return this.isType(type) ? 1 : 0;
	}

	@Override
	public Object toObject()
	{
		return null;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.requiredType == null)
		{
			markers.add(Markers.semantic(this.position, "nil.untyped"));
			return;
		}

		if (this.requiredType == Types.UNKNOWN || this.requiredType.isArrayType())
		{
			return;
		}

		IMethod match = IContext.resolveMethod(this.requiredType, null, this.methodName, EmptyArguments.INSTANCE);
		if (match == null)
		{
			markers.add(Markers.semantic(this.position, "nil.method", this.requiredType.toString(), this.methodName));
		}
		else
		{
			this.method = match;
			GenericData data = match.getGenericData(null, null, EmptyArguments.INSTANCE);
			this.requiredType = match.getType().getConcreteType(data);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
	}

	@Override
	public IValue foldConstants()
	{
		return this;
	}

	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		// Write an array type
		if (this.requiredType.isArrayType())
		{
			IType elementType = this.requiredType.getElementType();
			if (elementType.isPrimitive() || elementType.getTheClass() == Types.OBJECT_CLASS)
			{
				// Write a Field Access to the EMPTY fields in the Primitive
				// Array Classes
				writer.visitFieldInsn(Opcodes.GETSTATIC, this.requiredType.getTheClass().getInternalName(), "EMPTY",
				                      this.requiredType.getExtendedName());
				return;
			}

			writer.visitLdcInsn(0);
			writer.visitMultiANewArrayInsn(this.requiredType, 1);
		}
		else
		{
			this.method.writeCall(writer, null, EmptyArguments.INSTANCE, this.requiredType, this.requiredType,
			                      this.getLineNumber());
		}

		if (type != null)
		{
			this.requiredType.writeCast(writer, type, this.getLineNumber());
		}
	}

	@Override
	public String toString()
	{
		return "nil";
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("nil");
	}
}
