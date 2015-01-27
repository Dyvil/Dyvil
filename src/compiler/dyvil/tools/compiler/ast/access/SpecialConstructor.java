package dyvil.tools.compiler.ast.access;

import java.util.List;

import jdk.internal.org.objectweb.asm.Opcodes;
import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.statement.StatementList;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Type;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.ast.value.ValueList;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.Markers;
import dyvil.tools.compiler.lexer.position.ICodePosition;
import dyvil.tools.compiler.util.Modifiers;
import dyvil.tools.compiler.util.Util;

public class SpecialConstructor extends ASTNode implements IValue, IValued
{
	public IType		type;
	
	public ValueList	list;
	
	public boolean		isCustom;
	
	public IMethod		method;
	
	public SpecialConstructor(ICodePosition position)
	{
		this.position = position;
	}
	
	public SpecialConstructor(ICodePosition position, ConstructorCall cc)
	{
		this.position = cc.getPosition();
		this.type = cc.type;
		this.isCustom = cc.isCustom;
		this.list = new StatementList(position);
	}
	
	@Override
	public int getValueType()
	{
		return CONSTRUCTOR_CALL;
	}
	
	@Override
	public void expandPosition(ICodePosition position)
	{
		this.list.expandPosition(position);
	}
	
	@Override
	public void setType(IType type)
	{
		this.type = type;
	}
	
	@Override
	public IType getType()
	{
		return this.type;
	}
	
	@Override
	public IValue withType(IType type)
	{
		return Type.isSuperType(type, this.type) ? this : null;
	}
	
	@Override
	public boolean isType(IType type)
	{
		return Type.isSuperType(type, this.type);
	}
	
	@Override
	public int getTypeMatch(IType type)
	{
		if (this.type.equals(type))
		{
			return 3;
		}
		else if (this.type.isSuperType(type))
		{
			return 2;
		}
		return 0;
	}
	
	@Override
	public void setValue(IValue value)
	{
	}
	
	@Override
	public IValue getValue()
	{
		return null;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context)
	{
		this.type = this.type.resolve(context);
		if (!this.type.isResolved())
		{
			markers.add(Markers.create(this.type.getPosition(), "resolve.type", this.type.toString()));
			return;
		}
		
		this.list.resolveTypes(markers, this.type);
	}
	
	@Override
	public IValue resolve(List<Marker> markers, IContext context)
	{
		if (!this.type.isResolved())
		{
			return this;
		}
		
		MethodMatch method = this.type.resolveMethod(null, "<init>", Util.EMPTY_VALUES);
		if (method != null)
		{
			this.method = method.theMethod;
			this.list.resolve(markers, this.method);
			return this;
		}
		
		markers.add(Markers.create(this.position, "resolve.constructor", this.type.toString()));
		return this;
	}
	
	@Override
	public void check(List<Marker> markers, IContext context)
	{
		IClass iclass = this.type.getTheClass();
		if (iclass.hasModifier(Modifiers.INTERFACE_CLASS))
		{
			markers.add(Markers.create(this.position, "constructor.interface", iclass.getName()));
		}
		else if (iclass.hasModifier(Modifiers.ABSTRACT))
		{
			markers.add(Markers.create(this.position, "constructor.abstract", iclass.getName()));
		}
		else if (this.method != null)
		{
			byte access = context.getAccessibility(this.method);
			if (access == IContext.SEALED)
			{
				markers.add(Markers.create(this.position, "access.constructor.sealed", iclass.getName()));
			}
			else if ((access & IContext.READ_ACCESS) == 0)
			{
				markers.add(Markers.create(this.position, "access.constructor.invisible", iclass.getName()));
			}
			
			this.list.check(markers, this.method);
		}
	}
	
	@Override
	public IValue foldConstants()
	{
		this.list.foldConstants();
		return this;
	}
	
	@Override
	public void writeExpression(MethodWriter writer)
	{
		int opcode;
		int args = 0;
		if (this.isCustom)
		{
			opcode = Opcodes.INVOKESTATIC;
		}
		else
		{
			opcode = Opcodes.INVOKESPECIAL;
			args = 1;
			
			writer.visitTypeInsn(Opcodes.NEW, this.type);
			writer.visitInsn(Opcodes.DUP);
		}
		
		String owner = this.method.getTheClass().getInternalName();
		String name = "<init>";
		String desc = "()V";
		writer.visitMethodInsn(opcode, owner, name, desc, false, args, null);
		
		this.list.writeExpression(writer);
	}
	
	@Override
	public void writeStatement(MethodWriter writer)
	{
		int opcode;
		int args = 0;
		if (this.isCustom)
		{
			opcode = Opcodes.INVOKESTATIC;
		}
		else
		{
			opcode = Opcodes.INVOKESPECIAL;
			args = 1;
			
			writer.visitTypeInsn(Opcodes.NEW, this.type);
		}
		
		String owner = this.method.getTheClass().getInternalName();
		String name = "<init>";
		String desc = "()V";
		writer.visitMethodInsn(opcode, owner, name, desc, false, args, null);
		
		this.list.writeExpression(writer);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append("new ");
		this.type.toString(prefix, buffer);
		buffer.append(' ');
		this.list.toString(prefix, buffer);
	}
}
