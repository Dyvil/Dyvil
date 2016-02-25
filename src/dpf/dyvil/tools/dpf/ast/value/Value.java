package dyvil.tools.dpf.ast.value;

import dyvil.collection.Collection;
import dyvil.collection.Map;
import dyvil.tools.dpf.visitor.ValueVisitor;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.position.ICodePosition;

public interface Value extends IASTNode
{
	@Override
	default ICodePosition getPosition()
	{
		return null;
	}
	
	@Override
	default void setPosition(ICodePosition position)
	{
	}
	
	@Override
	default void expandPosition(ICodePosition position)
	{
	}
	
	@Override
	void toString(String prefix, StringBuilder buffer);
	
	void accept(ValueVisitor visitor);

	static Value wrap(Object value)
	{
		if (value == null)
		{
			return null;
		}

		if (value instanceof Value)
		{
			return (Value) value;
		}

		Class<?> valueClass = value.getClass();
		if (valueClass == String.class)
		{
			return new StringValue((String) value);
		}
		if (valueClass == Integer.class)
		{
			return new IntValue((Integer) value);
		}
		if (valueClass == Long.class)
		{
			return new LongValue((Long) value);
		}
		if (valueClass == Float.class)
		{
			return new FloatValue((Float) value);
		}
		if (valueClass == Double.class)
		{
			return new DoubleValue((Double) value);
		}
		if (valueClass == Name.class)
		{
			return new NameAccess((Name) value);
		}
		if (valueClass == Boolean.class)
		{
			return new NameAccess(Name.getSpecial(value.toString()));
		}

		if (value instanceof Collection)
		{
			return new ListValue((Collection) value);
		}
		if (value instanceof Map)
		{
			return new MapValue((Map) value);
		}

		return null;
	}
}
