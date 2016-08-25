package dyvil.tools.compiler.ast.type.compound;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Mutability;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.raw.IObjectType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class MapType implements IObjectType
{
	public static final class MapTypes
	{
		public static final IClass MAP_CLASS             = Package.dyvilCollection.resolveClass("Map");
		public static final IClass MUTABLE_MAP_CLASS     = Package.dyvilCollection.resolveClass("MutableMap");
		public static final IClass IMMUTABLE_MAP_CLASS   = Package.dyvilCollection.resolveClass("ImmutableMap");
		public static final IClass MAP_CONVERTIBLE_CLASS = Types.LITERALCONVERTIBLE_CLASS
			                                                   .resolveClass(Name.fromRaw("FromMap"));

		public static final ITypeParameter KEY_VARIABLE   = MapTypes.MAP_CLASS.getTypeParameter(0);
		public static final ITypeParameter VALUE_VARIABLE = MapTypes.MAP_CLASS.getTypeParameter(1);
	}

	protected IType keyType;
	protected IType valueType;

	protected Mutability mutability = Mutability.UNDEFINED;

	// Metadata
	private IClass theClass;

	public static MapType base(IType keyType, IType valueType)
	{
		return new MapType(keyType, valueType, Mutability.UNDEFINED, MapTypes.MAP_CLASS);
	}

	public static MapType mutable(IType keyType, IType valueType)
	{
		return new MapType(keyType, valueType, Mutability.MUTABLE, MapTypes.MUTABLE_MAP_CLASS);
	}

	public static MapType immutable(IType keyType, IType valueType)
	{
		return new MapType(keyType, valueType, Mutability.IMMUTABLE, MapTypes.IMMUTABLE_MAP_CLASS);
	}

	public MapType()
	{
	}

	public MapType(IType keyType, IType valueType, Mutability mutability)
	{
		this.keyType = keyType;
		this.mutability = mutability;
		this.valueType = valueType;
	}

	protected MapType(IType keyType, IType valueType, Mutability mutability, IClass theClass)
	{
		this.keyType = keyType;
		this.valueType = valueType;
		this.mutability = mutability;
		this.theClass = theClass;
	}

	@Override
	public int typeTag()
	{
		return MAP;
	}

	@Override
	public ICodePosition getPosition()
	{
		final ICodePosition keyPos = this.keyType.getPosition();
		final ICodePosition valuePos = this.valueType.getPosition();

		return keyPos != null && valuePos != null ? keyPos.to(valuePos) : null;
	}

	public void setKeyType(IType keyType)
	{
		this.keyType = keyType;
	}

	public IType getKeyType()
	{
		return this.keyType;
	}

	public void setValueType(IType valueType)
	{
		this.valueType = valueType;
	}

	public IType getValueType()
	{
		return this.valueType;
	}

	@Override
	public Name getName()
	{
		return this.theClass.getName();
	}

	@Override
	public boolean isGenericType()
	{
		return true;
	}

	@Override
	public IClass getTheClass()
	{
		return this.theClass;
	}

	@Override
	public IType asParameterType()
	{
		return new MapType(this.keyType.asParameterType(), this.valueType.asParameterType(), this.mutability,
		                   this.theClass);
	}

	@Override
	public Mutability getMutability()
	{
		return this.mutability;
	}

	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		if (typeParameter.getGeneric() == this.theClass)
		{
			if (typeParameter.getIndex() == 0)
			{
				return this.keyType;
			}
			else
			{
				return this.valueType;
			}
		}
		return this.theClass.resolveType(typeParameter, this);
	}

	@Override
	public boolean hasTypeVariables()
	{
		return this.keyType.hasTypeVariables() || this.valueType.hasTypeVariables();
	}

	@Override
	public IType getConcreteType(ITypeContext context)
	{
		final IType newKeyType = this.keyType.getConcreteType(context);
		final IType newValueType = this.valueType.getConcreteType(context);
		if (newKeyType != this.keyType || newValueType != this.valueType)
		{
			return new MapType(newKeyType, newValueType, this.mutability, this.theClass);
		}
		return this;
	}

	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		this.keyType.inferTypes(concrete.resolveType(this.theClass.getTypeParameter(0)), typeContext);
		this.valueType.inferTypes(concrete.resolveType(this.theClass.getTypeParameter(1)), typeContext);
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
	public boolean isResolved()
	{
		return this.theClass != null;
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		this.theClass = getClass(this.mutability);

		this.keyType = this.keyType.resolveType(markers, context);
		this.valueType = this.valueType.resolveType(markers, context);
		return this;
	}

	@Override
	public void checkType(MarkerList markers, IContext context, TypePosition position)
	{
		final TypePosition argumentPosition =
			position == TypePosition.SUPER_TYPE ? TypePosition.SUPER_TYPE_ARGUMENT : TypePosition.GENERIC_ARGUMENT;

		this.keyType.checkType(markers, context, argumentPosition);
		this.valueType.checkType(markers, context, argumentPosition);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.keyType.check(markers, context);
		this.valueType.check(markers, context);
	}

	@Override
	public void foldConstants()
	{
		this.keyType.foldConstants();
		this.valueType.foldConstants();
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.keyType.cleanup(context, compilableList);
		this.valueType.cleanup(context, compilableList);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
		this.theClass.getMethodMatches(list, receiver, name, arguments);
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		this.theClass.getImplicitMatches(list, value, targetType);
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, IArguments arguments)
	{
	}

	@Override
	public IMethod getFunctionalMethod()
	{
		return null;
	}

	@Override
	public String getInternalName()
	{
		return this.theClass.getInternalName();
	}

	@Override
	public void appendSignature(StringBuilder buffer, boolean genericArg)
	{
		buffer.append('L').append(this.theClass.getInternalName()).append('<');
		this.keyType.appendSignature(buffer, true);
		this.valueType.appendSignature(buffer, true);
		buffer.append('>').append(';');
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		this.keyType.writeTypeExpression(writer);
		this.valueType.writeTypeExpression(writer);
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvilx/lang/model/type/MapType", "apply",
		                       "(Ldyvilx/lang/model/type/Type;Ldyvilx/lang/model/type/Type;)Ldyvilx/lang/model/type/MapType;",
		                       false);
	}

	@Override
	public void addAnnotation(IAnnotation annotation, TypePath typePath, int step, int steps)
	{
		if (step >= steps || typePath.getStep(step) != TypePath.TYPE_ARGUMENT)
		{
			return;
		}

		if (typePath.getStepArgument(step) == 0)
		{
			this.keyType = IType.withAnnotation(this.keyType, annotation, typePath, step + 1, steps);
		}
		else
		{
			this.valueType = IType.withAnnotation(this.valueType, annotation, typePath, step + 1, steps);
		}
	}

	@Override
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		this.keyType.writeAnnotations(visitor, typeRef, typePath + "0;");
		this.valueType.writeAnnotations(visitor, typeRef, typePath + "1;");
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		IType.writeType(this.keyType, out);
		IType.writeType(this.valueType, out);
		this.mutability.write(out);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.keyType = IType.readType(in);
		this.valueType = IType.readType(in);
		this.mutability = Mutability.read(in);
		this.theClass = getClass(this.mutability);
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder().append('[');
		this.mutability.appendKeyword(builder);
		builder.append(this.keyType);
		builder.append(':');
		builder.append(this.valueType);
		builder.append(']');
		return builder.toString();
	}

	@Override
	public void toString(String prefix, StringBuilder builder)
	{
		builder.append('[');
		this.mutability.appendKeyword(builder);
		this.keyType.toString(prefix, builder);
		builder.append(':');
		this.valueType.toString(prefix, builder);
		builder.append(']');
	}

	@Override
	public IType clone()
	{
		return new MapType(this.keyType, this.valueType, this.mutability, this.theClass);
	}
}
