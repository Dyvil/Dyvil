package dyvilx.tools.compiler.ast.method;

import dyvil.lang.Name;
import dyvil.reflect.Modifiers;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.field.IDataMember;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.field.capture.CaptureHelper;
import dyvilx.tools.compiler.ast.field.capture.CaptureParameter;
import dyvilx.tools.compiler.ast.generic.ITypeContext;
import dyvilx.tools.compiler.ast.parameter.ArgumentList;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

public class NestedMethod extends CodeMethod
{
	private CaptureHelper<CaptureParameter> captureHelper = new CaptureHelper<>(CaptureParameter.factory(this));

	public NestedMethod(SourcePosition position, Name name, IType type, AttributeList attributes)
	{
		super(position, name, type, attributes);
	}

	@Override
	public boolean isNested()
	{
		return true;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (!context.isThisAvailable())
		{
			this.attributes.addFlag(Modifiers.STATIC);
		}

		super.resolveTypes(markers, context);
	}

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

	@Override
	public IDataMember capture(IVariable variable)
	{
		if (this.isMember(variable))
		{
			return variable;
		}

		return this.captureHelper.capture(variable);
	}
}
