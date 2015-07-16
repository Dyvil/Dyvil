package dyvil.tools.compiler.ast.context;

import dyvil.collection.List;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;

public class CombiningContext implements IContext
{
	private final IContext context1;
	private final IContext context2;
	
	public CombiningContext(IContext context1, IContext context2)
	{
		this.context1 = context1;
		this.context2 = context2;
	}

	@Override
	public boolean isStatic()
	{
		return this.context1.isStatic() || this.context2.isStatic();
	}

	@Override
	public IDyvilHeader getHeader()
	{
		IDyvilHeader header = this.context1.getHeader();
		return header == null ? this.context2.getHeader() : header;
	}

	@Override
	public IClass getThisClass()
	{
		IClass iclass = this.context1.getThisClass();
		return iclass == null ? this.context2.getThisClass() : iclass;
	}

	@Override
	public Package resolvePackage(Name name)
	{
		Package pack = this.context1.resolvePackage(name);
		return pack == null ? this.context2.resolvePackage(name) : pack;
	}

	@Override
	public IClass resolveClass(Name name)
	{
		IClass iclass = this.context1.resolveClass(name);
		return iclass == null ? this.context2.resolveClass(name) : iclass;
	}

	@Override
	public IType resolveType(Name name)
	{
		IType type = this.context1.resolveType(name);
		return type == null ? this.context2.resolveType(name) : type;
	}

	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		ITypeVariable typeVar = this.context1.resolveTypeVariable(name);
		return typeVar == null ? this.context2.resolveTypeVariable(name) : typeVar;
	}

	@Override
	public IDataMember resolveField(Name name)
	{
		IDataMember field = this.context1.resolveField(name);
		return field == null ? this.context2.resolveField(name) : field;
	}

	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		this.context1.getMethodMatches(list, instance, name, arguments);
		this.context2.getMethodMatches(list, instance, name, arguments);
	}

	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
		this.context1.getConstructorMatches(list, arguments);
		this.context2.getConstructorMatches(list, arguments);
	}

	@Override
	public byte getVisibility(IClassMember member)
	{
		return 0;
	}

	@Override
	public boolean handleException(IType type)
	{
		return this.context1.handleException(type) || this.context2.handleException(type);
	}
}
