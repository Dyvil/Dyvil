package dyvil.tools.compiler.ast.expression;

import java.util.Iterator;

import dyvil.collection.iterator.ArrayIterator;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITypeList;
import dyvil.tools.compiler.ast.type.TupleType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Util;

public final class Tuple extends ASTNode implements IValue, IValueList
{
	public static final IClass	TUPLE_CONVERTIBLE	= Package.dyvilLangLiteral.resolveClass("TupleConvertible");
	
	private IValue[]			values;
	private int					valueCount;
	
	private IType				tupleType;
	private IMethod				method;
	private IArguments			arguments;
	
	public Tuple(ICodePosition position)
	{
		this.position = position;
		this.values = new IValue[3];
	}
	
	public Tuple(ICodePosition position, IValue[] values)
	{
		this.position = position;
		this.values = values;
		this.valueCount = values.length;
	}
	
	@Override
	public int valueTag()
	{
		return TUPLE;
	}
	
	@Override
	public boolean isPrimitive()
	{
		return false;
	}
	
	@Override
	public Iterator<IValue> iterator()
	{
		return new ArrayIterator(this.values);
	}
	
	@Override
	public int valueCount()
	{
		return this.valueCount;
	}
	
	@Override
	public boolean isEmpty()
	{
		return this.valueCount == 0;
	}
	
	@Override
	public void setValue(int index, IValue value)
	{
		this.values[index] = value;
	}
	
	@Override
	public void addValue(IValue value)
	{
		int index = this.valueCount++;
		if (this.valueCount > this.values.length)
		{
			IValue[] temp = new IValue[this.valueCount];
			System.arraycopy(this.values, 0, temp, 0, index);
			this.values = temp;
		}
		this.values[index] = value;
	}
	
	@Override
	public void addValue(int index, IValue value)
	{
		IValue[] temp = new IValue[++this.valueCount];
		System.arraycopy(this.values, 0, temp, 0, index);
		temp[index] = value;
		System.arraycopy(this.values, index, temp, index + 1, this.valueCount - index - 1);
		this.values = temp;
	}
	
	@Override
	public IValue getValue(int index)
	{
		return this.values[index];
	}
	
	@Override
	public IType getType()
	{
		if (this.tupleType != null)
		{
			return this.tupleType;
		}
		
		TupleType t = new TupleType(this.valueCount);
		for (int i = 0; i < this.valueCount; i++)
		{
			IType type = this.values[i].getType();
			// Tuple Value Boxing
			if (type.isPrimitive())
			{
				t.addType(type.getReferenceType());
			}
			else
			{
				t.addType(type);
			}
		}
		return this.tupleType = t;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (this.valueCount == 1)
		{
			return this.values[0].withType(type, typeContext, markers, context);
		}
		
		if (type.getTheClass().getAnnotation(TUPLE_CONVERTIBLE) != null)
		{
			this.tupleType = type;
			return this;
		}
		if (TupleType.isSuperType(type, this.values, this.valueCount))
		{
			ITypeList typeList = (ITypeList) this.getType();
			for (int i = 0; i < this.valueCount; i++)
			{
				IType elementType = typeList.getType(i);
				IValue value = this.values[i];
				IValue value1 = value.withType(elementType, typeContext, markers, context);
				if (value1 == null)
				{
					Marker m = markers.create(value.getPosition(), "tuple.type");
					m.addInfo("Pattern Type: " + value.getType());
					m.addInfo("Tuple Type: " + elementType);
				}
				else
				{
					this.values[i] = value = value1;
				}
			}
			return this;
		}
		
		return null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (this.valueCount == 1)
		{
			return this.values[0].isType(type);
		}
		
		return TupleType.isSuperType(type, this.values, this.valueCount) || type.getTheClass().getAnnotation(TUPLE_CONVERTIBLE) != null;
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		if (this.valueCount == 1)
		{
			return this.values[0].getTypeMatch(type);
		}
		
		return type.getSubTypeDistance(this.getType());
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			IValue v = this.values[i];
			v.resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.valueCount == 1)
		{
			return this.values[0].resolve(markers, context);
		}
		
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i] = this.values[i].resolve(markers, context);
		}
		
		return this;
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.tupleType.typeTag() == IType.TUPLE)
		{
			for (int i = 0; i < this.valueCount; i++)
			{
				this.values[i].checkTypes(markers, context);
			}
			
			return;
		}
		
		IMethod m = IContext.resolveMethod(this.getType(), null, Name.apply, this.arguments = new ArgumentList(this.values, this.valueCount));
		if (m == null)
		{
			StringBuilder builder = new StringBuilder();
			if (this.valueCount > 0)
			{
				this.values[0].getType().toString("", builder);
				for (int i = 1; i < this.valueCount; i++)
				{
					builder.append(", ");
					this.values[i].getType().toString("", builder);
				}
			}
			
			markers.add(this.position, "tuple.method", builder.toString(), this.tupleType.toString());
		}
		else
		{
			this.method = m;
			m.checkArguments(markers, this.position, context, null, this.arguments, null);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].check(markers, context);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i] = this.values[i].foldConstants();
		}
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i] = this.values[i].cleanup(context, compilableList);
		}
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		if (this.method != null)
		{
			this.method.writeCall(writer, null, this.arguments, this.tupleType);
			return;
		}
		
		String internal = this.tupleType.getInternalName();
		writer.writeTypeInsn(Opcodes.NEW, internal);
		writer.writeInsn(Opcodes.DUP);
		
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].writeExpression(writer);
		}
		
		String owner = internal;
		String desc = TupleType.getConstructorDescriptor(this.valueCount);
		writer.writeInvokeInsn(Opcodes.INVOKESPECIAL, owner, "<init>", desc, false);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].writeStatement(writer);
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(Formatting.Expression.tupleStart);
		Util.astToString(prefix, this.values, this.valueCount, Formatting.Expression.tupleSeperator, buffer);
		buffer.append(Formatting.Expression.tupleEnd);
	}
}
