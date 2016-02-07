package dyvil.tools.compiler.ast.parameter;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.external.ExternalMethod;
import dyvil.tools.compiler.ast.member.MemberKind;
import dyvil.tools.compiler.ast.method.ICallableMember;
import dyvil.tools.compiler.ast.modifiers.ModifierSet;
import dyvil.tools.compiler.ast.reference.ImplicitReferenceType;
import dyvil.tools.compiler.ast.reference.ReferenceType;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.compiler.util.Markers;
import dyvil.tools.parsing.Name;
import dyvil.tools.parsing.marker.Marker;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

import java.lang.annotation.ElementType;

public final class MethodParameter extends Parameter
{
	protected ICallableMember method;
	
	protected ReferenceType refType;
	protected boolean       assigned;
	
	public MethodParameter()
	{
	}
	
	public MethodParameter(Name name)
	{
		super(name);
	}

	public MethodParameter(Name name, IType type)
	{
		super(name, type);
	}

	public MethodParameter(Name name, IType type, ModifierSet modifierSet)
	{
		super(name, type, modifierSet);
	}

	public MethodParameter(ICodePosition position, Name name, IType type, ModifierSet modifiers)
	{
		super(position, name, type, modifiers);
	}

	@Override
	public MemberKind getKind()
	{
		return MemberKind.METHOD_PARAMETER;
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
	public boolean isAssigned()
	{
		return this.assigned;
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
	public ICallableMember getMethod()
	{
		return this.method;
	}

	@Override
	public IType getInternalType()
	{
		return this.refType != null ? this.refType : this.type;
	}
	
	@Override
	public boolean isReferenceCapturable()
	{
		return this.refType != null;
	}
	
	@Override
	public boolean isReferenceType()
	{
		return this.refType != null;
	}
	
	@Override
	public IValue checkAccess(MarkerList markers, ICodePosition position, IValue instance, IContext context)
	{
		return instance;
	}
	
	@Override
	public IValue checkAssign(MarkerList markers, IContext context, ICodePosition position, IValue instance, IValue newValue)
	{
		if (this.modifiers.hasIntModifier(Modifiers.FINAL))
		{
			markers.add(Markers.semantic(position, "parameter.assign.final", this.name.unqualified));
		}
		else
		{
			this.assigned = true;
		}
		
		IValue value1 = newValue.withType(this.type, null, markers, context);
		if (value1 == null)
		{
			Marker marker = Markers
					.semantic(newValue.getPosition(), "parameter.assign.type", this.name.unqualified);
			marker.addInfo(Markers.getSemantic("parameter.type", this.type));
			marker.addInfo(Markers.getSemantic("value.type", newValue.getType()));
			markers.add(marker);
		}
		else
		{
			newValue = value1;
		}
		
		return newValue;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		super.resolveTypes(markers, context);
		
		if (this.modifiers != null)
		{
			if (this.modifiers.hasIntModifier(Modifiers.VAR))
			{
				if (this.method instanceof ExternalMethod)
				{
					this.type = this.type.getElementType();
				}
				this.refType = new ImplicitReferenceType(this.type.getRefClass(), this.type);
			}
			else if (this.modifiers.hasIntModifier(Modifiers.INFIX & ~Modifiers.STATIC))
			{
				this.type.setExtension(true);
			}
		}
	}
	
	@Override
	public void write(MethodWriter writer)
	{
		this.localIndex = writer.localCount();
		writer.registerParameter(this.localIndex, this.name.qualified, this.getInternalType(), 0);

		this.writeAnnotations(writer);
	}

	@Override
	public void writeGet_Get(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		if (this.refType != null)
		{
			writer.writeVarInsn(Opcodes.ALOAD, this.localIndex);
			return;
		}
		writer.writeVarInsn(this.type.getLoadOpcode(), this.localIndex);
	}

	@Override
	public void writeGet_Unwrap(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		if (this.refType != null)
		{
			this.refType.writeUnwrap(writer);
		}
	}

	@Override
	public boolean writeSet_PreValue(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		if (this.refType != null)
		{
			writer.writeVarInsn(Opcodes.ALOAD, this.localIndex);
			return true;
		}
		return false;
	}

	@Override
	public void writeSet_Wrap(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		if (this.refType != null)
		{
			this.refType.writeWrap(writer);
		}
	}

	@Override
	public void writeSet_Set(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		if (this.refType == null)
		{
			writer.writeVarInsn(this.type.getStoreOpcode(), this.localIndex);
		}
	}

	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
	}
}
