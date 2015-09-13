package dyvil.tools.compiler.ast.constant;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LiteralExpression;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class NilValue implements IValue
{
	public static final class Types
	{
		public static final IClass NIL_CONVERTIBLE_CLASS = Package.dyvilLangLiteral.resolveClass("NilConvertible");
		
		private Types()
		{
			// no instances
		}
	}
	
	protected ICodePosition position;
	
	// Metadata
	private IType	requiredType;
	private Name	methodName;
	private IMethod	method;
	
	public NilValue()
	{
	}
	
	public NilValue(ICodePosition position)
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
	public IType getType()
	{
		return this.requiredType == null ? dyvil.tools.compiler.ast.type.Types.UNKNOWN : this.requiredType;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type == dyvil.tools.compiler.ast.type.Types.UNKNOWN)
		{
			return this;
		}
		
		if (type.isArrayType())
		{
			return this;
		}
		
		IAnnotation annotation = type.getTheClass().getAnnotation(Types.NIL_CONVERTIBLE_CLASS);
		if (annotation != null)
		{
			this.methodName = LiteralExpression.getMethodName(annotation);
			this.requiredType = type;
			return this;
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type.isArrayType() || type.getTheClass().getAnnotation(Types.NIL_CONVERTIBLE_CLASS) != null;
	}
	
	@Override
	public float getTypeMatch(IType type)
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
			markers.add(this.position, "nil.type");
			return;
		}
		
		if (this.requiredType.isArrayType())
		{
			return;
		}
		
		IMethod match = IContext.resolveMethod(this.requiredType, null, this.methodName, EmptyArguments.INSTANCE);
		if (match == null)
		{
			markers.add(this.position, "nil.method", this.requiredType.toString(), this.methodName);
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
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		// Write an array type
		if (this.requiredType.isArrayType())
		{
			IType elementType = this.requiredType.getElementType();
			if (elementType.isPrimitive())
			{
				// Write a Field Access to the EMPTY fields in the Primitive
				// Array Classes
				writer.writeFieldInsn(Opcodes.GETSTATIC, this.requiredType.getTheClass().getInternalName(), "EMPTY", this.requiredType.getExtendedName());
				return;
			}
			
			writer.writeLDC(0);
			int dims = 1;
			while (elementType.isArrayType())
			{
				elementType = elementType.getElementType();
				dims++;
				writer.writeLDC(0);
			}
			
			writer.writeNewArray(elementType, dims);
			return;
		}
		
		this.method.writeCall(writer, null, EmptyArguments.INSTANCE, this.requiredType, this.getLineNumber());
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.writeExpression(writer);
		writer.writeInsn(Opcodes.ARETURN);
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
