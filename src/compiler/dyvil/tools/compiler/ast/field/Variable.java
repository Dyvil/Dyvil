package dyvil.tools.compiler.ast.field;

import java.lang.annotation.ElementType;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.member.Member;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

import org.objectweb.asm.Label;

public final class Variable extends Member implements IVariable
{
	public int		index;
	public IValue	value;
	private IType	refType;
	
	public Variable()
	{
	}
	
	public Variable(ICodePosition position)
	{
		this.position = position;
	}
	
	public Variable(ICodePosition position, IType type)
	{
		this.position = position;
		this.type = type;
	}
	
	public Variable(ICodePosition position, Name name, IType type)
	{
		this.name = name;
		this.type = type;
		this.position = position;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.value;
	}
	
	@Override
	public void setIndex(int index)
	{
		this.index = index;
	}
	
	@Override
	public int getIndex()
	{
		return this.index;
	}
	
	@Override
	public boolean addRawAnnotation(String type)
	{
		switch (type)
		{
		case "dyvil/annotation/lazy":
			this.modifiers |= Modifiers.LAZY;
			return false;
		}
		return true;
	}
	
	@Override
	public ElementType getAnnotationType()
	{
		return ElementType.LOCAL_VARIABLE;
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
			markers.add(position, "variable.assign.final", this.name.unqualified);
		}
		
		IValue value1 = newValue.withType(this.type, null, markers, context);
		if (value1 == null)
		{
			Marker marker = markers.create(newValue.getPosition(), "variable.assign.type", this.name.unqualified);
			marker.addInfo("Variable Type: " + this.type);
			marker.addInfo("Value Type: " + newValue.getType());
		}
		else
		{
			newValue = value1;
		}
		
		return newValue;
	}
	
	@Override
	public boolean isCapturable()
	{
		return true;
	}
	
	@Override
	public boolean isReferenceType()
	{
		return this.refType != null;
	}
	
	@Override
	public void setReferenceType()
	{
		if (this.refType == null)
		{
			this.refType = Types.getRefType(this.type);
		}
	}
	
	@Override
	public IType getReferenceType()
	{
		return this.refType;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		super.resolveTypes(markers, context);
		
		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}
	}
	
	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);
		
		if (this.value != null)
		{
			this.value = this.value.resolve(markers, context);
		}
		
		boolean inferType = false;
		;
		if (this.type == Types.UNKNOWN)
		{
			inferType = true;
			this.type = this.value.getType();
			if (this.type == Types.UNKNOWN)
			{
				markers.add(this.position, "variable.type.infer", this.name.unqualified);
				this.type = Types.ANY;
			}
		}
		
		IValue value1 = this.value.withType(this.type, this.type, markers, context);
		if (value1 == null)
		{
			Marker marker = markers.create(this.position, "variable.type", this.name.unqualified);
			marker.addInfo("Variable Type: " + this.type);
			marker.addInfo("Value Type: " + this.value.getType());
		}
		else
		{
			this.value = value1;
			if (inferType)
			{
				this.type = value1.getType();
			}
		}
	}
	
	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		super.checkTypes(markers, context);
		
		this.value.checkTypes(markers, context);
	}
	
	@Override
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, context);
		
		this.value.check(markers, context);
		
		if (this.type == Types.VOID)
		{
			markers.add(this.position, "variable.type.void");
		}
	}
	
	@Override
	public void foldConstants()
	{
		super.foldConstants();
		
		this.value = this.value.foldConstants();
	}
	
	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		super.cleanup(context, compilableList);
		
		this.value = this.value.cleanup(context, compilableList);
	}
	
	@Override
	public String getDescription()
	{
		return this.type.getExtendedName();
	}
	
	@Override
	public String getSignature()
	{
		return this.type.getSignature();
	}
	
	public void writeLocal(MethodWriter writer, Label start, Label end)
	{
		IType type = this.refType != null ? this.refType : this.type;
		writer.writeLocal(this.index, this.name.qualified, type.getExtendedName(), type.getSignature(), start, end);
	}
	
	public void writeInit(MethodWriter writer, IValue value) throws BytecodeException
	{
		if (this.refType != null)
		{
			IConstructor c = this.refType.getTheClass().getBody().getConstructor(0);
			writer.writeTypeInsn(Opcodes.NEW, this.refType.getInternalName());
			writer.writeInsn(Opcodes.DUP);
			
			if (value != null)
			{
				value.writeExpression(writer);
			}
			else
			{
				writer.writeInsn(Opcodes.AUTO_DUP_X1);
			}
			c.writeInvoke(writer);
			
			this.index = writer.localCount();
			
			writer.setLocalType(this.index, this.refType.getInternalName());
			writer.writeVarInsn(Opcodes.ASTORE, this.index);
			return;
		}
		
		if (value != null)
		{
			value.writeExpression(writer);
		}
		this.index = writer.localCount();
		writer.setLocalType(this.index, this.type.getFrameType());
		writer.writeVarInsn(this.type.getStoreOpcode(), this.index);
	}
	
	@Override
	public void writeGet(MethodWriter writer, IValue instance) throws BytecodeException
	{
		if (this.refType != null)
		{
			writer.writeVarInsn(Opcodes.ALOAD, this.index);
			
			IClass c = this.refType.getTheClass();
			IDataMember f = c.getBody().getField(0);
			f.writeGet(writer, null);
			
			if (c == Types.OBJECT_REF_CLASS)
			{
				c = this.type.getTheClass();
				if (c != Types.OBJECT_CLASS)
				{
					writer.writeTypeInsn(Opcodes.CHECKCAST, c.getInternalName());
				}
			}
			return;
		}
		
		writer.writeVarInsn(this.type.getLoadOpcode(), this.index);
	}
	
	@Override
	public void writeSet(MethodWriter writer, IValue instance, IValue value) throws BytecodeException
	{
		if (this.refType != null)
		{
			writer.writeVarInsn(Opcodes.ALOAD, this.index);
			
			if (value != null)
			{
				value.writeExpression(writer);
			}
			else
			{
				writer.writeInsn(Opcodes.AUTO_SWAP);
			}
			
			IDataMember f = this.refType.getTheClass().getBody().getField(0);
			f.writeSet(writer, null, null);
			return;
		}
		
		if (value != null)
		{
			value.writeExpression(writer);
		}
		
		writer.writeVarInsn(this.type.getStoreOpcode(), this.index);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		this.type.toString("", buffer);
		buffer.append(' ').append(this.name);
		
		if (this.value != null)
		{
			buffer.append(Formatting.Field.keyValueSeperator);
			this.value.toString(prefix, buffer);
		}
	}
}
