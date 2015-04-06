package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.EnumValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.expression.ThisValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.operator.ClassOperator;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class FieldAccess extends ASTNode implements ICall, INamed, IValued
{
	public IValue	instance;
	public Name		name;
	
	public boolean	dotless;
	
	public IField	field;
	
	public FieldAccess(ICodePosition position)
	{
		this.position = position;
	}
	
	public FieldAccess(ICodePosition position, IValue instance, Name name)
	{
		this.position = position;
		this.instance = instance;
		this.name = name;
	}
	
	@Override
	public int getValueType()
	{
		return FIELD_ACCESS;
	}
	
	@Override
	public IType getType()
	{
		return this.field == null ? Types.UNKNOWN : this.field.getType();
	}
	
	@Override
	public boolean isType(IType type)
	{
		return this.field == null ? false : type.isSuperTypeOf(this.field.getType());
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (this.field == null)
		{
			return 0;
		}
		
		IType type1 = this.field.getType();
		if (type.equals(type1))
		{
			return 3;
		}
		if (type.isSuperTypeOf(type1))
		{
			return 2;
		}
		return 0;
	}
	
	@Override
	public void setName(Name name)
	{
		this.name = name;
	}
	
	@Override
	public Name getName()
	{
		return this.name;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.instance = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.instance;
	}
	
	@Override
	public void setArguments(IArguments arguments)
	{
	}
	
	@Override
	public IArguments getArguments()
	{
		return EmptyArguments.INSTANCE;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance = this.instance.resolve(markers, context);
		}
		
		if (this.instance != null && this.instance.getValueType() == CLASS_ACCESS)
		{
			if (this.name == Name._class)
			{
				ClassOperator co = new ClassOperator(((ClassAccess) this.instance).type);
				co.position = this.position;
				co.dotless = this.dotless;
				return co;
			}
		}
		
		IField field = ICall.resolveField(context, this.instance, this.name);
		if (field != null)
		{
			if (field.isEnumConstant())
			{
				EnumValue enumValue = new EnumValue(this.position);
				enumValue.name = this.name;
				enumValue.type = field.getType();
				return enumValue;
			}
			
			this.field = field;
			return this;
		}
		
		IMethod method = ICall.resolveMethod(null, context, this.instance, this.name, EmptyArguments.INSTANCE);
		if (method != null)
		{
			return this.toMethodCall(method);
		}
		
		if (this.instance == null)
		{
			IClass iclass = context.resolveClass(this.name);
			if (iclass != null)
			{
				return new ClassAccess(this.position, new Type(iclass));
			}
		}
		
		Marker marker = markers.create(this.position, "resolve.method_field", this.name.unqualified);
		marker.addInfo("Qualified Name: " + this.name.qualified);
		if (this.instance != null)
		{
			marker.addInfo("Instance Type: " + this.instance.getType());
		}
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.checkTypes(markers, context);
			
			if (this.field != null && this.field.hasModifier(Modifiers.STATIC) && this.instance.getValueType() != CLASS_ACCESS)
			{
				markers.add(this.position, "access.field.static", this.name);
				this.instance = null;
				return;
			}
		}
		else if (this.field != null && this.field.isField() && !this.field.hasModifier(Modifiers.STATIC))
		{
			markers.add(this.position, "access.field.unqualified", this.name);
			this.instance = new ThisValue(this.position, this.field.getTheClass().getType());
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.check(markers, context);
		}
		
		if (this.field != null)
		{
			if (this.field.hasModifier(Modifiers.DEPRECATED))
			{
				markers.add(this.position, "access.field.deprecated", this.name);
			}
			
			byte access = context.getAccessibility(this.field);
			if (access == IContext.STATIC)
			{
				markers.add(this.position, "access.field.instance", this.name);
			}
			else if (access == IContext.SEALED)
			{
				markers.add(this.position, "access.field.sealed", this.name);
			}
			else if ((access & IContext.READ_ACCESS) == 0)
			{
				markers.add(this.position, "access.field.invisible", this.name);
			}
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.field != null && this.field.hasModifier(Modifiers.CONST))
		{
			IValue v = this.field.getValue();
			return v != null && v.isConstant() ? v : this;
		}
		if (this.instance != null)
		{
			this.instance = this.instance.foldConstants();
		}
		return this;
	}
	
	public MethodCall toMethodCall(IMethod method)
	{
		MethodCall call = new MethodCall(this.position);
		call.instance = this.instance;
		call.name = this.name;
		call.method = method;
		call.dotless = this.dotless;
		call.arguments = EmptyArguments.INSTANCE;
		return call;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		this.field.writeGet(writer, this.instance);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.field.writeGet(writer, this.instance);
		writer.writeInsn(this.field.getType().getReturnOpcode());
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.instance != null)
		{
			this.instance.toString("", buffer);
			if (this.dotless && !Formatting.Field.useJavaFormat)
			{
				buffer.append(Formatting.Field.dotlessSeperator);
			}
			else
			{
				buffer.append('.');
			}
		}
		
		buffer.append(this.name);
	}
}
