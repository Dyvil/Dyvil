package dyvil.tools.compiler.ast.operator;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.AbstractValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LiteralConversion;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.type.ClassGenericType;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.ClassType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.PrimitiveType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class ClassOperator extends AbstractValue
{
	public static final class Types
	{
		public static final IClass    CLASS_CLASS = Package.javaLang.resolveClass("Class");
		public static final ClassType CLASS       = new ClassType(CLASS_CLASS);

		public static final IClass CLASS_CONVERTIBLE = Package.dyvilLangLiteral.resolveClass("ClassConvertible");
		
		private Types()
		{
			// no instances
		}
	}
	
	protected IType type;
	
	// Metadata
	private IType genericType;
	
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
	public boolean isConstant()
	{
		return true;
	}
	
	@Override
	public Object toObject()
	{
		return dyvil.tools.asm.Type.getType(this.type.getExtendedName());
	}
	
	@Override
	public boolean isResolved()
	{
		return true;
	}
	
	@Override
	public IType getType()
	{
		if (this.genericType == null)
		{
			ClassGenericType generic = new ClassGenericType(Types.CLASS_CLASS);
			generic.addType(this.type);
			return this.genericType = generic;
		}
		return this.genericType;
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		IAnnotation annotation = type.getTheClass().getAnnotation(Types.CLASS_CONVERTIBLE);
		if (annotation != null)
		{
			return new LiteralConversion(this, annotation).withType(type, typeContext, markers, context);
		}
		
		return type.isSuperTypeOf(this.getType()) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (type.getTheClass().getAnnotation(Types.CLASS_CONVERTIBLE) != null)
		{
			return true;
		}
		
		return type.isSuperTypeOf(this.getType());
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		if (type.getTheClass().getAnnotation(Types.CLASS_CONVERTIBLE) != null)
		{
			return 2;
		}
		
		return type.getSubTypeDistance(this.genericType);
	}
	
	@Override
	public IValue toConstant(MarkerList markers)
	{
		return this;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.type == null)
		{
			this.type = dyvil.tools.compiler.ast.type.Types.UNKNOWN;
			markers.add(I18n.createMarker(this.position, "classoperator.invalid"));
			return;
		}
		
		this.type = this.type.resolveType(markers, context);
		ClassGenericType generic = new ClassGenericType(Types.CLASS_CLASS);
		generic.addType(this.type);
		this.genericType = generic;
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.type.resolve(markers, context);
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.type.checkType(markers, context, TypePosition.CLASS);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.type.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		this.type.foldConstants();
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.type.cleanup(context, compilableList);
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (this.type.isPrimitive())
		{
			String owner;
			
			// Cannot use PrimitiveType.getInternalName as it returns the Dyvil
			// class instead of the Java one.
			switch (this.type.getTypecode())
			{
			case PrimitiveType.BOOLEAN_CODE:
				owner = "java/lang/Boolean";
				break;
			case PrimitiveType.BYTE_CODE:
				owner = "java/lang/Byte";
				break;
			case PrimitiveType.SHORT_CODE:
				owner = "java/lang/Short";
				break;
			case PrimitiveType.CHAR_CODE:
				owner = "java/lang/Character";
				break;
			case PrimitiveType.INT_CODE:
				owner = "java/lang/Integer";
				break;
			case PrimitiveType.LONG_CODE:
				owner = "java/lang/Long";
				break;
			case PrimitiveType.FLOAT_CODE:
				owner = "java/lang/Float";
				break;
			case PrimitiveType.DOUBLE_CODE:
				owner = "java/lang/Double";
				break;
			default:
				owner = "java/lang/Void";
				break;
			}
			
			writer.writeFieldInsn(Opcodes.GETSTATIC, owner, "TYPE", "Ljava/lang/Class;");
			return;
		}
		
		dyvil.tools.asm.Type t = dyvil.tools.asm.Type.getType(this.type.getExtendedName());
		writer.writeLDC(t);

		if (type == dyvil.tools.compiler.ast.type.Types.VOID)
		{
			writer.writeInsn(Opcodes.ARETURN);
		}
		else if (type != null)
		{
			this.genericType.writeCast(writer, type, this.getLineNumber());
		}
	}
	
	@Override
	public String toString()
	{
		return "class(" + this.type + ")";
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("class(");
		this.type.toString(prefix, buffer);
		buffer.append(')');
	}
}
