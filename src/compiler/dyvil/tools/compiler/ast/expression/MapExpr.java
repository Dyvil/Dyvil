package dyvil.tools.compiler.ast.expression;

import dyvil.annotation.internal.NonNull;
import dyvil.collection.mutable.HashSet;
import dyvil.lang.Formattable;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.context.IImplicitContext;
import dyvil.tools.compiler.ast.expression.access.ClassAccess;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.compound.MapType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

public class MapExpr implements IValue
{
	public static final class LazyTypes
	{
		public static final IClass MAP_CONVERTIBLE_CLASS = dyvil.tools.compiler.ast.type.builtin.Types.LITERALCONVERTIBLE_CLASS
			                                                   .resolveClass(Name.fromRaw("FromMap"));
	}

	private static final TypeChecker.MarkerSupplier KEY_MARKER_SUPPLIER   = TypeChecker.markerSupplier(
		"map.key.type.incompatible", "map.key.type.expected", "map.key.type.actual");
	private static final TypeChecker.MarkerSupplier VALUE_MARKER_SUPPLIER = TypeChecker.markerSupplier(
		"map.value.type.incompatible", "map.value.type.expected", "map.value.type.actual");

	protected SourcePosition position;

	protected @NonNull ArgumentList keys;
	protected @NonNull ArgumentList values;

	// Metadata
	private IType type;
	private IType keyType;
	private IType valueType;

	public MapExpr(SourcePosition position)
	{
		this.position = position;
	}

	public MapExpr(SourcePosition position, ArgumentList keys, ArgumentList values)
	{
		this.position = position;
		this.keys = keys;
		this.values = values;
	}

	@Override
	public int valueTag()
	{
		return MAP;
	}

	@Override
	public void setPosition(SourcePosition position)
	{
		this.position = position;
	}

	@Override
	public SourcePosition getPosition()
	{
		return this.position;
	}

	public IType getKeyType()
	{
		if (this.keyType != null)
		{
			return this.keyType;
		}
		return this.keyType = this.keys.getCommonType();
	}

	public IType getValueType()
	{
		if (this.valueType != null)
		{
			return this.valueType;
		}
		return this.valueType = this.values.getCommonType();
	}

	@Override
	public boolean isResolved()
	{
		if (this.type != null)
		{
			return this.type.isResolved();
		}
		if (this.keyType != null && this.valueType != null)
		{
			return this.keyType.isResolved() && this.valueType.isResolved();
		}
		return this.keys.isResolved() && this.values.isResolved();
	}

	@Override
	public boolean isClassAccess()
	{
		return this.keys.size() == 1 && this.keys.getFirst().isClassAccess() && this.values.getFirst()
		                                                                                   .isClassAccess();
	}

	@Override
	public IValue asIgnoredClassAccess()
	{
		if (!this.isClassAccess())
		{
			return IValue.super.asIgnoredClassAccess();
		}
		return new ClassAccess(this.position,
		                       MapType.base(this.keys.getFirst().getType(), this.values.getFirst().getType()))
			       .asIgnoredClassAccess();
	}

	@Override
	public IType getType()
	{
		if (this.type == null)
		{
			return this.type = MapType.immutable(this.getKeyType(), this.getValueType());
		}
		return this.type;
	}

	@Override
	public void setType(IType type)
	{
		this.type = type;
	}

	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (!Types.isSuperClass(type, MapType.MapTypes.IMMUTABLE_MAP_CLASS.getClassType()))
		{
			final IAnnotation annotation = type.getTheClass().getAnnotation(LazyTypes.MAP_CONVERTIBLE_CLASS);
			if (annotation == null)
			{
				return null;
			}

			// [ x : y, ... ] -> Type(x : y, ...))

			final int size = this.keys.size();
			final ArgumentList arguments = new ArgumentList(size);

			for (int i = 0; i < size; i++)
			{
				arguments.add(new ColonOperator(this.keys.get(i), this.values.get(i)));
			}

			return new LiteralConversion(this, annotation, arguments).withType(type, typeContext, markers, context);
		}

		final int size = this.keys.size();

		final IType keyType = this.keyType = Types.resolveTypeSafely(type, MapType.MapTypes.KEY_VARIABLE);
		final IType valueType = this.valueType = Types.resolveTypeSafely(type, MapType.MapTypes.VALUE_VARIABLE);

		for (int i = 0; i < size; i++)
		{
			final IValue key = TypeChecker.convertValue(this.keys.get(i), keyType, typeContext, markers, context,
			                                            KEY_MARKER_SUPPLIER);
			this.keys.set(i, key);

			final IValue value = TypeChecker.convertValue(this.values.get(i), valueType, typeContext, markers, context,
			                                              VALUE_MARKER_SUPPLIER);
			this.values.set(i, value);
		}

