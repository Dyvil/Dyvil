package dyvil.tools.compiler.ast.member;


public abstract class Member extends Annotatable
{
	private int					modifiers;
	
	private String				name;
	private Type				type;
	
	protected Member()
	{
	}
	
	public Member(String name, Type type, int modifiers)
	{
		this.name = name;
		this.type = type;
		this.modifiers = modifiers;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public void setType(Type type)
	{
		this.type = type;
	}
	
	public void setModifiers(int modifiers)
	{
		this.modifiers = modifiers;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public Type getType()
	{
		return this.type;
	}
	
	public int getModifiers()
	{
		return this.modifiers;
	}
}
