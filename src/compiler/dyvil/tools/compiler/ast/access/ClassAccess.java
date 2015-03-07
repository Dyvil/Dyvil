package dyvil.tools.compiler.ast.access;

import java.util.List;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.GenericType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class ClassAccess extends ASTNode implements IValue
{
	public IType	type;
	
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
	public int getValueType()
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
		return Type.isSuperType(type, this.type) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return Type.isSuperType(type, this.type);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (this.type.equals(type))
		{
			return 3;
		}
		else if (type.isSuperTypeOf(this.type))
		{
			return 2;
		}
		return 0;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		this.type = this.type.resolve(null, context);
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
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
		
		MethodMatch m = context.resolveMethod(null, qualifiedName, EmptyArguments.INSTANCE);
		if (m != null)
		{
			MethodCall call = new MethodCall(this.position);
			call.name = this.type.getName();
			call.qualifiedName = qualifiedName;
			call.method = m.theMethod;
			call.dotless = true;
			if (this.type.isGeneric())
			{
				// Copy generic Type arguments
				GenericType generic = (GenericType) this.type;
				call.generics = generic.generics;
				call.genericCount = generic.genericCount;
			}
			call.arguments = EmptyArguments.INSTANCE;
			return call;
		}
		
		if (!this.type.isResolved())
		{
			markers.add(Markers.create(this.position, this.type.isArrayType() ? "resolve.type" : "resolve.any", this.type.toString()));
		}
		
		return this;
	}
	
	public MethodCall toMethodCall() {
		MethodCall call = new MethodCall(this.position);
		call.name = this.type.getName();
		call.qualifiedName = this.type.getQualifiedName();
		call.dotless = true;
		if (this.type.isGeneric())
		{
			// Copy generic Type arguments
			GenericType generic = (GenericType) this.type;
			call.generics = generic.generics;
			call.genericCount = generic.genericCount;
		}
		call.arguments = EmptyArguments.INSTANCE;
		return call;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		IClass iclass = this.type.getTheClass();
		if (iclass != null)
		{
			if (iclass.hasModifier(Modifiers.DEPRECATED))
			{
				markers.add(Markers.create(this.position, "access.type.deprecated", iclass.getName()));
			}
			
			if (context.getAccessibility(iclass) == IContext.SEALED)
			{
				markers.add(Markers.create(this.position, "access.type.sealed", iclass.getName()));
			}
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		return this;
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
				field.writeGet(writer, null);
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
