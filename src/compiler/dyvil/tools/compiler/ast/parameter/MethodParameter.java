package dyvil.tools.compiler.ast.parameter;

import java.lang.annotation.ElementType;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.method.ICallableMember;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.MethodWriterImpl;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.I18n;
import dyvil.tools.compiler.util.Util;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public final class MethodParameter extends Parameter
{
	public ICallableMember method;
	
	public MethodParameter()
	{
	}
	
	public MethodParameter(ICodePosition position, Name name)
	{
		this.position = position;
		this.name = name;
		this.type = Types.UNKNOWN;
	}
	
	public MethodParameter(Name name)
	{
		this.name = name;
	}
	
	public MethodParameter(Name name, IType type)
	{
		this.name = name;
		this.type = type;
	}
	
	@Override
	public boolean isField()
	{
		return false;
	}
	
	@Override
	public boolean isVariable()
	{
		return true;
	}
	
	@Override
	public ElementType getElementType()
	{
		return ElementType.PARAMETER;
	}
	
	@Override
	public void setMethod(ICallableMember method)
	{
		this.method = method;
	}
	
	@Override
	public IValue checkAccess(MarkerList markers, ICodePosition position, IValue instance, IContext context)
	{
		return instance;
	}
	
	@Override
	public IValue checkAssign(MarkerList markers, IContext context, ICodePosition position, IValue instance, IValue newValue)
	{
		if ((this.modifiers & Modifiers.FINAL) != 0)
		{
			markers.add(I18n.createMarker(position, "parameter.assign.final", this.name.unqualified));
		}
		
		IValue value1 = newValue.withType(this.type, null, markers, context);
		if (value1 == null)
		{
			Marker marker = I18n.createMarker(newValue.getPosition(), "parameter.assign.type", this.name.unqualified);
			marker.addInfo(I18n.getString("parameter.type", this.type));
			marker.addInfo(I18n.getString("value.type", newValue.getType()));
			markers.add(marker);
		}
		else
		{
			newValue = value1;
		}
		
		return newValue;
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);
		
		if (this.defaultValue != null)
		{
			this.defaultValue = this.defaultValue.resolve(markers, context);
			
			IValue value1 = this.type.convertValue(this.defaultValue, this.type, markers, context);
			if (value1 == null)
			{
				Marker marker = I18n.createMarker(this.defaultValue.getPosition(), "parameter.type.incompatible", this.name.unqualified);
				marker.addInfo(I18n.getString("parameter.type", this.type));
				marker.addInfo(I18n.getString("value.type", this.defaultValue.getType()));
				markers.add(marker);
			}
			else
			{
				this.defaultValue = value1;
			}
			
			this.defaultValue = Util.constant(this.defaultValue, markers);
			return;
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
		
		if (this.type == Types.VOID)
		{
			markers.add(I18n.createMarker(this.position, "parameter.type.void"));
		}
	}
	
	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		super.cleanup(context, compilableList);
		
		if (this.defaultValue != null)
		{
			compilableList.addCompilable(this);
		}
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		if (this.defaultValue == null)
		{
			return;
		}
		
		// Copy the access modifiers and add the STATIC modifier
		int modifiers = this.method.getModifiers() & Modifiers.ACCESS_MODIFIERS | Modifiers.STATIC;
		String name = "parDefault$" + this.method.getName().qualified + "$" + this.index;
		String desc = "()" + this.type.getExtendedName();
		MethodWriter mw = new MethodWriterImpl(writer, writer.visitMethod(modifiers, name, desc, null, null));
		mw.begin();
		this.defaultValue.writeExpression(mw, this.type);
		mw.end(this.type);
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		this.localIndex = writer.localCount();
		writer.registerParameter(this.localIndex, this.name.qualified, this.type, 0);
		
		if ((this.modifiers & Modifiers.VAR) != 0)
		{
			writer.visitParameterAnnotation(this.index, "Ldyvil/annotation/_internal/var;", true);
		}
		
		this.writeAnnotations(writer);
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue instance, int lineNumber) throws BytecodeException
	{
		writer.writeVarInsn(this.type.getLoadOpcode(), this.localIndex);
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value, int lineNumber) throws BytecodeException
	{
		if (value != null)
		{
			value.writeExpression(writer, this.type);
		}
		writer.writeVarInsn(this.type.getStoreOpcode(), this.localIndex);
	}
}
