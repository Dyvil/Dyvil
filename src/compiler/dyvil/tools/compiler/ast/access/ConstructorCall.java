package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.Parameter;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.GenericType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public final class ConstructorCall extends ASTNode implements IValue, ICall, ITypeContext
{
	public IType		type;
	public IArguments	arguments	= EmptyArguments.INSTANCE;
	
	public IMethod		method;
	
	public ConstructorCall(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int getValueType()
	{
		return CONSTRUCTOR_CALL;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return false;
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
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
	public void setArguments(IArguments arguments)
	{
		this.arguments = arguments;
	}
	
	@Override
	public IArguments getArguments()
	{
		return this.arguments;
	}
	
	@Override
	public IType resolveType(String name)
	{
		ITypeList generics = this.type.isGeneric() ? (GenericType) this.type : null;
		return this.method.resolveType(name, null, this.arguments, generics);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.type = this.type.resolve(markers, context);
		this.arguments.resolveTypes(markers, context);
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.arguments.resolve(markers, context);
		
		if (!this.type.isResolved())
		{
			return this;
		}
		
		if (this.type.isArrayType())
		{
			int len = this.arguments.size();
			int dims = this.type.getArrayDimensions();
			if (dims != len)
			{
				Marker marker = markers.create(this.position, "access.constructor.array_length");
				marker.addInfo("Type Dimensions: " + dims);
				marker.addInfo("Number of Length Arguments: " + len);
				
				return this;
			}
			
			// TODO Handle this cast
			ArgumentList paramList = (ArgumentList) this.arguments;
			
			for (int i = 0; i < len; i++)
			{
				IValue v = paramList.getValue(i);
				IType t = v.getType();
				if (t != Type.INT)
				{
					Marker marker = markers.create(v.getPosition(), "access.constructor.arraylength_type");
					marker.addInfo("Value Type: " + t);
					
				}
			}
			
			return this;
		}
		
		MethodMatch match = this.type.resolveConstructor(this.arguments);
		if (match == null)
		{
			Marker marker = markers.create(this.position, "resolve.constructor", this.type.toString());
			StringBuilder builder = new StringBuilder("Argument Types: [");
			Util.typesToString("", this.arguments, ", ", builder);
			marker.addInfo(builder.append(']').toString());
			
			return this;
		}
		
		this.method = match.theMethod;
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.method != null)
		{
			this.method.checkArguments(markers, null, this.arguments, this);
		}
		this.arguments.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.arguments.check(markers, context);
		
		if (this.type.isArrayType())
		{
			return;
		}
		
		IClass iclass = this.type.getTheClass();
		if (iclass == null)
		{
			return;
		}
		if (iclass.hasModifier(Modifiers.INTERFACE_CLASS))
		{
			markers.add(this.position, "constructor.interface", iclass.getName());
		}
		else if (iclass.hasModifier(Modifiers.ABSTRACT))
		{
			markers.add(this.position, "constructor.abstract", iclass.getName());
		}
		
		if (this.method != null)
		{
			if (this.method.hasModifier(Modifiers.DEPRECATED))
			{
				markers.add(this.position, "access.constructor.deprecated", iclass.getName());
			}
			
			byte access = context.getAccessibility(this.method);
			if (access == IContext.SEALED)
			{
				markers.add(this.position, "access.constructor.sealed", iclass.getName());
			}
			else if ((access & IContext.READ_ACCESS) == 0)
			{
				markers.add(this.position, "access.constructor.invisible", iclass.getName());
			}
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		this.arguments.foldConstants();
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		if (this.type.isArrayType())
		{
			int len = this.arguments.size();
			
			if (len == 1)
			{
				this.arguments.getFirstValue().writeExpression(writer);
				this.type.setArrayDimensions(0);
				writer.writeTypeInsn(Opcodes.ANEWARRAY, this.type);
				this.type.setArrayDimensions(1);
				return;
			}
			
			// TODO Handle this cast
			ArgumentList paramList = (ArgumentList) this.arguments;
			
			for (int i = 0; i < len; i++)
			{
				paramList.getValue(i).writeExpression(writer);
			}
			
			writer.writeNewArray(this.type, len);
			return;
		}
		
		writer.writeTypeInsn(Opcodes.NEW, this.type);
		writer.writeInsn(Opcodes.DUP);
		
		int args = this.method.parameterCount();
		if (this.method.isVarargs())
		{
			int len = args - 1;
			Parameter param;
			for (int i = 0; i < len; i++)
			{
				param = this.method.getParameter(i);
				this.arguments.writeValue(i, param.qualifiedName, param.defaultValue, writer);
			}
			param = this.method.getParameter(len);
			this.arguments.writeVarargsValue(len, param.qualifiedName, param.type, writer);
		}
		else
		{
			for (int i = 0; i < args; i++)
			{
				Parameter param = this.method.getParameter(i);
				this.arguments.writeValue(i, param.qualifiedName, param.defaultValue, writer);
			}
		}
		
		String owner = this.type.getInternalName();
		String name = "<init>";
		String desc = this.method.getDescriptor();
		writer.writeInvokeInsn(Opcodes.INVOKESPECIAL, owner, name, desc, false, 1 + args, (String) null);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.writeExpression(writer);
		writer.writeInsn(Opcodes.ARETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("new ");
		this.type.toString("", buffer);
		this.arguments.toString(prefix, buffer);
	}
}
