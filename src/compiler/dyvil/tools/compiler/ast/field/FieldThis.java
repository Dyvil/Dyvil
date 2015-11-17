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
	protected IClass			owner;
	protected final IAccessible	outer;
	protected final IClass		theClass;
	
	private String	name;
	private String	desc;
	
	public FieldThis(IClass owner, IAccessible outer, IClass theClass)
	{
		this.owner = owner;
		this.theClass = theClass;
		this.outer = outer;
		
		this.name = "this$" + theClass.getName().qualified;
		this.desc = 'L' + this.theClass.getInternalName() + ';';
	}
	
	public IClass getTheClass()
	{
		return this.theClass;
	}
	
	public IAccessible getOuter()
	{
		return this.outer;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String getDescription()
	{
		return this.desc;
	}
	
	@Override
	public IType getType()
	{
		return this.theClass.getType();
	}
	
	public void writeField(ClassWriter writer)
	{
		writer.visitField(Modifiers.PRIVATE | Modifiers.SYNTHETIC, this.name, this.desc, null, null);
	}
	
	@Override
	public void writeGet(MethodWriter writer) throws BytecodeException
	{
		writer.writeVarInsn(Opcodes.ALOAD, 0);
		writer.writeFieldInsn(Opcodes.GETFIELD, this.owner.getInternalName(), this.name, this.desc);
	}
}
