package dyvil.tools.compiler.ast.access;

import java.util.List;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.constant.INumericValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.transform.Symbols;
import dyvil.tools.compiler.util.Util;

public class AssignMethodCall extends ASTNode implements IValue, IValued, ITypeContext, INamed
{
	public String		name;
	public String		qualifiedName;
	
	public IValue		instance;
	public IArguments	arguments	= Util.EMPTY_VALUES;
	
	public boolean		dotless;
	
	public IMethod		method;
	public IMethod		updateMethod;
	
	public AssignMethodCall(ICodePosition position)
	{
		this.position = position;
	}
	
	public AssignMethodCall(ICodePosition position, IValue instance, String name)
	{
		this.position = position;
		this.instance = instance;
		this.name = name;
		this.qualifiedName = Symbols.qualify(name);
	}
	
	@Override
	public int getValueType()
	{
		return METHOD_CALL;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return this.method.isIntrinsic() || this.getType().isPrimitive();
	}
	
	@Override
	public IType getType()
	{
		return this.method == null ? Type.NONE : this.method.getType();
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (type == Type.NONE || type == Type.VOID)
		{
			return true;
		}
		return this.method == null ? false : Type.isSuperType(type, this.method.getType());
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
	public void setName(String name, String qualifiedName)
	{
		this.name = name;
		this.qualifiedName = qualifiedName;
	}
	
	@Override
	public void setName(String name)
	{
		this.name = name;
	}
	
	@Override
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public void setQualifiedName(String name)
	{
		this.qualifiedName = name;
	}
	
	@Override
	public String getQualifiedName()
	{
		return this.qualifiedName;
	}
	
	@Override
	public boolean isName(String name)
	{
		return this.qualifiedName.equals(name);
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
	public IType resolveType(String name)
	{
		return this.method.resolveType(name, this.instance, this.arguments, null);
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.resolveTypes(markers, context);
		}
		
		for (IValue v : this.arguments)
		{
			v.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		this.arguments.resolve(markers, context);
		
		IMethod method = IAccess.resolveMethod(context, this.instance, this.qualifiedName, this.arguments);
		if (method != null)
		{
			this.method = method;
			return this;
		}
		
		Marker marker = Markers.create(this.position, "resolve.method", this.name);
		marker.addInfo("Qualified Name: " + this.qualifiedName);
		marker.addInfo("Instance Type: " + this.instance.getType());
		StringBuilder builder = new StringBuilder("Argument Types: [");
		// FIXME Util.typesToString("", this.arguments, ", ", builder);
		marker.addInfo(builder.append(']').toString());
		markers.add(marker);
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.check(markers, context);
			
			if (this.instance.getValueType() == APPLY_METHOD_CALL)
			{
				ApplyMethodCall call = (ApplyMethodCall) this.instance;
				IValue instance1 = call.instance;
				IArguments arguments1 = call.arguments.addLastValue(call);
				
				IType type = instance1.getType();
				MethodMatch match = type.resolveMethod(instance1, "update", arguments1);
				if (match != null)
				{
					this.updateMethod = match.theMethod;
				}
				else
				{
					Marker marker = Markers.create(this.position, "access.assign_call.update");
					marker.addInfo("Instance Type: " + type);
					markers.add(marker);
				}
			}
		}
		
		for (IValue v : this.arguments)
		{
			v.check(markers, context);
		}
		
		if (this.method != null)
		{
			this.method.checkArguments(markers, this.instance, this.arguments, this);
			
			IType type1 = this.instance.getType();
			IType type2 = this.method.getType();
			if (!Type.isSuperType(type1, type2))
			{
				Marker marker = Markers.create(this.position, "access.assign_call.type", this.name, this.instance.toString());
				marker.addInfo("Field Type: " + type1);
				marker.addInfo("Method Type: " + type2);
				markers.add(marker);
			}
			
			if (this.method.hasModifier(Modifiers.DEPRECATED))
			{
				markers.add(Markers.create(this.position, "access.method.deprecated", this.name));
			}
			
			byte access = context.getAccessibility(this.method);
			if (access == IContext.STATIC)
			{
				markers.add(Markers.create(this.position, "access.method.instance", this.name));
			}
			else if (access == IContext.SEALED)
			{
				markers.add(Markers.create(this.position, "access.method.sealed", this.name));
			}
			else if ((access & IContext.READ_ACCESS) == 0)
			{
				markers.add(Markers.create(this.position, "access.method.invisible", this.name));
			}
		}
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
				writer.visitInsn(Opcodes.DUP);
			}
			
			f.writeGet(writer, null);
			this.method.writeCall(writer, null, this.arguments);
			writer.visitInsn(Opcodes.DUP);
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
			
			writer.visitInsn(Opcodes.DUP2);
			
			call.method.writeCall(writer, null, Util.EMPTY_VALUES);
			this.method.writeCall(writer, null, this.arguments);
			writer.visitInsn(Opcodes.DUP_X2);
			this.updateMethod.writeCall(writer, null, Util.EMPTY_VALUES);
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
				writer.visitInsn(Opcodes.DUP);
			}
			
			f.writeGet(writer, null);
			this.method.writeCall(writer, null, this.arguments);
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
			
			writer.visitInsn(Opcodes.DUP2);
			
			call.method.writeCall(writer, null, Util.EMPTY_VALUES);
			this.method.writeCall(writer, null, this.arguments);
			this.updateMethod.writeCall(writer, null, Util.EMPTY_VALUES);
		}
	}
	
	private boolean writeIINC(MethodWriter writer, IField f)
	{
		if (this.arguments.size() == 1 && f.getType() == Type.INT && f instanceof IVariable)
		{
			boolean minus = false;
			if ("$plus".equals(this.qualifiedName) || (minus = "$minus".equals(this.qualifiedName)))
			{
				IValue value1 = this.arguments.getFirstValue();
				if (IValue.isNumeric(value1.getValueType()))
				{
					int count = ((INumericValue) value1).intValue();
					writer.visitIincInsn(((IVariable) f).getIndex(), minus ? -count : count);
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
			if (this.dotless && !Formatting.Method.useJavaFormat)
			{
				buffer.append(Formatting.Method.dotlessSeperator);
			}
			else
			{
				buffer.append('.');
			}
		}
		
		if (Formatting.Method.convertQualifiedNames)
		{
			buffer.append(this.qualifiedName).append("$eq");
		}
		else
		{
			buffer.append(this.name).append('=');
		}
		
		this.arguments.toString(prefix, buffer);
	}
}
