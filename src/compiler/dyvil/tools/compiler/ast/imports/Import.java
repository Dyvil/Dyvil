package dyvil.tools.compiler.ast.imports;

import java.util.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public final class Import extends ASTNode implements IImportContainer
{
	public IImport	theImport;
	public IImport	last;
	public boolean	isStatic;
	
	public Import(ICodePosition position)
	{
		this.position = position;
	}
	
	@Override
	public void addImport(IImport iimport)
	{
		this.theImport = iimport;
	}
	
	public void resolveTypes(MarkerList markers)
	{
		this.theImport.resolveTypes(markers, Package.rootPackage, this.isStatic);
		
		IImport iimport = this.theImport;
		while (iimport instanceof SimpleImport)
		{
			IImport child = ((SimpleImport) iimport).child;
			if (child == null)
			{
				break;
			}
			iimport = child;
		}
		
		this.last = iimport;
	}
	
	@Override
	public boolean isStatic()
	{
		return false;
	}
	
	@Override
	public IType getThisType()
	{
		return null;
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
	public FieldMatch resolveField(Name name)
	{
		return this.last.resolveField(name);
	}
	
	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
		this.last.getMethodMatches(list, instance, name, arguments);
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return 0;
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
