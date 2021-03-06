package dyvilx.tools.compiler.ast.method;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.attribute.modifiers.ModifierUtil;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.field.capture.CaptureHelper;
import dyvilx.tools.compiler.ast.field.capture.CaptureParameter;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

public class NestedMethod extends CodeMethod
{
	// =============== Fields ===============

	private CaptureHelper<CaptureParameter> captureHelper = new CaptureHelper<>(CaptureParameter.factory(this));

	// =============== Constructors ===============

	public NestedMethod(SourcePosition position, Name name, IType type, AttributeList attributes)
	{
		super(position, name, type, attributes);
	}

	// =============== Properties ===============

	@Override
	public boolean isNested()
	{
		return true;
	}

	// =============== Methods ===============

	// --------------- Context ---------------

	@Override
	public boolean isMember(IVariable variable)
	{
		return super.isMember(variable) || this.captureHelper.isMember(variable);
	}

	@Override
	public IDataMember capture(IVariable variable)
	{
		if (this.isMember(variable))
		{
			return variable;
		}

		return this.captureHelper.capture(variable);
	}

	// --------------- Resolution ---------------

	@Override
	public void checkCall(MarkerList markers, SourcePosition position, IContext context, IValue instance,
		ArgumentList arguments, ITypeContext typeContext)
	{
		if (position != null && this.position != null && position.isBefore(this.position))
		{
			markers.add(Markers.semanticError(position, "method.nested.access.early", this.name));
		}

		super.checkCall(markers, position, context, instance, arguments, typeContext);
	}

	// --------------- Resolution Phases ---------------

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (!context.isThisAvailable())
		{
			this.attributes.addFlag(Modifiers.STATIC);
		}

		super.resolveTypes(markers, context);
	}

	// --------------- Diagnostic Phases ---------------

	@Override
	public void check(MarkerList markers, IContext context)
	{
		final int accessLevel = this.getAccessLevel();
		if (accessLevel != Modifiers.PRIVATE)
		{
			markers.add(Markers.semanticError(this.position, "method.nested.not_private", this.getName(),
			                                  ModifierUtil.accessModifiersToString(accessLevel)));
		}

		super.check(markers, context);
	}

	// --------------- Pre-Compilation Phases ---------------

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		this.setInternalName(this.getInternalName() + "$" + classCompilableList.classCompilableCount());

		super.cleanup(compilableList, classCompilableList);
	}
}
