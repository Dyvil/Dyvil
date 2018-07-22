package dyvilx.tools.compiler.ast.field.capture;

import dyvil.lang.Name;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.expression.access.FieldAccess;
import dyvilx.tools.compiler.ast.field.IVariable;
import dyvilx.tools.compiler.ast.method.ICallableMember;
import dyvilx.tools.compiler.ast.parameter.IParameter;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.method.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.parsing.marker.MarkerList;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.function.Function;

public class CaptureParameter extends CaptureVariable implements IParameter
{
	protected ICallableMember method;

	protected int index;

	public CaptureParameter(ICallableMember method, IVariable variable)
	{
		super(variable);
		this.method = method;
	}

	public static Function<? super IVariable, ? extends CaptureParameter> factory(ICallableMember method)
	{
		return v -> {
			final CaptureParameter parameter = new CaptureParameter(method, v);
			method.getParameters().add(parameter);
			return parameter;
		};
	}

	@Override
	public Name getLabel()
	{
		return Name.fromQualified("capture_" + this.variable.getInternalName());
	}

	@Override
	public void setLabel(Name name)
	{
	}

	@Override
	public String getQualifiedLabel()
	{
		return null;
	}

	@Override
	public IType getCovariantType()
	{
		return this.variable.getType();
	}

	@Override
	public ICallableMember getMethod()
	{
		return this.method;
	}

	@Override
	public void setMethod(ICallableMember method)
	{
		this.method = method;
	}

	@Override
	public int getIndex()
	{
		return this.index;
	}

	@Override
	public void setIndex(int index)
	{
		this.index = index;
	}

	@Override
	public boolean isLocal()
	{
		return true;
	}

	@Override
	public boolean isDefault()
	{
		return true;
	}

	@Override
	public IValue getDefaultValue(IContext context)
	{
		final IValue access = new FieldAccess(this.variable)
		{
			@Override
			public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
			{
				this.field.writeGetRaw(writer, this.receiver, this.lineNumber());
			}
		}.resolve(MarkerList.BLACKHOLE, context);
		access.checkTypes(MarkerList.BLACKHOLE, context); // ensures proper capture
		return access;
	}

	@Override
	public void writeSignature(DataOutput out)
	{
	}

	@Override
	public void readSignature(DataInput in)
	{
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
	}
}
