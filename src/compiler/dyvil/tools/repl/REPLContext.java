package dyvil.tools.repl;

import java.util.HashMap;
import java.util.Map;

import dyvil.collection.mutable.ArrayList;
import dyvil.lang.List;
import dyvil.reflect.Modifiers;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.expression.IValued;
import dyvil.tools.compiler.ast.external.ExternalClass;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.imports.ImportDeclaration;
import dyvil.tools.compiler.ast.imports.IncludeDeclaration;
import dyvil.tools.compiler.ast.member.IClassCompilable;
import dyvil.tools.compiler.ast.member.IMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.ast.structure.DyvilHeader;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.CodePosition;
import dyvil.tools.compiler.lexer.position.ICodePosition;

public class REPLContext extends DyvilHeader implements IValued
{
	private static final CodePosition	CODE_POSITION	= new CodePosition(1, 0, 1);
	
	protected static int				resultIndex;
	
	private Map<Name, REPLVariable>		variables		= new HashMap();
	
	private IValue						value;
	private ImportDeclaration			importDeclaration;
	private IncludeDeclaration			includeDeclaration;
	private IClass						tempClass;
	private List<IClassCompilable>		compilableList	= new ArrayList();
	private List<IClassCompilable>		innerClassList	= new ArrayList();
	
	public REPLContext()
	{
		super("REPL");
	}
	
	private boolean reportErrors(MarkerList markers)
	{
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
				return true;
			}
		}
		return false;
	}
	
	protected void processHeader()
	{
		MarkerList markers = new MarkerList();
		
		if (tempClass != null)
		{
			tempClass.resolveTypes(markers, this);
			tempClass.resolve(markers, this);
			tempClass.checkTypes(markers, this);
			tempClass.check(markers, this);
			tempClass.foldConstants();
			
			super.addClass(tempClass);
			System.out.println("Defined class " + tempClass.getName());
			tempClass = null;
		}
		
		if (this.includeDeclaration != null)
		{
			IncludeDeclaration inc = this.includeDeclaration;
			this.includeDeclaration = null;
			inc.resolve(markers);
			
			if (this.reportErrors(markers))
			{
				return;
			}
			
			this.addIncludeToArray(inc);
			System.out.println("Included " + inc.getHeader().getFullName());
			return;
		}
		
		if (this.importDeclaration == null || this.importDeclaration.theImport == null)
		{
			return;
		}
		importDeclaration.resolveTypes(markers, this, false);
		
		if (this.reportErrors(markers))
		{
			return;
		}
		
		boolean isStatic = importDeclaration.isStatic;
		
		if (isStatic)
		{
			super.addStaticImport(importDeclaration);
			System.out.println("Using " + importDeclaration.theImport);
		}
		else
		{
			super.addImport(importDeclaration);
			System.out.println("Imported " + importDeclaration.theImport);
		}
		
		importDeclaration = null;
	}
	
	protected void processValue()
	{
		if (this.value == null)
		{
			return;
		}
		
		MarkerList markers = new MarkerList();
		Name name = Name.getQualified("res" + resultIndex);
		IValue value = this.value;
		IType type = Types.UNKNOWN;
		ICodePosition position = CODE_POSITION;
		
		this.value = null;
		
		REPLVariable field = new REPLVariable(position, name, type, value);
		field.modifiers = Modifiers.FINAL;
		field.resolveTypes(markers, this);
		field.resolve(markers, this);
		field.checkTypes(markers, this);
		field.check(markers, this);
		
		if (this.reportErrors(markers))
		{
			return;
		}
		
		field.foldConstants();
		
		// TODO Inner Classes
		
		field.compute("REPL$" + resultIndex, this.compilableList);
		
		this.compilableList.clear();
		this.innerClassList.clear();
		
		if (field.getType() != Types.VOID)
		{
			this.variables.put(name, field);
			System.out.println(field.toString());
			resultIndex++;
		}
	}
	
	@Override
	public void addOperator(Operator op)
	{
		this.operators.put(op.name, op);
		
		System.out.println("Defined " + op);
	}
	
	@Override
	public Operator getOperator(Name name)
	{
		return this.operators.get(name);
	}
	
	@Override
	public void addImport(ImportDeclaration component)
	{
		importDeclaration = component;
	}
	
	@Override
	public void addStaticImport(ImportDeclaration component)
	{
		importDeclaration = component;
	}
	
	@Override
	public void addInclude(IncludeDeclaration component)
	{
		includeDeclaration = component;
	}
	
	@Override
	public void addClass(IClass iclass)
	{
		this.tempClass = iclass;
	}
	
	@Override
	public void addInnerClass(IClassCompilable iclass)
	{
		String name = "REPL$" + resultIndex;
		if (iclass.hasSeparateFile())
		{
			iclass.setInnerIndex(name, this.innerClassList.size());
			this.innerClassList.add(iclass);
		}
		else
		{
			iclass.setInnerIndex(name, this.compilableList.size());
			this.compilableList.add(iclass);
		}
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
