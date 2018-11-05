package dyvilx.tools.compiler.ast.parameter;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.ArrayExpr;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.member.MemberKind;
import dyvilx.tools.compiler.ast.method.ICallableMember;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.ast.type.compound.ArrayType;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.Marker;
import dyvilx.tools.parsing.marker.MarkerList;

public class CodeParameter extends AbstractParameter
{
	public CodeParameter()
	{
	}

	public CodeParameter(Name name)
	{
		super(name);
	}

	public CodeParameter(Name name, IType type)
	{
		super(name, type);
	}

	public CodeParameter(ICallableMember callable, SourcePosition position, Name name, IType type)
	{
		super(callable, position, name, type);
	}

	public CodeParameter(ICallableMember callable, SourcePosition position, Name name, IType type,
		AttributeList attributes)
	{
		super(callable, position, name, type, attributes);
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);

		if (this.type == Types.UNKNOWN)
		{
			markers.add(Markers.semanticError(this.position, "parameter.type.infer", this.name));
		}

		if (this.value == null)
		{
			return;
		}

		this.attributes.addFlag(Modifiers.DEFAULT);

		final IValue value = this.value.resolve(markers, context);

		final String kindName = this.getKind().getName();

		this.value = TypeChecker.convertValue(value, this.type, null, markers, context, TypeChecker.markerSupplier(
			kindName + ".type.incompatible", kindName + ".type", "value.type", this.name));
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.type != null)
		{
			// for final methods and constructors, covariant types in parameter types are ok,
			// because the method or constructor cannot be overridden
			final boolean allowCovariant =
				this.method != null && (this.method.getKind() == MemberKind.CONSTRUCTOR || this.method.hasModifier(
					Modifiers.FINAL));
			final int position = allowCovariant ?
				                     IType.TypePosition.SUPER_TYPE_ARGUMENT :
				                     IType.TypePosition.PARAMETER_TYPE;
			this.type.checkType(markers, context, position);
		}

		this.attributes.checkTypes(markers, context);

		if (this.value != null)
		{
			this.value.checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, context);

		if (this.value != null)
		{
			this.value.check(markers, context);
		}

		if (Types.isVoid(this.type))
		{
			markers.add(Markers.semanticError(this.position, "parameter.type.void"));
		}

		if (this.isVarargs() && !this.type.canExtract(ArrayType.class)
		    && this.type.getAnnotation(ArrayExpr.LazyFields.ARRAY_CONVERTIBLE) == null)
		{
			final Marker marker = Markers.semanticError(this.type.getPosition(), "parameter.varargs.incompatible",
			                                            this.name);
			marker.addInfo(Markers.getSemantic("parameter.type", this.type));
			markers.add(marker);
		}
	}

	@Override
	public void foldConstants()
	{
		super.foldConstants();

		if (this.value != null)
		{
			this.value = this.value.foldConstants();
		}
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		super.cleanup(compilableList, classCompilableList);

		if (this.value != null)
		{
			this.value = this.value.cleanup(compilableList, classCompilableList);

			classCompilableList.addClassCompilable(this);
		}
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		this.writeDefaultValue(writer);
	}
}
