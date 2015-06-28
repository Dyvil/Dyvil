package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class ClassAccess extends ASTNode implements IValue
{
	private static final byte	OBJECT_ACCESS	= 1;
	private static final byte	APPLY_CALL		= 2;
	
	public IType				type;
	
	public ClassAccess(IType type)
	{
		this.type = type;
	}
	
	public ClassAccess(ICodePosition position, IType type)
	{
		this.position = position;
		this.type = type;
	}
	
	@Override
	public boolean isConstant()
	{
		return true;
	}
	
	@Override
	public int valueTag()
	{
		return CLASS_ACCESS;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return false;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public IValue withType(IType type)
	{
		if (type.isSuperTypeOf(this.type))
		{
			return this;
		}
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type.isSuperTypeOf(this.type);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (this.type.equals(type))
		{
			return 3;
		}
		if (type.isSuperTypeOf(this.type))
		{
			return 2;
		}
		return 0;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.type = this.type.resolve(null, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		Name name = this.type.getName();
		IDataMember f = context.resolveField(name);
		if (f != null)
		{
			FieldAccess access = new FieldAccess(this.position);
			access.name = name;
			access.field = f;
			return access;
		}
		
		IMethod m = IContext.resolveMethod(context, null, name, EmptyArguments.INSTANCE);
		if (m != null)
		{
			MethodCall call = new MethodCall(this.position);
			call.name = name;
			call.method = m;
			call.dotless = true;
			call.arguments = EmptyArguments.INSTANCE;
			return call;
		}
		
		if (!this.type.isResolved())
		{
			markers.add(this.position, this.type.isArrayType() ? "resolve.type" : "resolve.any", this.type.toString());
		}
		
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		IClass iclass = this.type.getTheClass();
		if (iclass != null)
		{
			if (iclass.hasModifier(Modifiers.DEPRECATED))
			{
				markers.add(this.position, "type.access.deprecated", iclass.getName());
			}
			
			if (context.getVisibility(iclass) == IContext.SEALED)
			{
				markers.add(this.position, "type.access.sealed", iclass.getName());
			}
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		IClass iclass = this.type.getTheClass();
		if (iclass == null)
		{
			// Already reported this in RESOLVE ^
			return;
		}
		
		if (iclass.hasModifier(Modifiers.OBJECT_CLASS))
		{
			// Object type, we can safely use it's instance field.
			return;
		}
		
		markers.add(this.position, "type.access.invalid", this.type.toString());
	}
	
	@Override
	public IValue foldConstants()
	{
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		IClass iclass = this.type.getTheClass();
		if (iclass != null)
		{
			IDataMember field = iclass.getMetadata().getInstanceField();
			if (field != null)
			{
				field.writeGet(writer, null);
			}
		}
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		System.out.println();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString("", buffer);
	}
}
