package dyvilx.tools.compiler.ast.type.compound;

import dyvil.reflect.Opcodes;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.generic.ITypeParameter;
import dyvilx.tools.compiler.ast.generic.TypeParameterList;
import dyvilx.tools.compiler.ast.structure.Package;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.Mutability;
import dyvilx.tools.compiler.ast.type.TypeList;
import dyvilx.tools.compiler.ast.type.generic.GenericType;
import dyvilx.tools.compiler.ast.type.generic.ResolvedGenericType;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;
import dyvil.source.position.SourcePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class MapType extends ResolvedGenericType
{
	public static final class MapTypes
	{
		public static final IClass MAP_CLASS           = Package.dyvilCollection.resolveClass("Map");
		public static final IClass MUTABLE_MAP_CLASS   = Package.dyvilCollection.resolveClass("MutableMap");
		public static final IClass IMMUTABLE_MAP_CLASS = Package.dyvilCollection.resolveClass("ImmutableMap");

		public static final ITypeParameter KEY_VARIABLE;
		public static final ITypeParameter VALUE_VARIABLE;

		static
		{
			final TypeParameterList typeParams = MAP_CLASS.getTypeParameters();
			KEY_VARIABLE = typeParams.get(0);
			VALUE_VARIABLE = typeParams.get(1);
		}
	}

	protected Mutability mutability;

	public static MapType base(IType keyType, IType valueType)
	{
		return new MapType(Mutability.UNDEFINED, keyType, valueType);
	}

	public static MapType mutable(IType keyType, IType valueType)
	{
		return new MapType(Mutability.MUTABLE, keyType, valueType);
	}

	public static MapType immutable(IType keyType, IType valueType)
	{
		return new MapType(Mutability.IMMUTABLE, keyType, valueType);
	}

	public MapType()
	{
		super(null);
		this.setMutability(Mutability.UNDEFINED);
	}

	public MapType(Mutability mutability, IType keyType)
	{
		super(null, null, keyType);
		this.setMutability(mutability);
	}

	public MapType(Mutability mutability, IType keyType, IType valueType)
	{
		super(null, null, keyType, valueType);
		this.setMutability(mutability);
	}

	public MapType(SourcePosition position, Mutability mutability, TypeList arguments)
	{
		super(position, null, arguments);
		this.setMutability(mutability);
	}

	@Override
	public int typeTag()
	{
		return MAP;
	}

	@Override
	public IClass getTheClass()
	{
		if (this.theClass != null)
		{
			return this.theClass;
		}
		return this.theClass = getClass(this.mutability);
	}

	public IType getKeyType()
	{
		return this.arguments.get(0);
	}

	public IType getValueType()
	{
		return this.arguments.get(1);
	}

	@Override
	public Mutability getMutability()
	{
		return this.mutability;
	}

	public void setMutability(Mutability mutability)
	{
		this.mutability = mutability;
		this.theClass = null; // clear class cache
	}

	private static IClass getClass(Mutability mutability)
	{
		if (mutability == Mutability.IMMUTABLE)
		{
			return MapTypes.IMMUTABLE_MAP_CLASS;
		}
		if (mutability == Mutability.MUTABLE)
		{
			return MapTypes.MUTABLE_MAP_CLASS;
		}
		return MapTypes.MAP_CLASS;
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		this.theClass = getClass(this.mutability);

		this.arguments.resolveTypes(markers, context);
		return this;
	}

	@Override
	public void checkType(MarkerList markers, IContext context, int position)
	{
		this.arguments.checkTypes(markers, context, TypePosition.genericArgument(position));
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		this.getKeyType().writeTypeExpression(writer);
		this.getValueType().writeTypeExpression(writer);
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/types/MapType", "apply",
		                       "(Ldyvil/reflect/types/Type;Ldyvil/reflect/types/Type;)Ldyvil/reflect/types/MapType;",
		                       false);
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		IType.writeType(this.getKeyType(), out);
		IType.writeType(this.getValueType(), out);
		this.mutability.write(out);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.arguments.set(0, IType.readType(in));
		this.arguments.set(1, IType.readType(in));
		this.mutability = Mutability.read(in);
	}

	@Override
	protected GenericType withArguments(TypeList arguments)
	{
		return new MapType(this.position, this.mutability, arguments);
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder().append('[');
		this.mutability.appendKeyword(builder);
		builder.append(this.getKeyType());
		builder.append(':');
		builder.append(this.getValueType());
		builder.append(']');
		return builder.toString();
	}

	@Override
	public void toString(String prefix, StringBuilder builder)
	{
		builder.append('[');
		this.mutability.appendKeyword(builder);
		this.getKeyType().toString(prefix, builder);
		builder.append(':');
		this.getValueType().toString(prefix, builder);
		builder.append(']');
	}
}
