package dyvil.tools.compiler.ast.parameter;

import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.header.IClassCompilableList;
import dyvil.tools.compiler.ast.header.ICompilableList;
import dyvil.tools.compiler.ast.member.MemberKind;
import dyvil.tools.compiler.ast.method.ICallableMember;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.transform.TypeChecker;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

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

	public CodeParameter(ICallableMember callable, ICodePosition position, Name name, IType type)
	{
		super(callable, position, name, type);
	}

	public CodeParameter(ICallableMember callable, ICodePosition position, Name name, IType type, ModifierSet modifiers,
		                    AnnotationList annotations)
	{
		super(callable, position, name, type, modifiers, annotations);
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);

		if (this.type == Types.UNKNOWN)
		{
			markers.add(Markers.semantic(this.position, this.getKind().getName() + ".type.infer", this.name));
		}

		if (this.defaultValue == null)
		{
			return;
		}

		IValue defaultValue = this.defaultValue.resolve(markers, context);

		final String kindName = this.getKind().getName();
		final TypeChecker.MarkerSupplier markerSupplier = TypeChecker.markerSupplier(kindName + ".type.incompatible",
		                                                                             kindName + ".type", "value.type",
		                                                                             this.name);

		defaultValue = TypeChecker.convertValue(defaultValue, this.type, null, markers, context, markerSupplier);

		this.defaultValue = IValue.toAnnotationConstant(defaultValue, markers, context);
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.type != null)
		{
			final boolean constructorParam = this.method != null && this.method.getKind() == MemberKind.CONSTRUCTOR;
			final int position = constructorParam ?
				                     IType.TypePosition.SUPER_TYPE_ARGUMENT :
				                     IType.TypePosition.PARAMETER_TYPE;
			this.type.checkType(markers, context, position);
		}
		if (this.annotations != null)
		{
			this.annotations.checkTypes(markers, context);
		}

		if (this.defaultValue != null)
		{
			this.defaultValue.checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, context);

		if (this.defaultValue != null)
		{
			this.defaultValue.check(markers, context);
		}

		if (Types.isVoid(this.type))
		{
			markers.add(Markers.semantic(this.position, this.getKind().getName() + ".type.void"));
		}
	}

	@Override
	public void foldConstants()
	{
		super.foldConstants();

		if (this.defaultValue != null)
		{
			this.defaultValue = this.defaultValue.foldConstants();
		}
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		super.cleanup(compilableList, classCompilableList);

		if (this.defaultValue != null)
		{
			this.defaultValue = this.defaultValue.cleanup(compilableList, classCompilableList);
		}
	}
}
