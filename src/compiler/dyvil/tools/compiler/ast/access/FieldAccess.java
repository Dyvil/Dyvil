package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.constant.EnumValue;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.EmptyArguments;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.ast.IASTNode;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class FieldAccess implements IValue, INamed, IReceiverAccess
{
	protected ICodePosition position;
	protected IValue        receiver;
	protected Name          name;
	
	protected boolean dotless;
	
	// Metadata
	protected IDataMember field;
	protected IType       type;
	
	public FieldAccess()
	{
	}
	
	public FieldAccess(ICodePosition position)
	{
		this.position = position;
	}
	
	public FieldAccess(ICodePosition position, IValue instance, Name name)
	{
		this.position = position;
		this.receiver = instance;
		this.name = name;
	}
	
	public FieldAccess(ICodePosition position, IValue instance, IDataMember field)
	{
		this.position = position;
		this.receiver = instance;
		this.field = field;
		this.name = field.getName();
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
	
	public MethodCall toMethodCall(IMethod method)
	{
		MethodCall call = new MethodCall(this.position);
		call.receiver = this.receiver;
		call.name = this.name;
		call.method = method;
		call.dotless = this.dotless;
		call.arguments = EmptyArguments.INSTANCE;
		return call;
	}
	
	@Override
	public int valueTag()
	{
		return FIELD_ACCESS;
	}
	
	public IValue getInstance()
	{
		return this.receiver;
	}
	
	public IDataMember getField()
	{
		return this.field;
	}
	
	public boolean isDotless()
	{
		return this.dotless;
	}
	
	public void setDotless(boolean dotless)
	{
		this.dotless = dotless;
	}
	
	@Override
	public boolean isConstantOrField()
	{
		return this.field != null && this.field.hasModifier(Modifiers.CONST);
	}

	@Override
	public boolean hasSideEffects()
	{
		return this.receiver.hasSideEffects();
	}

	@Override
	public boolean isResolved()
	{
		return this.field != null;
	}
	
	@Override
	public IType getType()
	{
		if (this.type == null)
		{
			if (this.field == null)
			{
				return Types.UNKNOWN;
			}
			if (this.receiver == null)
			{
				return this.type = this.field.getType();
			}
			return this.type = this.field.getType().getConcreteType(this.receiver.getType()).getReturnType();
		}
		return this.type;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		if (this.field == null)
		{
			return this; // dont create an extra type error
		}
		
		return type.isSuperTypeOf(this.getType()) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return this.field != null && type.isSuperTypeOf(this.getType());
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		if (this.field == null)
		{
			return 0;
		}
		
		return type.getSubTypeDistance(this.getType());
	}

	public void setName(Name name)
	{
		this.name = name;
	}
	
	@Override
	public Name getName()
	{
		return this.name;
	}
	
	@Override
	public void setReceiver(IValue value)
	{
		this.receiver = value;
	}
	
	@Override
	public IValue getReceiver()
	{
		return this.receiver;
	}
	
	@Override
	public IValue toConstant(MarkerList markers)
	{
		if (this.field == null)
		{
			return this; // do not create an extra error
		}
		
		int depth = DyvilCompiler.maxConstantDepth;
		IValue v = this;
		
		do
		{
			if (depth-- < 0)
			{
				markers.add(I18n.createMarker(this.getPosition(), "annotation.field.not_constant", this.name));
				return this;
			}
			
			v = v.foldConstants();
		}
		while (!v.isConstantOrField());
		
		return v.toConstant(markers);
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.resolveTypes(markers, context);
		}
	}
	
	@Override
	public void resolveReceiver(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver = this.receiver.resolve(markers, context);
		}
	}

	@Override
	public IValue resolve(MarkerList markers, IContext context)
	{
		this.resolveReceiver(markers, context);

		IValue v = this.resolveFieldAccess(markers, context);
		if (v != null)
		{
			return v;
		}

		// Don't report an error if the receiver is not resolved
		if (this.receiver != null && !this.receiver.isResolved())
		{
			return this;
		}

		Marker marker = I18n.createMarker(this.position, "resolve.method_field", this.name.unqualified);
		marker.addInfo(I18n.getString("name.qualified", this.name.qualified));
		if (this.receiver != null)
		{
			marker.addInfo(I18n.getString("receiver.type", this.receiver.getType()));
		}
		
		markers.add(marker);
		return this;
	}

	protected IValue resolveFieldAccess(MarkerList markers, IContext context)
	{
		if (ICall.privateAccess(context, this.receiver))
		{
			IValue value = this.resolveField(markers, context);
			if (value != null)
			{
				return value;
			}
			value = this.resolveMethod(markers, context);
			if (value != null)
			{
				return value;
			}
		}
		else
		{
			IValue value = this.resolveMethod(markers, context);
			if (value != null)
			{
				return value;
			}
			value = this.resolveField(markers, context);
			if (value != null)
			{
				return value;
			}
		}
		
		if (this.receiver == null)
		{
			IClass iclass = IContext.resolveClass(context, this.name);
			if (iclass != null)
			{
				return new ClassAccess(this.position, iclass.getType());
			}
		}
		
		return null;
	}

	private IValue resolveField(MarkerList markers, IContext context)
	{
		IDataMember field = ICall.resolveField(context, this.receiver, this.name);
		if (field != null)
		{
			if (field.isEnumConstant())
			{
				EnumValue enumValue = new EnumValue(field.getType(), this.name);
				return enumValue;
			}
			
			this.field = field;
			return this;
		}
		return null;
	}
	
	private IValue resolveMethod(MarkerList markers, IContext context)
	{
		IMethod method = ICall.resolveMethod(context, this.receiver, this.name, EmptyArguments.INSTANCE);
		if (method != null)
		{
			AbstractCall mc = this.toMethodCall(method);
			mc.checkArguments(markers, context);
			return mc;
		}
		return null;
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.checkTypes(markers, context);
		}
		
		if (this.field != null)
		{
			this.field = this.field.capture(context);
			this.receiver = this.field.checkAccess(markers, this.position, this.receiver, context);
		}
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.check(markers, context);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		if (this.field != null && this.field.hasModifier(Modifiers.CONST))
		{
			IValue v = this.field.getValue();
			return v != null && v.isConstantOrField() ? v : this;
		}
		if (this.receiver != null)
		{
			this.receiver = this.receiver.foldConstants();
		}
		return this;
	}
	
	@Override
	public IValue cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (this.receiver != null)
		{
			this.receiver = this.receiver.cleanup(context, compilableList);
		}
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		int lineNumber = this.getLineNumber();
		this.field.writeGet(writer, this.receiver, lineNumber);

		if (type == null)
		{
			type = this.type;
		}
		if (type == Types.VOID)
		{
			writer.writeInsn(this.type.getReturnOpcode());
		}
		if (type != null)
		{
			this.field.getType().writeCast(writer, type, lineNumber);
		}
	}
	
	@Override
	public String toString()
	{
		return IASTNode.toString(this);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.receiver != null)
		{
			this.receiver.toString("", buffer);
			if (this.dotless && !Formatting.getBoolean("field.access.java_format"))
			{
				buffer.append(' ');
			}
			else
			{
				buffer.append('.');
			}
		}
		
		buffer.append(this.name);
	}
}
