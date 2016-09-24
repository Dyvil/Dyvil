package dyvil.tools.compiler.ast.type.compound;

import dyvil.reflect.Opcodes;
import dyvil.tools.asm.TypeAnnotatableVisitor;
import dyvil.tools.asm.TypePath;
import dyvil.tools.compiler.ast.access.MethodCall;
import dyvil.tools.compiler.ast.annotation.AnnotationUtil;
import dyvil.tools.compiler.ast.annotation.IAnnotation;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.IConstantValue;
import dyvil.tools.compiler.ast.constructor.IConstructor;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.NilExpr;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.GenericData;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.generic.ITypeParameter;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MatchList;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.parameter.SingleArgument;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.ast.type.raw.IObjectType;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class OptionType implements IObjectType
{
	public static final class LazyFields
	{
		public static final IClass         OPTION_CLASS          = Package.dyvilUtil.resolveClass("Option");
		public static final ITypeParameter OPTION_TYPE_PARAMETER = OPTION_CLASS.getTypeParameter(0);

		public static final IClass SOME_CLASS = Package.dyvilUtil.resolveClass("Some");

		public static final IMethod APPLY_METHOD = SOME_CLASS.getBody().getMethod(Names.apply);

		public static final IMethod GET_METHOD = OPTION_CLASS.getBody().getMethod(Names.get);

		private LazyFields()
		{
			// no instances
		}
	}

	private static class NoneValue extends NilExpr implements IConstantValue
	{
		public static final NoneValue instance = (NoneValue) new NoneValue().withType(new OptionType(Types.ANY),
		                                                                              ITypeContext.DEFAULT, null, null);

		private NoneValue()
		{
		}

		@Override
		public int stringSize()
		{
			return "None".length();
		}

		@Override
		public boolean toStringBuilder(StringBuilder builder)
		{
			builder.append("None");
			return true;
		}
	}

	protected IType type;

	public OptionType()
	{
	}

	public OptionType(IType type)
	{
		this.type = type;
	}

	@Override
	public int typeTag()
	{
		return OPTIONAL;
	}

	@Override
	public boolean isGenericType()
	{
		return true;
	}

	@Override
	public Name getName()
	{
		return this.type.getName();
	}

	@Override
	public IClass getTheClass()
	{
		return LazyFields.OPTION_CLASS;
	}

	@Override
	public boolean isConvertibleFrom(IType type)
	{
		return Types.isSuperType(type, this.type);
	}

	@Override
	public IValue convertValue(IValue value, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		final IType valueType = value.getType();
		if (valueType == Types.UNKNOWN || IObjectType.super.isSuperTypeOf(valueType))
		{
			return value.withType(this, typeContext, markers, context);
		}

		final IValue typedReturnValue = value.withType(this.type, typeContext, markers, context);
		if (typedReturnValue == null)
		{
			return null;
		}

		// Wrap the argument in a call to Some.[T]apply(x)
		final MethodCall methodCall = new MethodCall(value.getPosition(), null, LazyFields.APPLY_METHOD,
		                                             new SingleArgument(value));
		methodCall.setGenericData(new GenericData(LazyFields.APPLY_METHOD, this.type));
		return methodCall;
	}

	@Override
	public IType resolveType(ITypeParameter typeParameter)
	{
		return typeParameter == LazyFields.OPTION_TYPE_PARAMETER ? this.type : null;
	}

	@Override
	public boolean hasTypeVariables()
	{
		return this.type.hasTypeVariables();
	}

	@Override
	public IType getConcreteType(ITypeContext context)
	{
		final IType type = this.type.getConcreteType(context);
		if (type != this.type)
		{
			return new OptionType(type);
		}
		return this;
	}

	@Override
	public void inferTypes(IType concrete, ITypeContext typeContext)
	{
		final IType type = concrete.resolveType(LazyFields.OPTION_TYPE_PARAMETER);
		this.type.inferTypes(type, typeContext);
	}

	@Override
	public boolean isResolved()
	{
		return true;
	}

	@Override
	public IType resolveType(MarkerList markers, IContext context)
	{
		this.type = this.type.resolveType(markers, context);
		return this;
	}

	@Override
	public void checkType(MarkerList markers, IContext context, int position)
	{
		this.type.checkType(markers, context, position);
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		this.type.check(markers, context);
	}

	@Override
	public void foldConstants()
	{
		this.type.foldConstants();
	}

	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		this.type.cleanup(context, compilableList);
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		return null;
	}

	@Override
	public void getMethodMatches(MatchList<IMethod> list, IValue receiver, Name name, IArguments arguments)
	{
		LazyFields.OPTION_CLASS.getMethodMatches(list, receiver, name, arguments);
	}

	@Override
	public void getImplicitMatches(MatchList<IMethod> list, IValue value, IType targetType)
	{
		LazyFields.OPTION_CLASS.getImplicitMatches(list, value, targetType);
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
		return "dyvil/util/Option";
	}

	@Override
	public void appendExtendedName(StringBuilder buffer)
	{
		buffer.append("Ldyvil/util/Option;");
	}

	@Override
	public void appendSignature(StringBuilder buffer, boolean genericArg)
	{
		buffer.append("Ldyvil/util/Option<");
		this.type.appendSignature(buffer, true);
		buffer.append(">;");
	}

	@Override
	public void writeTypeExpression(MethodWriter writer) throws BytecodeException
	{
		this.type.writeTypeExpression(writer);
		writer.visitMethodInsn(Opcodes.INVOKESTATIC, "dyvilx/lang/model/type/OptionType", "apply",
		                       "(Ldyvilx/lang/model/type/Type;)Ldyvilx/lang/model/type/OptionType;", false);
	}

	@Override
	public void writeDefaultValue(MethodWriter writer) throws BytecodeException
	{
		writer.visitFieldInsn(Opcodes.INVOKESTATIC, "dyvil/util/Option", "apply", "()Ldyvil/util/Option;");
	}

	@Override
	public IType withAnnotation(IAnnotation annotation)
	{
		if (AnnotationUtil.IMPLICITLY_UNWRAPPED_INTERNAL.equals(annotation.getType().getInternalName()))
		{
			return new ImplicitOptionType(this.type);
		}
		return null;
	}

	@Override
	public void addAnnotation(IAnnotation annotation, TypePath typePath, int step, int steps)
	{
		if (typePath.getStep(step) != TypePath.TYPE_ARGUMENT)
		{
			return;
		}

		if (typePath.getStepArgument(step) == 0)
		{
			this.type = IType.withAnnotation(this.type, annotation, typePath, step + 1, steps);
		}
	}

	@Override
	public void writeAnnotations(TypeAnnotatableVisitor visitor, int typeRef, String typePath)
	{
		this.type.writeAnnotations(visitor, typeRef, typePath + "0;");
	}

	@Override
	public IConstantValue getDefaultValue()
	{
		return NoneValue.instance;
	}

	@Override
	public void write(DataOutput out) throws IOException
	{
		IType.writeType(this.type, out);
	}

	@Override
	public void read(DataInput in) throws IOException
	{
		this.type = IType.readType(in);
	}

	@Override
	public String toString()
	{
		return this.type.toString() + '?';
	}

	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString(prefix, buffer);
		buffer.append('?');
	}

	@Override
	public IType clone()
	{
		return new OptionType(this.type);
	}
}
