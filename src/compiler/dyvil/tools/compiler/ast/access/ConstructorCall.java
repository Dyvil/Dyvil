package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public class ConstructorCall extends ASTNode implements ICall
{
	public IType		type;
	public IArguments	arguments	= EmptyArguments.INSTANCE;
	
	public IConstructor	constructor;
	
	public ConstructorCall(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public int valueTag()
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
		return type.isSuperTypeOf(this.type) ? this : null;
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
	
	public ClassConstructor toClassConstructor()
	{
		ClassConstructor cc = new ClassConstructor(this.position);
		cc.type = this.type;
		cc.constructor = this.constructor;
		cc.arguments = this.arguments;
		return cc;
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
			
			if (!(this.arguments instanceof ArgumentList))
			{
				markers.add(markers.create(this.position, "access.constructor.array"));
				return this;
			}
			
			ArgumentList paramList = (ArgumentList) this.arguments;
			
			for (int i = 0; i < len; i++)
			{
				IValue v = paramList.getValue(i);
				IType t = v.getType();
				if (t != Types.INT)
				{
					Marker marker = markers.create(v.getPosition(), "access.constructor.arraylength_type");
					marker.addInfo("Value Type: " + t);
				}
			}
			
			return this;
		}
		
		IConstructor match = IContext.resolveConstructor(markers, this.type, this.arguments);
		if (match == null)
		{
			Marker marker = markers.create(this.position, "resolve.constructor", this.type.toString());
			StringBuilder builder = new StringBuilder("Argument Types: ");
			Util.typesToString("", this.arguments, ", ", builder);
			marker.addInfo(builder.toString());
			
			return this;
		}
		
		this.constructor = match;
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.constructor != null)
		{
			this.constructor.checkArguments(markers, this.arguments);
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
		
		if (this.constructor != null)
		{
			if (this.constructor.hasModifier(Modifiers.DEPRECATED))
			{
				markers.add(this.position, "access.constructor.deprecated", iclass.getName());
			}
			
			byte access = context.getAccessibility(this.constructor);
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
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		if (this.type.isArrayType())
		{
			int len = this.arguments.size();
			
			if (len == 1)
			{
				this.arguments.getFirstValue().writeExpression(writer);
				writer.writeNewArray(this.type, 1);
				return;
			}
			
			ArgumentList paramList = (ArgumentList) this.arguments;
			
			for (int i = 0; i < len; i++)
			{
				paramList.getValue(i).writeExpression(writer);
			}
			
			writer.writeNewArray(this.type, len);
			return;
		}
		
		this.constructor.writeCall(writer, this.arguments, null);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
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