		return this;
	}

	@Override
	public boolean isType(IType type)
	{
		if (!MapType.MapTypes.MAP_CLASS.isSubClassOf(type))
		{
			return this.isConvertibleFrom(type);
		}

		IType keyType = Types.resolveTypeSafely(type, MapType.MapTypes.KEY_VARIABLE);
		IType valueType = Types.resolveTypeSafely(type, MapType.MapTypes.VALUE_VARIABLE);

		return this.keys.isType(keyType) && this.values.isType(valueType);
	}

	private boolean isConvertibleFrom(IType type)
	{
		return type.getAnnotation(LazyTypes.MAP_CONVERTIBLE_CLASS) != null;
	}

	@Override
	public int getTypeMatch(IType type, IImplicitContext implicitContext)
	{
		if (!MapType.MapTypes.MAP_CLASS.isSubClassOf(type))
		{
			return this.isConvertibleFrom(type) ? CONVERSION_MATCH : MISMATCH;
		}

		if (this.keys.isEmpty())
		{
			return EXACT_MATCH;
		}

		final IType keyType = Types.resolveTypeSafely(type, MapType.MapTypes.KEY_VARIABLE);
		final int keyMatch = this.keys.getTypeMatch(keyType, implicitContext);
		if (keyMatch == MISMATCH)
		{
			return MISMATCH;
		}

		final IType valueType = Types.resolveTypeSafely(type, MapType.MapTypes.VALUE_VARIABLE);
		final int valueMatch = this.values.getTypeMatch(valueType, implicitContext);
		return Math.min(keyMatch, valueMatch);
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		this.keys.resolveTypes(markers, context);
		this.values.resolveTypes(markers, context);
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.keys.resolve(markers, context);
		this.values.resolve(markers, context);
		return this;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		this.keys.checkTypes(markers, context);
		this.values.checkTypes(markers, context);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.keys.check(markers, context);
		this.values.check(markers, context);

		final HashSet<Object> keys = new HashSet<>();

		for (IValue key : this.keys)
		{
			if (key.hasSideEffects())
			{
				markers.add(Markers.semantic(key.getPosition(), "map.key.side_effects"));
				continue;
			}

			if (!key.isConstantOrField())
			{
				continue;
			}

			final Object value = key.toObject();
			if (value == null || keys.add(value))
			{
				continue;
			}

			markers.add(Markers.semantic(key.getPosition(), "map.key.duplicate", value));
		}
	}

	@Override
	public IValue foldConstants()
	{
		this.keys.foldConstants();
		this.values.foldConstants();
		return this;
	}

	@Override
	public IValue cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.keys.cleanup(compilableList, classCompilableList);
		this.values.cleanup(compilableList, classCompilableList);
		return this;
	}

	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		final int size = this.keys.size();
		if (size == 0)
		{
			writer.visitFieldInsn(Opcodes.GETSTATIC, "dyvil/collection/immutable/EmptyMap", "instance",
			                      "Ldyvil/collection/immutable/EmptyMap;");
			return;
		}

		final IType keyObject = this.keyType.getObjectType();
		final IType valueObject = this.valueType.getObjectType();
		final int varIndex = writer.localCount();

		writer.visitLdcInsn(size);
		writer.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
		writer.visitVarInsn(Opcodes.ASTORE, varIndex);

		writer.visitLdcInsn(size);
		writer.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
		writer.visitVarInsn(Opcodes.ASTORE, varIndex + 1);

		for (int i = 0; i < size; i++)
		{
			writer.visitVarInsn(Opcodes.ALOAD, varIndex);
			writer.visitLdcInsn(i);
			this.keys.get(i).writeExpression(writer, keyObject);
			writer.visitInsn(Opcodes.AASTORE);

			writer.visitVarInsn(Opcodes.ALOAD, varIndex + 1);
			writer.visitLdcInsn(i);
			this.values.get(i).writeExpression(writer, valueObject);
			writer.visitInsn(Opcodes.AASTORE);
		}

		writer.visitVarInsn(Opcodes.ALOAD, varIndex);
		writer.visitVarInsn(Opcodes.ALOAD, varIndex + 1);
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/collection/ImmutableMap", "apply",
		                       "([Ljava/lang/Object;[Ljava/lang/Object;)Ldyvil/collection/ImmutableMap;", true);

		writer.resetLocals(varIndex);

		if (type != null)
		{
			this.getType().writeCast(writer, type, this.lineNumber());
		}
	}

	@Override
	public String toString()
	{
		return Formattable.toString(this);
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		final int size = this.keys.size();
		if (size == 0)
		{
			buffer.append("[:]");
			return;
		}

		final String newIndent = Formatting.getIndent("map.entry_separator.indent", indent);
		final String keyValueSeparator = Formatting.getSeparator("map.key_value_separator", ':');
		final String entrySeparator = Formatting.getSeparator("map.entry_separator", ',');

		buffer.append('[');
		if (Formatting.getBoolean("map.open_paren.space_after"))
		{
			buffer.append(' ');
		}

		this.keys.getFirst().toString(newIndent, buffer);
		buffer.append(keyValueSeparator);
		this.values.getFirst().toString(newIndent, buffer);

		for (int i = 1; i < size; i++)
		{
			buffer.append(entrySeparator);

			this.keys.get(i).toString(newIndent, buffer);
			buffer.append(keyValueSeparator);
			this.values.get(i).toString(newIndent, buffer);
		}

		if (Formatting.getBoolean("map.close_paren.space_before"))
		{
			buffer.append(' ');
		}
		buffer.append(']');
	}
}
