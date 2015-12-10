package dyvil.tools.dpf.ast.value;

import dyvil.collection.Entry;
import dyvil.collection.Map;
import dyvil.collection.mutable.ArrayMap;
import dyvil.tools.dpf.ast.Expandable;
import dyvil.tools.dpf.visitor.MapVisitor;
import dyvil.tools.dpf.visitor.ValueVisitor;
import dyvil.tuple.Tuple2;

import java.util.function.BiFunction;

public class MapValue extends ValueCreator implements Value, MapVisitor, Expandable
{
	protected Map<Value, Value> entries = new ArrayMap<>();

	private Value tempKey;
	
	public MapValue()
	{
	}

	public MapValue(Map<?, ?> map)
	{
		final int size = map.size();
		this.entries = new ArrayMap<>(size);

		for (Entry<?, ?> entry : map)
		{
			this.entries.put(Value.wrap(entry.getKey()), Value.wrap(entry.getValue()));
		}
	}
	
	@Override
	protected void setValue(Value value)
	{
		if (this.tempKey == null)
		{
			this.tempKey = value;
		}
		else
		{
			this.entries.put(this.tempKey, value);
			this.tempKey = null;
		}
	}
	
	@Override
	public ValueVisitor visitKey()
	{
		return this;
	}
	
	@Override
	public ValueVisitor visitValue()
	{
		return this;
	}
	
	@Override
	public void accept(ValueVisitor visitor)
	{
		MapVisitor mapVisitor = visitor.visitMap();

		for (Entry<Value, Value> entry : this.entries)
		{
			entry.getKey().accept(mapVisitor.visitKey());
			entry.getValue().accept(mapVisitor.visitValue());
		}
		
		mapVisitor.visitEnd();
	}

	@Override
	public MapValue expand(Map<String, Object> mappings, boolean mutate)
	{
		final BiFunction<Value, Value, Entry<? extends Value, ? extends Value>> entryBiFunction = (k, v) -> new Tuple2<>(
				Value.wrap(Expandable.expand(k, mappings, true)), Value.wrap(Expandable.expand(v, mappings, mutate)));

		if (mutate)
		{
			this.entries.mapEntries(entryBiFunction);
			return this;
		}
		else
		{
			MapValue copy = new MapValue();
			copy.entries = this.entries.entryMapped(entryBiFunction);
			return copy;
		}
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		int len = this.entries.size();
		if (len <= 0)
		{
			buffer.append("{}");
			return;
		}

		Value[] keys = this.entries.toKeyArray(Value.class);
		Value[] values = this.entries.toValueArray(Value.class);
		
		String prefix1 = prefix + "\t";
		
		buffer.append("{\n").append(prefix1);
		keys[0].toString(prefix1, buffer);
		buffer.append(" : ");
		values[0].toString(prefix1, buffer);
		for (int i = 1; i < len; i++)
		{
			buffer.append(",\n").append(prefix1);
			keys[i].toString(prefix1, buffer);
			buffer.append(" : ");
			values[i].toString(prefix1, buffer);
		}
		buffer.append('\n').append(prefix).append('}');
	}
}
