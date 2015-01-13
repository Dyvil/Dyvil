package dyvil.tools.compiler.ast.imports;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dyvil.tools.compiler.ast.ASTNode;
import dyvil.tools.compiler.ast.api.*;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.config.Formatting;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.SemanticError;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class SimpleImport extends ASTNode implements IImport, IImportContainer
{
	public IImport				parent;
	public String				name;
	public String				alias;
	public IImport				child;
	
	private IClass				theClass;
	private Package				thePackage;
	
	private FieldMatch			field;
	private List<MethodMatch>	methods;
	
	public SimpleImport(ICodePosition position)
	{
		this.position = position;
	}
	
	public SimpleImport(ICodePosition position, IImport parent, String name)
	{
		this.position = position;
		this.parent = parent;
		this.name = name;
	}
	
	@Override
	public void addImport(IImport iimport)
	{
		this.child = iimport;
	}
	
	public void setAlias(String alias)
	{
		this.alias = alias;
	}
	
	public String getAlias()
	{
		return this.alias;
	}
	
	@Override
	public void resolveTypes(List<Marker> markers, IContext context, boolean isStatic)
	{
		if (isStatic && this.child == null)
		{
			FieldMatch field = context.resolveField(null, this.name);
			if (field != null)
			{
				this.field = field;
				return;
			}
			
			this.methods = new ArrayList();
			context.getMethodMatches(this.methods, null, this.name, null);
			if (!this.methods.isEmpty())
			{
				Collections.sort(this.methods);
				return;
			}
			
			markers.add(new SemanticError(this.position, "'" + this.name + "' could not be resolved to a method or field"));
			return;
		}
		
		Package pack = context.resolvePackage(this.name);
		if (pack != null)
		{
			this.thePackage = pack;
			if (this.child != null)
			{
				this.child.resolveTypes(markers, pack, isStatic);
			}
			return;
		}
		
		IClass iclass = context.resolveClass(this.name);
		if (iclass != null)
		{
			this.theClass = iclass;
			if (this.child != null)
			{
				this.child.resolveTypes(markers, iclass, isStatic);
			}
			return;
		}
		
		markers.add(new SemanticError(this.position, "'" + this.name + "' could not be resolved to a package or class"));
	}
	
	@Override
	public boolean isStatic()
	{
		return false;
	}
	
	@Override
	public IType getThisType()
	{
		return this.theClass.getThisType();
	}
	
	@Override
	public Package resolvePackage(String name)
	{
		if (this.name.equals(name) || this.alias.equals(name))
		{
			return this.thePackage;
		}
		return null;
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		if (this.child != null)
		{
			return this.child.resolveClass(name);
		}
		if (this.name.equals(name) || this.alias.equals(name))
		{
			return this.theClass;
		}
		return null;
	}
	
	@Override
	public FieldMatch resolveField(IContext context, String name)
	{
		if (this.child != null)
		{
			return this.child.resolveField(context, name);
		}
		if (this.name.equals(name) || this.alias.equals(name))
		{
			return this.field;
		}
		return null;
	}
	
	@Override
	public MethodMatch resolveMethod(IContext context, String name, IType[] argumentTypes)
	{
		if (this.child != null)
		{
			return this.child.resolveMethod(context, name, argumentTypes);
		}
		if (!this.methods.isEmpty() && (this.name.equals(name) || this.alias.equals(name)))
		{
			return this.methods.get(0);
		}
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IType type, String name, IType[] argumentTypes)
	{
		if (this.child != null)
		{
			this.child.getMethodMatches(list, type, name, argumentTypes);
		}
		if (this.name.equals(name) || this.alias.equals(name))
		{
			list.addAll(this.methods);
		}
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void toString(String prefix, StringBuilder buffer)
	{
		buffer.append(this.name);
		if (this.alias != null)
		{
			buffer.append(Formatting.Import.aliasSeperator);
			buffer.append(this.alias);
		}
		if (this.child != null)
		{
			buffer.append('.');
			this.child.toString(prefix, buffer);
		}
	}
}
