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
		if (valueClass == dyvil.lang.Int.class)
		{
			return new IntValue(((dyvil.lang.Int) value).intValue());
		}
		if (valueClass == dyvil.lang.Long.class)
		{
			return new LongValue(((dyvil.lang.Long) value).longValue());
		}
		if (valueClass == dyvil.lang.Float.class)
		{
			return new FloatValue(((dyvil.lang.Float) value).floatValue());
		}
		if (valueClass == dyvil.lang.Double.class)
		{
			return new DoubleValue(((dyvil.lang.Double) value).doubleValue());
		}
		if (valueClass == Name.class)
		{
			return new NameAccess((Name) value);
		}
		if (valueClass == dyvil.lang.Boolean.class)
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
