package dyvil.tools.repl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.classes.NestedClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.external.ExternalClass;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.Variable;
import dyvil.tools.compiler.ast.generic.ITypeVariable;
import dyvil.tools.compiler.ast.imports.HeaderComponent;
import dyvil.tools.compiler.ast.imports.PackageDecl;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.ConstructorMatch;
import dyvil.tools.compiler.ast.method.MethodMatch;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.ast.parameter.IArguments;
import dyvil.tools.compiler.ast.statement.FieldInitializer;
import dyvil.tools.compiler.ast.structure.IDyvilHeader;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.CodePosition;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class REPLContext implements IValued, IDyvilHeader
{
	private static final CodePosition	CODE_POSITION	= new CodePosition(1, 0, 1);
	
	protected static int				resultIndex;
	
	private Map<Name, REPLVariable>		variables		= new HashMap();
	private Map<Name, Operator>			operators		= new HashMap();
	
	private IValue						value;
	
	public void processValue()
	{
		if (this.value == null)
		{
			return;
		}
		
		MarkerList markers = new MarkerList();
		Name name;
		IValue value;
		IType type;
		ICodePosition position;
		
		if (this.value.valueTag() == IValue.VARIABLE)
		{
			Variable var = ((FieldInitializer) this.value).variable;
			name = var.name;
			value = var.value;
			type = var.type;
			position = var.position;
			
			if (value == null)
			{
				return;
			}
		}
		else
		{
			name = Name.getQualified("res" + resultIndex);
			value = this.value;
			type = Types.UNKNOWN;
			position = CODE_POSITION;
		}
		
		this.value = null;
		
		REPLVariable field = new REPLVariable(position, name, type, value);
		field.resolveTypes(markers, this);
		field.resolve(markers, this);
		field.checkTypes(markers, this);
		field.check(markers, this);
		field.foldConstants();
		
		if (!markers.isEmpty())
		{
			StringBuilder buf = new StringBuilder();
			String code = DyvilREPL.currentCode;
			markers.sort();
			for (Marker m : markers)
			{
				m.log(code, buf);
			}
			
			System.err.println(buf.toString());
			
			if (markers.getErrors() > 0)
			{
				return;
			}
		}
		
		field.compute();
		if (field.getType() != Types.VOID)
		{
			this.variables.put(name, field);
			System.out.println(field.toString());
			resultIndex++;
		}
	}
	
	@Override
	public String getName()
	{
		return "REPL";
	}
	
	@Override
	public void setPackage(Package pack)
	{
	}
	
	@Override
	public Package getPackage()
	{
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
	public void addImport(HeaderComponent i)
	{
	}
	
	@Override
	public void addStaticImport(HeaderComponent i)
	{
	}
	
	@Override
	public boolean hasStaticImports()
	{
		return false;
	}
	
	@Override
	public void addOperator(Operator op)
	{
		this.operators.put(op.name, op);
	}
	
	@Override
	public Operator getOperator(Name name)
	{
		return this.operators.get(name);
	}
	
	@Override
	public int classCount()
	{
		return 0;
	}
	
	@Override
	public void addClass(IClass iclass)
	{
	}
	
	@Override
	public IClass getClass(int index)
	{
		return null;
	}
	
	@Override
	public IClass getClass(Name name)
	{
		return null;
	}
	
	@Override
	public int innerClassCount()
	{
		return 0;
	}
	
	@Override
	public void addInnerClass(NestedClass iclass)
	{
	}
	
	@Override
	public NestedClass getInnerClass(int index)
	{
		return null;
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
	public boolean isStatic()
	{
		return true;
	}
	
	@Override
	public IClass getThisClass()
	{
		return null;
	}
	
	@Override
	public Package resolvePackage(Name name)
	{
		return null;
	}
	
	@Override
	public IClass resolveClass(Name name)
	{
		// Standart Dyvil Classes
		IClass iclass = Package.dyvilLang.resolveClass(name);
		if (iclass != null)
		{
			return iclass;
		}
		
		// Standart Java Classes
		return Package.javaLang.resolveClass(name);
	}
	
	@Override
	public ITypeVariable resolveTypeVariable(Name name)
	{
		return null;
	}
	
	@Override
	public IField resolveField(Name name)
	{
		IField f = this.variables.get(name);
		if (f != null)
		{
			return f;
		}
		return null;
	}
	
	@Override
	public void getMethodMatches(List<MethodMatch> list, IValue instance, Name name, IArguments arguments)
	{
	}
	
	@Override
	public void getConstructorMatches(List<ConstructorMatch> list, IArguments arguments)
	{
	}
	
	@Override
	public boolean handleException(IType type)
	{
		return true;
	}
	
	@Override
	public byte getVisibility(IMember member)
	{
		IClass iclass = member.getTheClass();
		if (iclass == this || iclass == null)
		{
			return VISIBLE;
		}
		
		int level = member.getAccessLevel();
		if ((level & Modifiers.SEALED) != 0)
		{
			if (iclass instanceof ExternalClass)
			{
				return SEALED;
			}
			// Clear the SEALED bit by ANDing with 0b1111
			level &= 0b1111;
		}
		if (level == Modifiers.PUBLIC)
		{
			return VISIBLE;
		}
		
		return INVISIBLE;
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
