package dyvil.tools.repl;

import java.util.HashMap;
import java.util.Map;

import dyvil.lang.List;

import dyvil.collection.mutable.ArrayList;
import dyvil.reflect.Modifiers;
import dyvil.reflect.ReflectUtils;
import dyvil.tools.compiler.ast.classes.IClass;
import dyvil.tools.compiler.ast.consumer.IClassBodyConsumer;
import dyvil.tools.compiler.ast.consumer.IValueConsumer;
import dyvil.tools.compiler.ast.expression.IValue;
import dyvil.tools.compiler.ast.external.ExternalClass;
import dyvil.tools.compiler.ast.field.IDataMember;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.field.IProperty;
import dyvil.tools.compiler.ast.imports.ImportDeclaration;
import dyvil.tools.compiler.ast.imports.IncludeDeclaration;
import dyvil.tools.compiler.ast.member.IClassCompilable;
import dyvil.tools.compiler.ast.member.IClassMember;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.method.IConstructor;
import dyvil.tools.compiler.ast.method.IMethod;
import dyvil.tools.compiler.ast.operator.Operator;
import dyvil.tools.compiler.ast.structure.DyvilHeader;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.lexer.marker.Marker;
import dyvil.tools.compiler.lexer.marker.MarkerList;
import dyvil.tools.compiler.lexer.position.CodePosition;

public class REPLContext extends DyvilHeader implements IValueConsumer, IClassBodyConsumer
{
	private static final CodePosition	CODE_POSITION	= new CodePosition(1, 0, 1);
	
	protected static int				resultIndex;
	protected static int				classIndex;
	
	private Map<Name, REPLVariable>		variables		= new HashMap();
	
	private String						currentClassName;
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
	
	private boolean computeVariable(REPLVariable field)
	{
		this.currentClassName = "REPL" + classIndex++;
		
		MarkerList markers = new MarkerList();
		field.resolveTypes(markers, this);
		field.resolve(markers, this);
		field.checkTypes(markers, this);
		field.check(markers, this);
		
		if (this.reportErrors(markers))
		{
			this.compilableList.clear();
			this.innerClassList.clear();
			return false;
		}
		
		field.foldConstants();
		
		for (IClassCompilable icc : this.innerClassList)
		{
			try
			{
				String fileName = icc.getFileName();
				byte[] bytes = ClassWriter.compile(icc);
				ReflectUtils.unsafe.defineClass(fileName, bytes, 0, bytes.length, null, null);
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
		
		field.compute(this.currentClassName, this.compilableList);
		
		this.compilableList.clear();
		this.innerClassList.clear();
		return true;
	}
	
	@Override
	public void setValue(IValue value)
	{
		Name name = Name.getQualified("res" + resultIndex);
		REPLVariable field = new REPLVariable(CODE_POSITION, name, Types.UNKNOWN, value);
		field.modifiers = Modifiers.FINAL;
		
		if (this.computeVariable(field))
		{
			if (field.getType() != Types.VOID)
			{
				this.variables.put(field.name, field);
				System.out.println(field.toString());
				resultIndex++;
			}
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
	public void addImport(ImportDeclaration declaration)
	{
		MarkerList markers = new MarkerList();
		declaration.resolveTypes(markers, this, false);
		
		if (this.reportErrors(markers))
		{
			return;
		}
		
		super.addImport(declaration);
		System.out.println("Added import declaration for '" + declaration.theImport + "'");
	}
	
	@Override
	public void addUsing(ImportDeclaration declaration)
	{
		MarkerList markers = new MarkerList();
		declaration.resolveTypes(markers, this, true);
		
		if (this.reportErrors(markers))
		{
			return;
		}
		
		super.addImport(declaration);
		System.out.println("Added using declaration for '" + declaration.theImport + "'");
	}
	
	@Override
	public void addInclude(IncludeDeclaration component)
	{
		component.resolve(markers);
		
		if (this.reportErrors(markers))
		{
			return;
		}
		
		this.addIncludeToArray(component);
		System.out.println("Included the header '" + component.getHeader().getFullName() + "'");
		return;
	}
	
	@Override
	public void addClass(IClass iclass)
	{
		MarkerList markers = new MarkerList();
		iclass.resolveTypes(markers, this);
		iclass.resolve(markers, this);
		iclass.checkTypes(markers, this);
		iclass.check(markers, this);
		iclass.foldConstants();
		
		super.addClass(iclass);
		System.out.println("Defined class " + iclass.getName());
	}
	
	@Override
	public void addInnerClass(IClassCompilable iclass)
	{
		if (iclass.hasSeparateFile())
		{
			iclass.setInnerIndex(this.currentClassName, this.innerClassList.size());
			this.innerClassList.add(iclass);
		}
		else
		{
			iclass.setInnerIndex(this.currentClassName, this.compilableList.size());
			this.compilableList.add(iclass);
		}
	}
	
	@Override
	public void addField(IField field)
	{
		this.currentClassName = "REPL" + classIndex++;
		
		REPLVariable var = new REPLVariable(field.getPosition(), field.getName(), field.getType(), field.getValue());
		var.setAnnotations(field.getAnnotations(), field.annotationCount());
		var.modifiers = field.getModifiers();
		
		if (this.computeVariable(var))
		{
			this.variables.put(var.name, var);
			System.out.println(var.toString());
		}
	}
	
	@Override
	public void addProperty(IProperty property)
	{
		System.out.println(property);
	}
	
	@Override
	public void addMethod(IMethod method)
	{
		System.out.println(method);
	}
	
	@Override
	public void addConstructor(IConstructor constructor)
	{
	}
	
	@Override
	public IDataMember resolveField(Name name)
	{
		IField f = this.variables.get(name);
		if (f != null)
		{
			return f;
		}
		
		return super.resolveField(name);
	}
	
	@Override
	public boolean handleException(IType type)
	{
		return true;
	}
	
	@Override
	public byte getVisibility(IClassMember member)
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
	public String getName()
	{
		return this.currentClassName;
	}
	
	@Override
	public String getFullName()
	{
		return this.currentClassName;
	}
	
	@Override
	public String getFullName(String name)
	{
		return this.currentClassName + '$' + name;
	}
	
	@Override
	public String getInternalName()
	{
		return this.currentClassName;
	}
	
	@Override
	public String getInternalName(String name)
	{
		return this.currentClassName + '$' + name;
	}
}
