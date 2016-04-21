package dyvil.tools.compiler.ast.parameter;

import dyvil.tools.asm.Label;
import dyvil.tools.compiler.ast.annotation.AnnotationList;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.builtin.Types;
import dyvil.tools.compiler.backend.MethodWriter;
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

	public CodeParameter(ICodePosition position, Name name, IType type)
	{
		super(position, name, type);
	}

	public CodeParameter(ICodePosition position, Name name, IType type, ModifierSet modifiers, AnnotationList annotations)
	{
		super(position, name, type, modifiers, annotations);
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
		super.checkTypes(markers, context);

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

		if (Types.isSameType(this.type, Types.VOID))
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
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		super.cleanup(context, compilableList);

		if (this.defaultValue != null)
		{
			this.defaultValue = this.defaultValue.cleanup(context, compilableList);
		}
	}

	@Override
	public void writeLocal(MethodWriter writer, Label start, Label end)
	{
		writer.visitLocalVariable(this.name.qualified, this.getDescriptor(), this.getSignature(), start, end,
		                          this.localIndex);
	}
}
