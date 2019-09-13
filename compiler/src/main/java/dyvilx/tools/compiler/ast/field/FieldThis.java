package dyvilx.tools.compiler.ast.field;

import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.backend.classes.ClassWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.backend.method.MethodWriter;

public class FieldThis implements IAccessible
{
	protected final IClass      owner;
	protected final IAccessible targetAccess;
	protected final IType       targetType;

	private String name;
	private String desc;

	public FieldThis(IClass owner, IAccessible targetAccess, IType targetType)
	{
		this.owner = owner;
		this.targetType = targetType;
		this.targetAccess = targetAccess;
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
		return this.name = "this$" + this.targetType.getName().qualified;
	}

	public String getDescriptor()
	{
		if (this.desc != null)
		{
			return this.desc;
		}
		return this.desc = 'L' + this.targetType.getInternalName() + ';';
	}

	@Override
	public IType getType()
	{
		return this.targetType;
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
