package dyvil.tools.compiler.ast.field;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;

public class FieldThis implements IAccessible
{
	protected final IClass      owner;
	protected final IAccessible targetAccess;
	protected final IClass      targetClass;

	private String name;
	private String desc;

	public FieldThis(IClass owner, IAccessible targetAccess, IClass targetClass)
	{
		this.owner = owner;
		this.targetClass = targetClass;
		this.targetAccess = targetAccess;
	}

	public IClass getTargetClass()
	{
		return this.targetClass;
	}

	public IAccessible getTargetAccess()
	{
		return this.targetAccess;
	}

	public String getName()
	{
		if (this.name != null)
		{
			return this.name;
		}
		return this.name = "this$" + this.targetClass.getName().qualified;
	}

	public String getDescriptor()
	{
		if (this.desc != null)
		{
			return this.desc;
		}
		return this.desc = 'L' + this.targetClass.getInternalName() + ';';
	}

	@Override
	public IType getType()
	{
		return this.targetClass.getThisType();
	}

	public void writeField(ClassWriter writer)
	{
		writer.visitField(Modifiers.PRIVATE | Modifiers.SYNTHETIC, this.getName(), this.getDescriptor(), null, null);
	}

	@Override
	public void writeGet(MethodWriter writer) throws BytecodeException
	{
		writer.visitVarInsn(Opcodes.ALOAD, 0);
		writer.visitFieldInsn(Opcodes.GETFIELD, this.owner.getInternalName(), this.getName(), this.getDescriptor());
	}
}
