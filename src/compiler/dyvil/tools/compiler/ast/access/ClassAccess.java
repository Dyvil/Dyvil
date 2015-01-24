package dyvil.tools.compiler.ast.access;

import java.util.List;

import dyvil.collections.SingleElementList;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;

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
		
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		IClass iclass = this.type.getTheClass();
		if (iclass != null)
		{
			if (context.getAccessibility(iclass) == IContext.SEALED)
			{
				markers.add(Markers.create(this.position, "access.class.sealed", iclass.getName()));
			}
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		return this;
	}
	
	@Override
	public boolean resolve(IContext context, List<Marker> markers)
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
		MethodMatch m = context.resolveMethod(null, name, new SingleElementList(next));
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
		Marker error;
		if (!this.type.isArrayType())
		{
			error = Markers.create(this.position, "resolve.any", this.type.toString());
		}
		else
		{
			error = Markers.create(this.position, "resolve.type", this.type.toString());
		}
		return error;
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
