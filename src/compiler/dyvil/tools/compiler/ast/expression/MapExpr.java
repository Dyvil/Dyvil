package dyvil.tools.compiler.ast.expression;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Mutability;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.MapType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class MapExpr implements IValue
{
	private static final TypeChecker.MarkerSupplier KEY_MARKER_SUPPLIER   = TypeChecker.markerSupplier(
		"map.key.type.incompatible", "map.key.type.expected", "map.key.type.actual");
	private static final TypeChecker.MarkerSupplier VALUE_MARKER_SUPPLIER = TypeChecker.markerSupplier(
		"map.value.type.incompatible", "map.value.type.expected", "map.value.type.actual");

	protected ICodePosition position;

	protected IValue[] keys;
	protected IValue[] values;

	protected int count;

	// Metadata
	private IType type;
	private IType keyType;
	private IType valueType;

	public MapExpr(ICodePosition position)
	{
		this.position = position;
	}

	public MapExpr(ICodePosition position, IValue[] keys, IValue[] values, int count)
	{
		this.position = position;
		this.keys = keys;
		this.values = values;
		this.count = count;
	}

	@Override
	public int valueTag()
	{
		return MAP;
	}

	@Override
	public void setPosition(ICodePosition position)
	{
		this.position = position;
	}

	@Override
	public ICodePosition getPosition()
	{
		return this.position;
	}

	public IType getKeyType()
	{
		if (this.keyType != null)
		{
			return this.keyType;
		}
		return this.keyType = ArrayExpr.getCommonType(this.keys, this.count);
	}

	public IType getValueType()
	{
		if (this.valueType != null)
		{
			return this.valueType;
		}
		return this.valueType = ArrayExpr.getCommonType(this.values, this.count);
	}

	@Override
	public boolean isResolved()
	{
		return this.type != null && this.type.isResolved();
	}

	@Override
	public IType getType()
	{
		if (this.type == null)
		{
			return this.type = new MapType(this.getKeyType(), this.getValueType(), Mutability.IMMUTABLE,
			                               MapType.MapTypes.IMMUTABLE_MAP_CLASS);
		}
		return this.type;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}

	@Override
	public IValue withType(IType mapType, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (!MapType.MapTypes.MAP_CLASS.isSubTypeOf(mapType))
		{
			IAnnotation annotation = mapType.getTheClass().getAnnotation(MapType.MapTypes.MAP_CONVERTIBLE_CLASS);
			if (annotation != null)
			{
				ArgumentList arguments = new ArgumentList(new IValue[] { new ArrayExpr(this.keys, this.count),
					new ArrayExpr(this.values, this.count) }, 2);
				return new LiteralConversion(this, annotation, arguments)
					       .withType(mapType, typeContext, markers, context);
			}
			return null;
		}

		final IType keyType = this.keyType = Types.resolveTypeSafely(mapType, MapType.MapTypes.KEY_VARIABLE);
		final IType valueType = this.valueType = Types.resolveTypeSafely(mapType, MapType.MapTypes.VALUE_VARIABLE);

		for (int i = 0; i < this.count; i++)
		{
			this.keys[i] = TypeChecker
				               .convertValue(this.keys[i], keyType, typeContext, markers, context, KEY_MARKER_SUPPLIER);

			this.values[i] = TypeChecker.convertValue(this.values[i], valueType, typeContext, markers, context,
			                                          VALUE_MARKER_SUPPLIER);
		}

		return this;
	}

	@Override
	public boolean isType(IType type)
	{
		if (!MapType.MapTypes.MAP_CLASS.isSubTypeOf(type))
		{
			return this.isConvertibleFrom(type);
		}

		IType keyType = Types.resolveTypeSafely(type, MapType.MapTypes.KEY_VARIABLE);
		IType valueType = Types.resolveTypeSafely(type, MapType.MapTypes.VALUE_VARIABLE);

		for (int i = 0; i < this.count; i++)
		{
			if (!this.keys[i].isType(keyType))
			{
				return false;
			}
			if (!this.values[i].isType(valueType))
			{
				return false;
			}
		}

		return true;
	}

	private boolean isConvertibleFrom(IType type)
	{
		return type.getTheClass().getAnnotation(MapType.MapTypes.MAP_CONVERTIBLE_CLASS) != null;
	}

	@Override
	public int getTypeMatch(IType type)
	{
		if (!MapType.MapTypes.MAP_CLASS.isSubTypeOf(type))
		{
			return this.isConvertibleFrom(type) ? CONVERSION_MATCH : 0;
		}

		if (this.count == 0)
		{
			return 1;
		}

		final IType keyType = Types.resolveTypeSafely(type, MapType.MapTypes.KEY_VARIABLE);
		final IType valueType = Types.resolveTypeSafely(type, MapType.MapTypes.VALUE_VARIABLE);

		int total = 0;
		for (int i = 0; i < this.count; i++)
		{
			float match = this.keys[i].getTypeMatch(keyType);
			if (match <= 0F)
			{
				return 0;
			}
			total += match;

			match = this.values[i].getTypeMatch(valueType);
			if (match <= 0)
			{
				return 0;
			}
			total += match;
		}

		return 1 + total / (this.count * 2);
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.count; i++)
		{
			this.keys[i].resolveTypes(markers, context);
			this.values[i].resolveTypes(markers, context);
		}
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.count; i++)
		{
			this.keys[i] = this.keys[i].resolve(markers, context);
			this.values[i] = this.values[i].resolve(markers, context);
		}
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.count; i++)
		{
			this.keys[i].checkTypes(markers, context);
			this.values[i].checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		for (int i = 0; i < this.count; i++)
		{
			this.keys[i].check(markers, context);
			this.values[i].check(markers, context);
		}
	}

	@Override
	public IValue foldConstants()
	{
		for (int i = 0; i < this.count; i++)
		{
			this.keys[i] = this.keys[i].foldConstants();
			this.values[i] = this.values[i].foldConstants();
		}
		return this;
	}

	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		for (int i = 0; i < this.count; i++)
		{
			this.keys[i] = this.keys[i].cleanup(context, compilableList);
			this.values[i] = this.values[i].cleanup(context, compilableList);
		}
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		if (this.count == 0)
		{
			writer.visitFieldInsn(Opcodes.GETSTATIC, "dyvil/collection/immutable/EmptyMap", "instance",
			                      "Ldyvil/collection/immutable/EmptyMap;");
			return;
		}

		IType keyObject = this.keyType.getObjectType();
		IType valueObject = this.valueType.getObjectType();

		writer.visitLdcInsn(this.count);
		writer.visitMultiANewArrayInsn("java/lang/Object", 1);

		for (int i = 0; i < this.count; i++)
		{
			writer.visitInsn(Opcodes.DUP);
			writer.visitLdcInsn(i);
			this.keys[i].writeExpression(writer, keyObject);
			writer.visitInsn(Opcodes.AASTORE);
		}

		writer.visitLdcInsn(this.count);
		writer.visitMultiANewArrayInsn("java/lang/Object", 1);

		for (int i = 0; i < this.count; i++)
		{
			writer.visitInsn(Opcodes.DUP);
			writer.visitLdcInsn(i);
			this.values[i].writeExpression(writer, valueObject);
			writer.visitInsn(Opcodes.AASTORE);
		}

		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/collection/ImmutableMap", "apply",
		                       "([Ljava/lang/Object;[Ljava/lang/Object;)Ldyvil/collection/ImmutableMap;", true);

		if (type != null)
		{
			this.getType().writeCast(writer, type, this.getLineNumber());
		}
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.count <= 0)
		{
			if (Formatting.getBoolean("map.empty.space_between"))
			{
				buffer.append("[ ]");
			}
			else
			{
				buffer.append("[]");
			}
			return;
		}

		String mapPrefix = Formatting.getIndent("map.entry_separator.indent", prefix);
		buffer.append('[');
		if (Formatting.getBoolean("map.open_paren.space_after"))
		{
			buffer.append(' ');
		}

		this.keys[0].toString(mapPrefix, buffer);
		Formatting.appendSeparator(buffer, "map.key_value_separator", ':');
		this.values[0].toString(mapPrefix, buffer);

		for (int i = 1; i < this.count; i++)
		{
			Formatting.appendSeparator(buffer, "map.entry_separator", ',');

			this.keys[i].toString(mapPrefix, buffer);
			Formatting.appendSeparator(buffer, "map.key_value_separator", ':');
			this.values[i].toString(mapPrefix, buffer);
		}

		if (Formatting.getBoolean("map.close_paren.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append(']');
	}
}
