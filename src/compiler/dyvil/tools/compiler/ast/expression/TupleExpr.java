package dyvil.tools.compiler.ast.expression;

import dyvil.collection.iterator.ArrayIterator;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.VoidValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.TupleType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.util.Iterator;

public final class TupleExpr implements IValue, IValueList
{
	public static final class Types
	{
		public static final IClass TUPLE_CONVERTIBLE = Package.dyvilLangLiteral.resolveClass("TupleConvertible");
		
		private Types()
		{
			// no instances
		}
	}
	
	protected ICodePosition position;
	
	protected IValue[] values;
	protected int      valueCount;
	
	// Metadata
	private IType tupleType;
	
	public TupleExpr(ICodePosition position)
	{
		this.position = position;
		this.values = new IValue[3];
	}
	
	public TupleExpr(ICodePosition position, IValue[] values, int valueCount)
	{
		this.position = position;
		this.values = values;
		this.valueCount = valueCount;
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
		return TUPLE;
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
	public boolean isResolved()
	{
		return this.tupleType != null;
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
			t.addType(type);
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
		
		IAnnotation annotation = type.getTheClass().getAnnotation(Types.TUPLE_CONVERTIBLE);
		if (annotation != null)
		{
			return new LiteralConversion(this, annotation, new ArgumentList(this.values, this.valueCount))
					.withType(type, typeContext, markers, context);
		}
		
		IClass tupleClass = TupleType.getTupleClass(this.valueCount);
		if (!tupleClass.isSubTypeOf(type))
		{
			return null;
		}
		
		IClass iclass = type.getTheClass();
		
		for (int i = 0; i < this.valueCount; i++)
		{
			IType elementType = iclass == dyvil.tools.compiler.ast.type.Types.OBJECT_CLASS ?
					dyvil.tools.compiler.ast.type.Types.ANY :
					type.resolveTypeSafely(iclass.getTypeParameter(i));
			
			IValue value = this.values[i];
			IValue value1 = IType.convertValue(value, elementType, typeContext, markers, context);
			
			if (value1 == null)
			{
				Marker m = Markers.semantic(value.getPosition(), "tuple.element.type.incompatible");
				m.addInfo(Markers.getSemantic("value.type", value.getType()));
				m.addInfo(Markers.getSemantic("tuple.element.type", elementType.getConcreteType(typeContext)));
				markers.add(m);
			}
			else
			{
				this.values[i] = value = value1;
			}
		}
		
		this.tupleType = null;
		this.tupleType = this.getType();
		return this;
	}
	
	@Override
	public boolean isType(IType type)
	{
		if (this.valueCount == 1)
		{
			return this.values[0].isType(type);
		}
		
		return TupleType.isSuperType(type, this.values, this.valueCount)
				|| type.getTheClass().getAnnotation(Types.TUPLE_CONVERTIBLE) != null;
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
			this.values[i].resolveTypes(markers, context);
		}
	}
	
	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		if (this.valueCount == 0)
		{
			return new VoidValue(this.position);
		}
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
		for (int i = 0; i < this.valueCount; i++)
		{
			this.values[i].checkTypes(markers, context);
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
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		String internal = this.tupleType.getInternalName();
		writer.writeTypeInsn(Opcodes.NEW, internal);
		writer.writeInsn(Opcodes.DUP);
		
		for (int i = 0; i < this.valueCount; i++)
		{
			IValue value = this.values[i];
			value.writeExpression(writer, dyvil.tools.compiler.ast.type.Types.OBJECT);
		}
		
		String owner = internal;
		String desc = TupleType.getConstructorDescriptor(this.valueCount);
		writer.writeInvokeInsn(Opcodes.INVOKESPECIAL, owner, "<init>", desc, false);

		if (type == dyvil.tools.compiler.ast.type.Types.VOID)
		{
			writer.writeInsn(Opcodes.ARETURN);
		}
		else if (type != null)
		{
			this.tupleType.writeCast(writer, type, this.getLineNumber());
		}
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.valueCount == 0)
		{
			if (Formatting.getBoolean("tuple.empty.space_between"))
			{
				buffer.append("( )");
			}
			else
			{
				buffer.append("()");
			}
			return;
		}

		buffer.append('(');
		if (Formatting.getBoolean("tuple.open_paren.space_after"))
		{
			buffer.append(' ');
		}

		Util.astToString(prefix, this.values, this.valueCount, Formatting.getSeparator("tuple.separator", ','), buffer);

		if (Formatting.getBoolean("tuple.close_paren.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append(')');
	}
}
