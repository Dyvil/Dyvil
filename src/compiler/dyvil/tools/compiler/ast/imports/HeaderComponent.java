package dyvil.tools.compiler.ast.imports;

import java.util.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IContext;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class HeaderComponent extends ASTNode implements IImport
{
	public static final int	IMPORT	= 0;
	public static final int	USING	= 1;
	
	public IImport			theImport;
	public IImport			last;
	public boolean			isStatic;
	
	public HeaderComponent(ICodePosition position)
	{
		this.position = position;
	}
	
	public HeaderComponent(ICodePosition position, boolean isStatic)
	{
		this.position = position;
		this.isStatic = isStatic;
	}
	
	@Override
	public void addImport(IImport iimport)
	{
		this.theImport = iimport;
	}
	
	@Override
	public void resolveTypes(MarkerList markers, IContext context, boolean isStatic)
	{
		this.theImport.resolveTypes(markers, Package.rootPackage, this.isStatic);
		
		IImport iimport = this.theImport.getChild();
		while (true)
		{
			IImport child = iimport.getChild();
			if (child == null)
			{
				break;
			}
			iimport = child;
		}
		
		this.last = iimport;
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		return this.last.resolvePackage(name);
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		return this.last.resolveClass(name);
	}
	
	@Override
	public IField resolveField(Name name)
	{
		return this.last.resolveField(name);
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		this.last.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		if (this.isStatic)
		{
			buffer.append("using ");
		}
		else
		{
			buffer.append("import ");
		}
		this.theImport.toString(prefix, buffer);
	}
}
