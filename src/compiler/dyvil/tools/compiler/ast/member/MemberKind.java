package dyvil.tools.compiler.ast.member;

public enum MemberKind
{
	CLASS("class"),
	METHOD("method"),
	CONSTRUCTOR("constructor"),
	INITIALIZER("initializer"),
	FIELD("field"),
	PROPERTY("property"),
	METHOD_PARAMETER("parameter"),
	CLASS_PARAMETER("classparameter"),
	VARIABLE("variable");

	private String name;

	MemberKind(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return this.name;
	}
}
