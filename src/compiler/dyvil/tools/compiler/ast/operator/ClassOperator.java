package dyvil.tools.compiler.ast.operator;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LiteralExpression;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.GenericType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassFormat;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class ClassOperator extends ASTNode implements IValue
{
	public static final IClass	CLASS_CONVERTIBLE	= Package.dyvilLangLiteral.resolveClass("ClassConvertible");
	
	private IType				type;
	private IType				genericType;
	public boolean				dotless;
	
	public ClassOperator(ICodePosition position)
	{
		this.position = position;
	}
	
	public ClassOperator(IType type)
	{
		this.setType(type);
	}
	
	@Override
	public int valueTag()
	{
		return CLASS_OPERATOR;
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IType getType()
	{
		if (this.genericType == null)
		{
			GenericType generic = new GenericType(Types.CLASS_CLASS);
			generic.addType(this.type);
			return this.genericType = generic;
		}
		return this.genericType;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (type.getTheClass().getAnnotation(CLASS_CONVERTIBLE) != null)
		{
			return new LiteralExpression(this).withType(type, typeContext, markers, context);
		}
		
		return type.isSuperTypeOf(this.getType()) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (type.getTheClass().getAnnotation(CLASS_CONVERTIBLE) != null)
		{
			return true;
		}
		
		return type.isSuperTypeOf(this.getType());
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (type.getTheClass().getAnnotation(CLASS_CONVERTIBLE) != null)
		{
			return 2;
		}
		
		IType thisType = this.getType();
		if (type.equals(thisType))
		{
			return 3;
		}
		if (type.isSuperTypeOf(thisType))
		{
			return 2;
		}
		return 0;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.type == null)
		{
			this.type = Types.UNKNOWN;
			markers.add(this.position, "classoperator.invalid");
			return;
		}
		
		this.type = this.type.resolve(markers, context);
		GenericType generic = new GenericType(Types.CLASS_CLASS);
		generic.addType(this.type);
		this.genericType = generic;
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
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
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		if (this.type.isPrimitive())
		{
			String owner;
			
			// Cannot use PrimitiveType.getInternalName as it returns the Dyvil
			// class instead of the Java one.
			switch (((PrimitiveType) this.type).typecode)
			{
			case ClassFormat.T_BOOLEAN:
				owner = "java/lang/Boolean";
				break;
			case ClassFormat.T_BYTE:
				owner = "java/lang/Byte";
				break;
			case ClassFormat.T_SHORT:
				owner = "java/lang/Short";
				break;
			case ClassFormat.T_CHAR:
				owner = "java/lang/Character";
				break;
			case ClassFormat.T_INT:
				owner = "java/lang/Integer";
				break;
			case ClassFormat.T_LONG:
				owner = "java/lang/Long";
				break;
			case ClassFormat.T_FLOAT:
				owner = "java/lang/Float";
				break;
			case ClassFormat.T_DOUBLE:
				owner = "java/lang/Double";
				break;
			default:
				owner = "java/lang/Void";
				break;
			}
			
			writer.writeFieldInsn(Opcodes.GETSTATIC, owner, "TYPE", "Ljava/lang/Class;");
			return;
		}
		
		org.objectweb.asm.Type t = org.objectweb.asm.Type.getType(this.type.getExtendedName());
		writer.writeLDC(t);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		this.writeExpression(writer);
		writer.writeInsn(Opcodes.ARETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("class[");
		this.type.toString(prefix, buffer);
		buffer.append(']');
	}
}
