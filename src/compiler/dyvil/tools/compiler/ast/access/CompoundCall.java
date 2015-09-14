package dyvil.tools.compiler.ast.access;

import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IVariable;
import dyvil.tools.compiler.ast.generic.ITypeContext;
import dyvil.tools.compiler.ast.member.INamed;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class CompoundCall extends AbstractCall implements INamed
{
	public Name name;
	
	public CompoundCall(ICodePosition position)
	{
		this.position = position;
	}
	
	public CompoundCall(ICodePosition position, IValue instance, Name name)
	{
		this.position = position;
		this.instance = instance;
		this.name = name;
	}
	
	public CompoundCall(ICodePosition position, IValue instance, Name name, IArguments arguments)
	{
		this.position = position;
		this.instance = instance;
		this.name = name;
		this.arguments = arguments;
	}
	
	@Override
	public int valueTag()
	{
		return METHOD_CALL;
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
	public void setValue(IValue value)
	{
		this.arguments = this.arguments.withLastValue(Name.update, value);
	}
	
	@Override
	public IValue getValue()
	{
		return this.arguments.getLastValue();
	}
	
	@Override
	protected IValue resolveCall(MarkerList markers, IContext context)
	{
		int type = this.instance.valueTag();
		if (type == APPLY_CALL)
		{
			AbstractCall ac = (AbstractCall) this.instance;
			
			// x(y...) op= z
			// -> x(y...) = x(y...).op(z)
			// -> x.update(y..., x.apply(y...).op(z))
			
			IValue op = new MethodCall(this.position, ac, this.name, this.arguments).resolveCall(markers, context);
			IValue update = new UpdateMethodCall(this.position, ac.instance, ac.arguments.withLastValue(Name.update, op)).resolveCall(markers, context);
			return update;
		}
		else if (type == SUBSCRIPT_GET)
		{
			AbstractCall ac = (AbstractCall) this.instance;
			
			// x[y...] op= z
			// -> x[y...] = x[y...].op(z)
			// -> x.subscript_=(y..., x.subscript(y...).op(z))
			
			IValue op = new MethodCall(this.position, ac, this.name, this.arguments).resolveCall(markers, context);
			IValue subscript_$eq = new SubscriptSetter(this.position, ac.instance, ac.arguments.withLastValue(Name.subscript_$eq, op)).resolveCall(markers, context);
			return subscript_$eq;
		}
		else if (type == FIELD_ACCESS)
		{
			return this;
		}
		// TODO Error
		throw new Error();
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.checkTypes(markers, context);
			
			FieldAccess fa = (FieldAccess) this.instance;
			if (fa.field != null)
			{
				fa.field = fa.field.capture(context);
				this.arguments.setLastValue(fa.field.checkAssign(markers, context, fa.getPosition(), fa.instance, this.arguments.getLastValue()));
			}
		}
		
		if (this.method != null)
		{
			this.method.checkArguments(markers, this.position, context, this.instance, this.arguments, this.getGenericData());
		}
		this.arguments.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		if (this.instance != null)
		{
			this.instance.check(markers, context);
		}
		
		if (this.method != null)
		{
			IType type1 = this.instance.getType();
			IType type2 = this.getType();
			if (!type1.isSuperTypeOf(type2))
			{
				Marker marker = markers.create(this.position, "method.compound.type", this.name, this.instance.toString());
				marker.addInfo("Callee Type: " + type1);
				marker.addInfo("Method Type: " + type2);
			}
			
			this.method.checkCall(markers, this.position, context, this.instance, this.arguments, this.getGenericData());
		}
		
		this.arguments.check(markers, context);
	}
	
	@Override
	public void writeExpression(MethodWriter writer, IType type) throws BytecodeException
	{
	}
	
	@Override
	public void writeExpression(MethodWriter writer) throws BytecodeException
	{
	}
	
	@Override
	public void writeStatement(MethodWriter writer) throws BytecodeException
	{
		FieldAccess access = (FieldAccess) this.instance;
		IDataMember f = access.field;
		
		if (this.writeIINC(writer, f))
		{
			return;
		}
		
		IValue instance = access.instance;
		if (instance != null)
		{
			instance.writeExpression(writer);
			writer.writeInsn(Opcodes.AUTO_DUP);
		}
		
		int lineNumber = this.instance.getLineNumber();
		f.writeGet(writer, null, lineNumber);
		this.method.writeCall(writer, null, this.arguments, null, lineNumber);
		f.writeSet(writer, null, null, lineNumber);
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
			if (this.name == Name.plus || (minus = this.name == Name.minus))
			{
				IValue value1 = this.arguments.getFirstValue();
				if (IValue.isNumeric(value1.valueTag()))
				{
					int count = value1.intValue();
					writer.writeIINC(((IVariable) f).getIndex(), minus ? -count : count);
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.instance != null)
		{
			this.instance.toString(prefix, buffer);
			buffer.append(' ');
		}
		
		if (Formatting.Method.convertQualifiedNames)
		{
			buffer.append(this.name.qualified).append("$eq");
		}
		else
		{
			buffer.append(this.name.unqualified).append('=');
		}
		
		this.arguments.toString(prefix, buffer);
	}
}
