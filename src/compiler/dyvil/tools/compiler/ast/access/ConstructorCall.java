package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.IType.TypePosition;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class ConstructorCall implements ICall
{
	protected ICodePosition	position;
	protected IType			type;
	protected IArguments	arguments;
	
	protected IConstructor constructor;
	
	protected ConstructorCall()
	{
		this.arguments = EmptyArguments.INSTANCE;
	}
	
	public ConstructorCall(ICodePosition position)
	{
		this.position = position;
		this.arguments = EmptyArguments.INSTANCE;
	}
	
	public ConstructorCall(ICodePosition position, IType type, IArguments arguments)
	{
		this.position = position;
		this.type = type;
		this.arguments = arguments;
	}
	
	public ConstructorCall(ICodePosition position, IConstructor constructor, IArguments arguments)
	{
		this.position = position;
		this.constructor = constructor;
		this.type = constructor.getType();
		this.arguments = arguments;
	}
	
	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}
	
	@Override
	public void setPosition(ICodePosition position)
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
	
	public IConstructor getConstructor()
	{
		return this.constructor;
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
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return type.isSuperTypeOf(this.type) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type.isSuperTypeOf(this.type);
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
		if (this.type != null)
		{
			this.type = this.type.resolveType(markers, context);
		}
		else
		{
			markers.add(this.position, "constructor.invalid");
			this.type = Types.UNKNOWN;
		}
		
		if (this.arguments.isEmpty())
		{
			this.arguments = EmptyArguments.VISIBLE;
		}
		else
		{
			this.arguments.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.type.resolve(markers, context);
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
				Marker marker = markers.create(this.position, "constructor.access.array.length");
				marker.addInfo("Type Dimensions: " + dims);
				marker.addInfo("Number of Length Arguments: " + len);
				
				return this;
			}
			
			if (!(this.arguments instanceof ArgumentList))
			{
				markers.add(this.position, "constructor.access.array");
				return this;
			}
			
			ArgumentList paramList = (ArgumentList) this.arguments;
			
			for (int i = 0; i < len; i++)
			{
				IValue v = paramList.getValue(i);
				IValue v1 = v.withType(Types.INT, Types.INT, markers, context);
				if (v1 == null)
				{
					Marker marker = markers.create(v.getPosition(), "constructor.access.array.type");
					marker.addInfo("Value Type: " + v.getType());
				}
				else
				{
					paramList.setValue(i, v1);
				}
			}
			
			return this;
		}
		
		this.constructor = IContext.resolveConstructor(this.type, this.arguments);
		if (this.constructor == null)
		{
			this.reportResolve(markers);
			return this;
		}
		
		if (this.constructor.getTheClass().isGeneric() && !this.type.isGenericType())
		{
			this.type = this.constructor.checkGenericType(markers, position, context, type, arguments);
		}
		
		this.constructor.checkArguments(markers, this.position, context, this.type, this.arguments);
		return this;
	}
	
	protected void reportResolve(MarkerList markers)
	{
		if (!this.type.isResolved())
		{
			return;
		}
		
		Marker marker = markers.create(this.position, "resolve.constructor", this.type.toString());
		if (!this.arguments.isEmpty())
		{
			StringBuilder builder = new StringBuilder("Argument Types: ");
			this.arguments.typesToString(builder);
			marker.addInfo(builder.toString());
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.type.checkType(markers, context, TypePosition.TYPE);
		this.arguments.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.type.check(markers, context);
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
			markers.add(this.position, "constructor.interface", this.type);
			return;
		}
		if (iclass.hasModifier(Modifiers.ABSTRACT))
		{
			markers.add(this.position, "constructor.abstract", this.type);
		}
		
		if (this.constructor != null)
		{
			this.constructor.checkCall(markers, this.position, context, this.arguments);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		this.arguments.foldConstants();
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.arguments.cleanup(context, compilableList);
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
				writer.writeNewArray(this.type.getElementType(), 1);
				return;
			}
			
			ArgumentList paramList = (ArgumentList) this.arguments;
			IType type = this.type;
			
			for (int i = 0; i < len; i++)
			{
				paramList.getValue(i).writeExpression(writer);
				type = type.getElementType();
			}
			
			writer.writeNewArray(type, len);
			return;
		}
		
		this.constructor.writeCall(writer, this.arguments, null, this.getLineNumber());
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
