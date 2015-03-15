package dyvil.tools.repl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dyvil.tools.compiler.ast.access.FieldInitializer;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.field.FieldMatch;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.imports.Import;
import dyvil.tools.compiler.ast.imports.PackageDecl;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.structure.IDyvilUnit;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;

public class REPLContext implements IValued, IDyvilUnit
{
	private int						resultIndex;
	
	private Map<String, Variable>	variables	= new HashMap();
	
	private IValue					value;
	
	public void processValue()
	{
		if (this.value == null)
		{
			return;
		}
		
		MarkerList markers = new MarkerList();
		String name;
		Variable var;
		
		if (value.getValueType() == IValue.VARIABLE)
		{
			var = ((FieldInitializer) value).variable;
			name = var.qualifiedName;
			value = var.value;
			
			if (var.value == null)
			{
				return;
			}
			
			var.resolveTypes(markers, this);
			var.resolve(markers, this);
			var.check(markers, this);
			var.foldConstants();
		}
		else
		{
			name = "res" + resultIndex++;
			var = new Variable(null, name, null);
			value.resolveTypes(markers, this);
			value = value.resolve(markers, this);
			value.check(markers, this);
			value = value.foldConstants();
			var.setValue(value);
			var.setType(value.getType());
		}
		
		if (!markers.isEmpty())
		{
			StringBuilder buf = new StringBuilder();
			String code = DyvilREPL.currentCode;
			for (Marker m : markers)
			{
				m.log(code, buf);
			}
			
			System.out.println(buf.toString());
			
			if (markers.getErrors() > 0)
			{
				return;
			}
		}
		
		this.variables.put(name, var);
		System.out.println(var.toString());
	}
	
	@Override
	public void setPackage(Package pack)
	{
		// unsupported
	}
	
	@Override
	public Package getPackage()
	{
		// unsupported
		return null;
	}
	
	@Override
	public void setPackageDeclaration(PackageDecl pack)
	{
		
	}
	
	@Override
	public PackageDecl getPackageDeclaration()
	{
		return null;
	}
	
	@Override
	public void addImport(Import i)
	{
	}
	
	@Override
	public void addStaticImport(Import i)
	{
	}
	
	@Override
	public boolean hasStaticImports()
	{
		return false;
	}
	
	@Override
	public void addClass(IClass iclass)
	{
	}
	
	@Override
	public String getInternalName(String subClass)
	{
		return null;
	}
	
	@Override
	public String getFullName(String subClass)
	{
		return null;
	}
	
	@Override
	public IType getThisType()
	{
		return null;
	}
	
	@Override
	public Package resolvePackage(String name)
	{
		return null;
	}
	
	@Override
	public IClass resolveClass(String name)
	{
		return null;
	}
	
	@Override
	public FieldMatch resolveField(String name)
	{
		return null;
	}
	
	@Override
	public MethodMatch resolveMethod(IValue instance, String name, IArguments arguments)
	{
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, String name, IArguments arguments)
	{
	}
	
	@Override
	public MethodMatch resolveConstructor(IArguments arguments)
	{
		return null;
	}
	
	@Override
	public void getConstructorMatches(List<MethodMatch> list, IArguments arguments)
	{
	}
	
	@Override
	public byte getAccessibility(IMember member)
	{
		return 0;
	}
	
	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}
	
	@Override
	public IValue getValue()
	{
		return this.value;
	}
}
