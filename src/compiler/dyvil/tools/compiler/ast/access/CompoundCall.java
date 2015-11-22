package dyvil.tools.compiler.ast.access;

import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.transform.Names;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class CompoundCall extends AbstractCall implements INamed, IValueConsumer
{
	public Name name;
	
	public CompoundCall(ICodePosition position)
	{
		this.position = position;
	}
	
	public CompoundCall(ICodePosition position, IValue instance, Name name)
	{
		this.position = position;
		this.receiver = instance;
		this.name = name;
	}
	
	public CompoundCall(ICodePosition position, IValue instance, Name name, IArguments arguments)
	{
		this.position = position;
		this.receiver = instance;
		this.name = name;
		this.arguments = arguments;
	}
	
	@Override
	public int valueTag()
	{
		return COMPOUND_CALL;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.arguments = this.arguments.withLastValue(value);
	}
	
	@Override
	public IType getType()
	{
		return Types.VOID;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return type == Types.VOID;
	}
	
	@Override
	public IValue withType(IType type, ITypeContext typeContext, MarkerList markers, IContext context)
	{
		return type == Types.VOID ? this : null;
	}
	
	@Override
	public float getTypeMatch(IType type)
	{
		return 0F;
	}
	
	@Override
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
	public IValue resolveCall(MarkerList markers, IContext context)
	{
		int type = this.receiver.valueTag();
		if (type == APPLY_CALL)
		{
			AbstractCall ac = (AbstractCall) this.receiver;
			
			// x(y...) op= z
			// -> x(y...) = x(y...).op(z)
			// -> x.update(y..., x.apply(y...).op(z))
			
			IValue op = new MethodCall(this.position, ac, this.name, this.arguments).resolveCall(markers, context);
			IValue update = new UpdateMethodCall(this.position, ac.receiver,
			                                     ac.arguments.withLastValue(Names.update, op))
					.resolveCall(markers, context);
			return update;
		}
		else if (type == SUBSCRIPT_GET)
		{
			AbstractCall ac = (AbstractCall) this.receiver;
			
			// x[y...] op= z
			// -> x[y...] = x[y...].op(z)
			// -> x.subscript_=(y..., x.subscript(y...).op(z))
			
			IValue op = new MethodCall(this.position, ac, this.name, this.arguments).resolveCall(markers, context);
			IValue subscript_$eq = new SubscriptSetter(this.position, ac.receiver,
			                                           ac.arguments.withLastValue(Names.subscript_$eq, op))
					.resolveCall(markers, context);
			return subscript_$eq;
		}
		else if (type != FIELD_ACCESS)
		{
			// TODO Error
			throw new Error();
		}
		
		IMethod m = ICall.resolveMethod(context, this.receiver, this.name, this.arguments);
		if (m != null)
		{
			this.method = m;
			this.checkArguments(markers, context);
			return this;
		}
		return null;
	}
	
	@Override
	public void reportResolve(MarkerList markers, IContext context)
	{
		ICall.addResolveMarker(markers, this.position, this.receiver, this.name, this.arguments);
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.checkTypes(markers, context);
			
			FieldAccess fa = (FieldAccess) this.receiver;
			if (fa.field != null)
			{
				fa.field = fa.field.capture(context);
				this.arguments.setLastValue(fa.field.checkAssign(markers, context, fa.getPosition(), fa.receiver,
				                                                 this.arguments.getLastValue()));
			}
		}
		
		if (this.method != null)
		{
			this.method.checkArguments(markers, this.position, context, this.receiver, this.arguments,
			                           this.getGenericData());
		}
		this.arguments.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.receiver != null)
		{
			this.receiver.check(markers, context);
		}
		
		if (this.method != null)
		{
			if (this.receiver != null)
			{
				IType receiverType = this.receiver.getType();
				IType methodReturnType = this.method.getType();
				if (!receiverType.isSuperTypeOf(methodReturnType))
				{
					Marker marker = I18n.createMarker(this.position, "method.compound.type.incompatible", this.name,
					                                  this.receiver.toString());
					marker.addInfo(I18n.getString("receiver.type", receiverType));
					marker.addInfo(I18n.getString("method.type", methodReturnType));
					markers.add(marker);
				}
			}
			
			this.method
					.checkCall(markers, this.position, context, this.receiver, this.arguments, this.getGenericData());
		}
		
		this.arguments.check(markers, context);
	}
	
	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
		this.writeStatement(writer);
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
		this.writeStatement(writer);
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		FieldAccess access = (FieldAccess) this.receiver;
		IDataMember f = access.field;
		
		if (this.writeIINC(writer, f))
		{
			return;
		}
		
		f.writeSet(writer, access.receiver, new MethodCall(this.position, access, this.method, this.arguments),
		           this.getLineNumber());
	}
	
	private boolean writeIINC(MethodWriter writer, IDataMember f) throws BytecodeException
	{
		if (this.arguments.size() == 1 && f.getType() == Types.INT && f.isVariable())
		{
			if (((IVariable) f).isReferenceType())
			{
				return false;
			}
			
			boolean minus = false;
			if (this.name == Names.plus || (minus = this.name == Names.minus))
			{
				IValue value1 = this.arguments.getFirstValue();
				if (IValue.isNumeric(value1.valueTag()))
				{
					int count = value1.intValue();
					writer.writeIINC(((IVariable) f).getLocalIndex(), minus ? -count : count);
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.receiver != null)
		{
			this.receiver.toString(prefix, buffer);
			buffer.append(' ');
		}
		
		buffer.append(this.name).append('=');
		
		this.arguments.toString(prefix, buffer);
	}
}
