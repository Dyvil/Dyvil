package dyvil.tools.compiler.ast.operator;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.LiteralExpression;
import dyvil.tools.compiler.ast.expression.Value;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.type.ClassGenericType;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.ClassType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class TypeOperator extends Value
{
	public static final class Types
	{
		public static final IClass		TYPE_CLASS	= Package.dyvilLang.resolveClass("Type");
		public static final ClassType	TYPE		= new ClassType();
		
		public static final IClass TYPE_CONVERTIBLE = Package.dyvilLangLiteral.resolveClass("TypeConvertible");
		
		private Types()
		{
			// no instances
		}
	}
	
	protected IType type;
	
	// Metadata
	private IType genericType;
	
	public TypeOperator(ICodePosition position)
	{
		this.position = position;
	}
	
	public TypeOperator(IType type)
	{
		this.setType(type);
	}
	
	@Override
	public int valueTag()
	{
		return TYPE_OPERATOR;
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
			ClassGenericType generic = new ClassGenericType(Types.TYPE_CLASS);
			generic.addType(this.type);
			return this.genericType = generic;
		}
		return this.genericType;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		IAnnotation annotation = type.getTheClass().getAnnotation(Types.TYPE_CONVERTIBLE);
		if (annotation != null)
		{
			return new LiteralExpression(this, annotation).withType(type, typeContext, markers, context);
		}
		
		return type.isSuperTypeOf(this.getType()) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (type.getTheClass().getAnnotation(Types.TYPE_CONVERTIBLE) != null)
		{
			return true;
		}
		
		return type.isSuperTypeOf(this.getType());
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		if (type.getTheClass().getAnnotation(Types.TYPE_CONVERTIBLE) != null)
		{
			return CONVERSION_MATCH;
		}
		
		return type.getSubTypeDistance(this.getType());
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.type == null)
		{
			this.type = dyvil.tools.compiler.ast.type.Types.UNKNOWN;
			markers.add(I18n.createMarker(this.position, "typeoperator.invalid"));
			return;
		}
		
		this.type = this.type.resolveType(markers, context);
		ClassGenericType generic = new ClassGenericType(Types.TYPE_CLASS);
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
		this.type.checkType(markers, context, TypePosition.TYPE);
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
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.type.writeTypeExpression(writer);
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
		return "type(" + this.type + ")";
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("type(");
		this.type.toString(prefix, buffer);
		buffer.append(')');
	}
}
