package dyvil.tools.compiler.ast.access;

import org.objectweb.asm.Label;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public class ApplyMethodCall extends ASTNode implements IValue, IValued, ITypeContext
{
	public IValue		instance;
	public IArguments	arguments;
	
	public IMethod		method;
	private IType		type;
	
	public ApplyMethodCall(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int getValueType()
	{
		return APPLY_METHOD_CALL;
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
			return Type.NONE;
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
		return type == Type.VOID ? this : IValue.super.withType(type);
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (type == Type.VOID)
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
	public void setValue(IValue value)
	{
		this.instance = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.instance;
	}
	
	public void setArguments(IArguments arguments)
	{
		this.arguments = arguments;
	}
	
	public IArguments getArguments()
	{
		return this.arguments;
	}
	
	@Override
	public IType resolveType(String name)
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
			this.instance = this.instance.resolve(markers, context);
		}
		
		this.arguments.resolve(markers, context);
		
		IMethod method = IAccess.resolveMethod(context, this.instance, "apply", this.arguments);
		if (method != null)
		{
			this.method = method;
			return this;
		}
		
		Marker marker = markers.create(this.position, "resolve.method", "apply");
		marker.addInfo("Instance Type: " + this.instance.getType());
		StringBuilder builder = new StringBuilder("Argument Types: {");
		Util.typesToString("", this.arguments, ", ", builder);
		marker.addInfo(builder.append('}').toString());
		
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.checkTypes(markers, context);
		}
		
		if (this.method != null)
		{
			this.method.checkArguments(markers, this.instance, this.arguments, this);
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
			if (this.method.hasModifier(Modifiers.DEPRECATED))
			{
				markers.add(this.position, "access.method.deprecated", "apply");
			}
			
			byte access = context.getAccessibility(this.method);
			if (access == IContext.STATIC)
			{
				markers.add(this.position, "access.method.instance", "apply");
			}
			else if (access == IContext.SEALED)
			{
				markers.add(this.position, "access.method.sealed", "apply");
			}
			else if ((access & IContext.READ_ACCESS) == 0)
			{
				markers.add(this.position, "access.method.invisible", "apply");
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
		this.method.writeCall(writer, this.instance, this.arguments, this.type);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.method.writeCall(writer, this.instance, this.arguments, Type.VOID);
	}
	
	@Override
	public void writeJump(MethodWriter writer, Label dest)
	{
		this.method.writeJump(writer, dest, this.instance, this.arguments);
	}
	
	@Override
	public void writeInvJump(MethodWriter writer, Label dest)
	{
		this.method.writeInvJump(writer, dest, this.instance, this.arguments);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.instance != null)
		{
			this.instance.toString(prefix, buffer);
		}
		
		this.arguments.toString(prefix, buffer);
	}
}
