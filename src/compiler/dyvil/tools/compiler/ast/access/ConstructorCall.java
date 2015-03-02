package dyvil.tools.compiler.ast.access;

import java.util.ArrayList;
import java.util.List;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.GenericType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValueList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public final class ConstructorCall extends ASTNode implements IValue, IValueList, ITypeContext
{
	public IType		type;
	public List<IValue>	arguments	= new ArrayList(3);
	
	public boolean		isSugarCall;
	
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
	public void setValues(List<IValue> list)
	{
		this.arguments = list;
	}
	
	@Override
	public void setValue(int index, IValue value)
	{
		this.arguments.set(index, value);
	}
	
	@Override
	public void addValue(IValue value)
	{
		this.arguments.add(value);
	}
	
	@Override
	public List<IValue> getValues()
	{
		return this.arguments;
	}
	
	@Override
	public IValue getValue(int index)
	{
		return this.arguments.get(index);
	}
	
	public void setSugar(boolean sugar)
	{
		this.isSugarCall = sugar;
	}
	
	@Override
	public IType resolveType(String name)
	{
		List<IType> generics = this.type.isGeneric() ? ((GenericType) this.type).generics : null;
		return this.method.resolveType(name, null, this.arguments, null);
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		this.type = this.type.resolve(markers, context);
		
		int len = this.arguments.size();
		for (int i = 0; i < len; i++)
		{
			this.arguments.get(i).resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		int len = this.arguments.size();
		for (int i = 0; i < len; i++)
		{
			IValue v1 = this.arguments.get(i);
			IValue v2 = v1.resolve(markers, context);
			if (v1 != v2)
			{
				this.arguments.set(i, v2);
			}
		}
		
		if (!this.type.isResolved())
		{
			return this;
		}
		
		if (this.type.isArrayType())
		{
			int dims = this.type.getArrayDimensions();
			if (dims != len)
			{
				Marker marker = Markers.create(this.position, "access.constructor.array_length");
				marker.addInfo("Type Dimensions: " + dims);
				marker.addInfo("Number of Length Arguments: " + len);
				markers.add(marker);
				return this;
			}
			
			for (int i = 0; i < len; i++)
			{
				IValue v = this.arguments.get(i);
				IType t = v.getType();
				if (t != Type.INT)
				{
					Marker marker = Markers.create(v.getPosition(), "access.constructor.arraylength_type");
					marker.addInfo("Value Type: " + t);
					markers.add(marker);
				}
			}
			
			return this;
		}
		
		MethodMatch match = this.type.resolveConstructor(this.arguments);
		if (match == null)
		{
			Marker marker = Markers.create(this.position, "resolve.constructor", this.type.toString());
			StringBuilder builder = new StringBuilder("Argument Types: [");
			Util.typesToString("", this.arguments, ", ", builder);
			builder.append(']');
			marker.addInfo(builder.toString());
			markers.add(marker);
			return this;
		}
		
		this.method = match.theMethod;
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		for (IValue v : this.arguments)
		{
			v.check(markers, context);
		}
		
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
			markers.add(Markers.create(this.position, "constructor.interface", iclass.getName()));
		}
		else if (iclass.hasModifier(Modifiers.ABSTRACT))
		{
			markers.add(Markers.create(this.position, "constructor.abstract", iclass.getName()));
		}
		else if (this.method != null)
		{
			this.method.checkArguments(markers, null, this.arguments, this);
			
			if (this.method.hasModifier(Modifiers.DEPRECATED))
			{
				markers.add(Markers.create(this.position, "access.constructor.deprecated", iclass.getName()));
			}
			
			byte access = context.getAccessibility(this.method);
			if (access == IContext.SEALED)
			{
				markers.add(Markers.create(this.position, "access.constructor.sealed", iclass.getName()));
			}
			else if ((access & IContext.READ_ACCESS) == 0)
			{
				markers.add(Markers.create(this.position, "access.constructor.invisible", iclass.getName()));
			}
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		int len = this.arguments.size();
		for (int i = 0; i < len; i++)
		{
			IValue v1 = this.arguments.get(i);
			IValue v2 = v1.foldConstants();
			if (v1 != v2)
			{
				this.arguments.set(i, v2);
			}
		}
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
				this.arguments.get(0).writeExpression(writer);
				this.type.setArrayDimensions(0);
				writer.visitTypeInsn(Opcodes.ANEWARRAY, this.type);
				this.type.setArrayDimensions(1);
				return;
			}
			
			for (int i = 0; i < len; i++)
			{
				this.arguments.get(i).writeExpression(writer);
			}
			
			writer.visitMultiANewArrayInsn(this.type, len);
			return;
		}
		
		int args = this.arguments.size() + 1;
		writer.visitTypeInsn(Opcodes.NEW, this.type);
		writer.visitInsn(Opcodes.DUP);
		
		for (IValue arg : this.arguments)
		{
			arg.writeExpression(writer);
		}
		
		String owner = this.type.getInternalName();
		String name = "<init>";
		String desc = this.method.getDescriptor();
		writer.visitMethodInsn(Opcodes.INVOKESPECIAL, owner, name, desc, false, args, (String) null);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		this.writeExpression(writer);
		writer.visitInsn(Opcodes.ARETURN);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("new ");
		this.type.toString("", buffer);
		if (this.isSugarCall && !Formatting.Method.useJavaFormat)
		{
			buffer.append(Formatting.Method.sugarCallSeperator);
			this.arguments.get(0).toString("", buffer);
		}
		else
		{
			Util.parametersToString(prefix, this.arguments, buffer, Formatting.Method.useJavaFormat);
		}
	}
}
