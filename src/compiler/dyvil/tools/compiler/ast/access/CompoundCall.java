package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.constant.INumericValue;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public final class CompoundCall extends ASTNode implements IValue, IValued, ITypeContext, INamed
{
	public Name			name;
	
	public IValue		instance;
	public IArguments	arguments	= EmptyArguments.INSTANCE;
	
	public IMethod		method;
	public IMethod		updateMethod;
	private IType		type;
	
	public CompoundCall(ICodePosition position)
	{
		this.position = position;
	}
	
	public CompoundCall(ICodePosition position, IValue instance, Name name)
	{
		this.position = position;
		this.instance = instance;
		this.name = name;
	}
	
	@Override
	public int getValueType()
	{
		return METHOD_CALL;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return this.method != null && (this.method.isIntrinsic() || this.getType().isPrimitive());
	}
	
	@Override
	public IType getType()
	{
		if (this.method == null)
		{
			return Types.UNKNOWN;
		}
		if (this.type == null)
		{
			if (this.method.hasTypeVariables())
			{
				return this.type = this.method.getType(this);
			}
			return this.type = this.method.getType();
		}
		return this.type;
	}
	
	@Override
	public IValue withType(IType type)
	{
		return type == Types.VOID ? this : IValue.super.withType(type);
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (type == Types.VOID)
		{
			return true;
		}
		if (this.method == null)
		{
			return false;
		}
		return type.isSuperTypeOf(this.getType());
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (this.method == null)
		{
			return 0;
		}
		
		IType type1 = this.method.getType();
		if (type.equals(type1))
		{
			return 3;
		}
		else if (type.isSuperTypeOf(type1))
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
		this.arguments = this.arguments.addLastValue(Name.update, value);
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
	
	@Override
	public IType resolveType(Name name)
	{
		return this.method.resolveType(name, this.instance, this.arguments, null);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.resolveTypes(markers, context);
		}
		
		this.arguments.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.resolve(markers, context);
		}
		
		this.arguments.resolve(markers, context);
		
		IMethod method = IAccess.resolveMethod(markers, context, this.instance, this.name, this.arguments);
		if (method != null)
		{
			this.method = method;
			return this;
		}
		
		Marker marker = markers.create(this.position, "resolve.method", this.name.unqualified);
		marker.addInfo("Qualified Name: " + this.name.unqualified);
		marker.addInfo("Instance Type: " + this.instance.getType());
		StringBuilder builder = new StringBuilder("Argument Types: ");
		Util.typesToString("", this.arguments, ", ", builder);
		marker.addInfo(builder.toString());
		
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.checkTypes(markers, context);
			
			if (this.instance.getValueType() == APPLY_METHOD_CALL)
			{
				ApplyMethodCall call = (ApplyMethodCall) this.instance;
				IValue instance1 = call.instance;
				IArguments arguments1 = call.arguments.addLastValue(call);
				
				IType type = instance1.getType();
				IMethod match = IContext.resolveMethod(markers, type, instance1, Name.update, arguments1);
				if (match != null)
				{
					this.updateMethod = match;
				}
				else
				{
					Marker marker = markers.create(this.position, "access.assign_call.update");
					marker.addInfo("Instance Type: " + type);
				}
			}
		}
		
		if (this.method != null)
		{
			this.instance = this.method.checkArguments(markers, this.instance, this.arguments, this);
		}
		this.arguments.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.check(markers, context);
		}
		
		if (this.method != null)
		{
			IType type1 = this.instance.getType();
			IType type2 = this.getType();
			if (!type1.isSuperTypeOf(type2))
			{
				Marker marker = markers.create(this.position, "access.assign_call.type", this.name, this.instance.toString());
				marker.addInfo("Field Type: " + type1);
				marker.addInfo("Method Type: " + type2);
			}
			
			if (this.method.hasModifier(Modifiers.DEPRECATED))
			{
				markers.add(this.position, "access.method.deprecated", this.name);
			}
			
			byte access = context.getAccessibility(this.method);
			if (access == IContext.STATIC)
			{
				markers.add(this.position, "access.method.instance", this.name);
			}
			else if (access == IContext.SEALED)
			{
				markers.add(this.position, "access.method.sealed", this.name);
			}
			else if ((access & IContext.READ_ACCESS) == 0)
			{
				markers.add(this.position, "access.method.invisible", this.name);
			}
		}
		
		this.arguments.check(markers, context);
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.instance != null)
		{
			this.instance = this.instance.foldConstants();
		}
		this.arguments.foldConstants();
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		int i = this.instance.getValueType();
		if (i == FIELD_ACCESS)
		{
			FieldAccess access = (FieldAccess) this.instance;
			IField f = access.field;
			
			if (this.writeIINC(writer, f))
			{
				f.writeGet(writer, null);
				return;
			}
			
			IValue instance = access.instance;
			if (instance != null)
			{
				instance.writeExpression(writer);
				writer.writeInsn(Opcodes.DUP);
			}
			
			f.writeGet(writer, null);
			this.method.writeCall(writer, null, this.arguments, null);
			writer.writeInsn(Opcodes.DUP);
			f.writeSet(writer, null, null);
		}
		else if (i == APPLY_METHOD_CALL)
		{
			ApplyMethodCall call = (ApplyMethodCall) this.instance;
			
			call.instance.writeExpression(writer);
			
			for (IValue v : call.arguments)
			{
				v.writeExpression(writer);
			}
			
			writer.writeInsn(Opcodes.DUP2);
			
			call.method.writeCall(writer, null, EmptyArguments.INSTANCE, null);
			this.method.writeCall(writer, null, this.arguments, null);
			writer.writeInsn(Opcodes.DUP_X2);
			this.updateMethod.writeCall(writer, null, EmptyArguments.INSTANCE, null);
		}
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		int i = this.instance.getValueType();
		if (i == FIELD_ACCESS)
		{
			FieldAccess access = (FieldAccess) this.instance;
			IField f = access.field;
			
			if (this.writeIINC(writer, f))
			{
				return;
			}
			
			IValue instance = access.instance;
			if (instance != null)
			{
				instance.writeExpression(writer);
				writer.writeInsn(Opcodes.DUP);
			}
			
			f.writeGet(writer, null);
			this.method.writeCall(writer, null, this.arguments, null);
			f.writeSet(writer, null, null);
		}
		else if (i == APPLY_METHOD_CALL)
		{
			ApplyMethodCall call = (ApplyMethodCall) this.instance;
			
			call.instance.writeExpression(writer);
			
			for (IValue v : call.arguments)
			{
				v.writeExpression(writer);
			}
			
			writer.writeInsn(Opcodes.DUP2);
			
			call.method.writeCall(writer, null, EmptyArguments.INSTANCE, null);
			this.method.writeCall(writer, null, this.arguments, null);
			this.updateMethod.writeCall(writer, null, EmptyArguments.INSTANCE, null);
		}
	}
	
	private boolean writeIINC(MethodWriter writer, IField f)
	{
		if (this.arguments.size() == 1 && f.getType() == Types.INT && f.isVariable())
		{
			boolean minus = false;
			if (this.name == Name.plus || (minus = this.name == Name.minus))
			{
				IValue value1 = this.arguments.getFirstValue();
				if (IValue.isNumeric(value1.getValueType()))
				{
					int count = ((INumericValue) value1).intValue();
					writer.writeIINC(((IVariable) f).getIndex(), minus ? -count : count);
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.instance != null)
		{
			this.instance.toString(prefix, buffer);
			buffer.append(' ');
		}
		
		if (Formatting.Method.convertQualifiedNames)
		{
			buffer.append(this.name.qualified).append("$eq");
		}
		else
		{
			buffer.append(this.name.unqualified).append('=');
		}
		
		this.arguments.toString(prefix, buffer);
	}
}
