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
import dyvil.tools.compiler.ast.generic.Variance;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.ArgumentList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.ITyped;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.raw.IRawType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class WildcardType implements IRawType, ITyped
{
	public ICodePosition position;
	protected IType bound = Types.NULLABLE_ANY;
	protected Variance variance;

	public WildcardType()
	{
	}

	public WildcardType(Variance variance)
	{
		this.variance = variance;
	}

	public WildcardType(Variance variance, IType bound)
	{
		this.variance = variance;
		this.bound = bound;
	}

	public WildcardType(ICodePosition position, Variance variance)
	{
		this.position = position;
		this.variance = variance;
	}

	public WildcardType(ICodePosition position, IType bound, Variance variance)
	{
		this.position = position;
		this.bound = bound;
		this.variance = variance;
	}

	public Variance getVariance()
	{
		return this.variance;
	}

	public void setVariance(Variance variance)
	{
		this.variance = variance;
	}

	@Override
	public IType getType()
	{
		return this.bound;
	}

	@Override
	public void setType(IType type)
	{
		this.bound = type;
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
	public IType atPosition(ICodePosition position)
	{
		return new WildcardType(position, this.bound, this.variance);
	}

	@Override
	public int typeTag()
	{
		return WILDCARD_TYPE;
	}

	@Override
	public boolean isGenericType()
	{
		return false;
	}

	@Override
	public boolean useNonNullAnnotation()
	{
		return false;
	}

	@Override
	public Name getName()
	{
		return null;
	}

	@Override
	public IClass getTheClass()
	{
		return this.bound.getTheClass();
	}

	@Override
	public boolean hasTypeVariables()
	{
		return this.bound.hasTypeVariables();
	}

	@Override
	public IType asReturnType()
	{
		if (this.variance == Variance.CONTRAVARIANT)
		{
			return Types.ANY;
		}
		return this.bound;
	}

	@Override
	public IType asParameterType()
	{
		if (this.bound == null)
		{
			return this;
		}

		final IType bound = this.bound.asParameterType();
		if (this.variance == Variance.CONTRAVARIANT)
		{
			return bound;
		}

		return bound == this.bound ? this : new WildcardType(this.variance, bound);
	}

	@Override
	public IType getConcreteType(ITypeContext context)
	{
		if (context == ITypeContext.COVARIANT)
		{
			return this.asParameterType();
		}

		final IType concreteBound = this.bound.getConcreteType(context);
		if (concreteBound == this.bound)
		{
			return this;
		}

		if (concreteBound.typeTag() == WILDCARD_TYPE)
		{
			return concreteBound;
		}

		final WildcardType copy = new WildcardType(this.position, this.variance);
		copy.bound = concreteBound;
		return copy;
	}

	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		return this.bound.resolveType(typeParameter);
	}

	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		this.bound.inferTypes(concrete, typeContext);
	}

	@Override
	public int subTypeCheckLevel()
	{
		return this.bound.subTypeCheckLevel();
	}

	@Override
	public boolean isSameType(IType type)
	{
		return this.bound.isSameType(type);
	}

	@Override
	public boolean isSuperClassOf(IType subType)
	{
		return this.bound.isSuperClassOf(subType);
	}

	@Override
	public boolean isSuperTypeOf(IType subType)
	{
		return this.bound.isSuperTypeOf(subType);
	}

	@Override
	public boolean isSubClassOf(IType superType)
	{
		return this.bound.isSubClassOf(superType);
	}

	@Override
	public boolean isSubTypeOf(IType superType)
	{
		return this.bound.isSubTypeOf(superType);
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		this.bound = this.bound.resolveType(markers, context);

		return this;
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		this.bound.resolve(markers, context);
	}

	@Override
	public void checkType(MarkerList markers, IContext context, int position)
	{
		if ((position & TypePosition.WILDCARD_FLAG) == 0)
		{
			markers.add(Markers.semantic(this.position, "type.wildcard.invalid"));
		}

		this.bound.checkType(markers, context, TypePosition.SUPER_TYPE_ARGUMENT);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.bound.check(markers, context);
	}

	@Override
	public void foldConstants()
	{
		this.bound.foldConstants();
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.bound.cleanup(compilableList, classCompilableList);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		if (this.variance != Variance.CONTRAVARIANT)
		{
			return this.bound.resolveField(name);
		}

		return null;
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, ArgumentList arguments)
	{
		if (this.variance != Variance.CONTRAVARIANT)
		{
			this.bound.getMethodMatches(list, receiver, name, arguments);
		}
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		if (this.variance != Variance.CONTRAVARIANT)
		{
			this.bound.getImplicitMatches(list, value, targetType);
		}
	}

	@Override
	public void getConstructorMatches(MatchList<IConstructor> list, ArgumentList arguments)
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
		if (this.variance == Variance.CONTRAVARIANT)
		{
			return "java/lang/Object";
		}
		return this.bound.getInternalName();
	}

	@Override
	public void appendDescriptor(StringBuilder buffer, int type)
	{
		if (type == NAME_DESCRIPTOR)
		{
			buffer.append('L').append(this.getInternalName()).append(';');
			return;
		}

		if (!Types.isSameType(this.bound, Types.OBJECT))
		{
			this.variance.appendPrefix(buffer);
			this.bound.appendSignature(buffer, true);
		}
		else
		{
			buffer.append('*');
		}
	}

	@Override
	public int getLoadOpcode()
	{
		return Opcodes.ALOAD;
	}

	@Override
	public int getArrayLoadOpcode()
	{
		return Opcodes.AALOAD;
	}

	@Override
	public int getStoreOpcode()
	{
		return Opcodes.ASTORE;
	}

	@Override
	public int getArrayStoreOpcode()
	{
		return Opcodes.AASTORE;
	}

	@Override
	public int getReturnOpcode()
	{
		return Opcodes.ARETURN;
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		writer.visitFieldInsn(Opcodes.GETSTATIC, "dyvil/reflect/Variance", this.variance.name(),
		                      "Ldyvil/reflect/Variance;");

		if (this.bound != null)
		{
			this.bound.writeTypeExpression(writer);
		}
		else
		{
			writer.visitInsn(Opcodes.ACONST_NULL);
		}

		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvil/reflect/types/WildcardType", "apply",
		                       "(Ldyvil/reflect/Variance;Ldyvil/reflect/types/Type;)Ldyvil/reflect/types/WildcardType;",
		                       false);
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		Variance.write(this.variance, out);
		IType.writeType(this.bound, out);
	}

	@Override
	public IType withAnnotation(IAnnotation annotation)
	{
		final IType a = this.bound.withAnnotation(annotation);
		if (a == null)
		{
			return null;
		}

		this.bound = a;
		return this;
	}

	@Override
	public void addAnnotation(IAnnotation annotation, TypePath typePath, int step, int steps)
	{
		if (typePath.getStep(step) == TypePath.WILDCARD_BOUND)
		{
			this.bound = IType.withAnnotation(this.bound, annotation, typePath, step + 1, steps);
		}
	}

	@Override
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		this.bound.writeAnnotations(visitor, typeRef, typePath + '*');
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.variance = Variance.read(in);
		this.bound = IType.readType(in);
	}

	@Override
	public String toString()
	{
		if (this.bound == null)
		{
			return "_";
		}

		final StringBuilder builder = new StringBuilder();
		this.variance.appendPrefix(builder);
		builder.append(this.bound);
		return builder.toString();
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.bound != null)
		{
			this.variance.appendPrefix(buffer);
			this.bound.toString(prefix, buffer);
		}
		else
		{
			buffer.append('_');
		}
	}
}
