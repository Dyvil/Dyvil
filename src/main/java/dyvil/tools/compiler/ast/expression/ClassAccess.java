package dyvil.tools.compiler.ast.expression;

import java.util.List;

import dyvil.lang.array.Arrays;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.bytecode.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.marker.SyntaxError;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Modifiers;

public class ClassAccess extends ASTNode implements IValue, IAccess
{
	public IType	type;
	
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
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public int getValueType()
	{
		return CLASS_ACCESS;
	}
	
	@Override
	public void setName(String name, String qualifiedName)
	{
	}
	
	@Override
	public void setName(String name)
	{
	}
	
	@Override
	public String getName()
	{
		return this.type.getName();
	}
	
	@Override
	public void setQualifiedName(String name)
	{
	}
	
	@Override
	public String getQualifiedName()
	{
		return this.type.getQualifiedName();
	}
	
	@Override
	public boolean isName(String name)
	{
		return this.type.isName(name);
	}
	
	@Override
	public void setValue(IValue value)
	{
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
	
	@Override
	public void setValues(List<IValue> list)
	{
	}
	
	@Override
	public void setValue(int index, IValue value)
	{
	}
	
	@Override
	public void addValue(IValue value)
	{
	}
	
	@Override
	public List<IValue> getValues()
	{
		return null;
	}
	
	@Override
	public IValue getValue(int index)
	{
		return null;
	}
	
	@Override
	public void setArray(boolean array)
	{
	}
	
	@Override
	public boolean isArray()
	{
		return false;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		this.type = this.type.resolve(context);
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		if (!this.type.isResolved())
		{
			IValue v = this.resolve2(context);
			if (v != null)
			{
				return v;
			}
			markers.add(this.getResolveError());
		}
		
		IClass iclass = this.type.getTheClass();
		if (iclass == null || !iclass.hasModifier(Modifiers.OBJECT_CLASS))
		{
			markers.add(new SemanticError(this.position, "The type '" + this.type + "' is not a singleton object type"));
		}
		
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		IClass iclass = this.type.getTheClass();
		if (iclass != null && context.getAccessibility(iclass) == IContext.SEALED)
		{
			markers.add(new SemanticError(this.position, "The sealed class '" + iclass.getName() + "' cannot be accessed because it is private to it's library"));
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		return this;
	}
	
	@Override
	public boolean resolve(IContext context)
	{
		return this.type.isResolved();
	}
	
	@Override
	public IAccess resolve2(IContext context)
	{
		String qualifiedName = this.type.getQualifiedName();
		FieldMatch f = context.resolveField(qualifiedName);
		if (f != null)
		{
			FieldAccess access = new FieldAccess(this.position);
			access.name = this.type.getName();
			access.qualifiedName = qualifiedName;
			access.field = f.theField;
			return access;
		}
		
		MethodMatch m = context.resolveMethod(null, qualifiedName, Type.EMPTY_TYPES);
		if (m != null)
		{
			MethodCall call = new MethodCall(this.position);
			call.name = this.type.getName();
			call.qualifiedName = qualifiedName;
			call.method = m.theMethod;
			call.dotless = true;
			call.isSugarCall = true;
			return call;
		}
		
		return null;
	}
	
	@Override
	public IAccess resolve3(IContext context, IAccess next)
	{
		String name = this.type.getQualifiedName();
		// TODO SingleElementList
		MethodMatch m = context.resolveMethod(null, name, Arrays.asList(next));
		if (m != null)
		{
			MethodCall call = new MethodCall(this.position, null, name);
			call.addValue(next);
			call.method = m.theMethod;
			call.dotless = true;
			call.isSugarCall = true;
			return call;
		}
		
		return null;
	}
	
	@Override
	public Marker getResolveError()
	{
		return new SyntaxError(this.position, "'" + this.type + "' could not be resolved to a type or field");
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		IClass iclass = this.type.getTheClass();
		if (iclass != null)
		{
			IField field = iclass.getInstanceField();
			if (field != null)
			{
				field.writeGet(writer);
			}
		}
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString("", buffer);
	}
}
