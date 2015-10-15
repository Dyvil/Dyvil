package dyvil.tools.dpf.util;

import dyvil.tools.dpf.visitor.*;
import dyvil.tools.parsing.Name;

public class Printer implements NodeVisitor, ValueVisitor, ListVisitor, MapVisitor, StringInterpolationVisitor
{
	private int indent = 0;
	
	private void printIndent() {
		for (int i = 0;i < this.indent; i++) {
			System.out.print('\t');
		}
	}
	
	@Override
	public NodeVisitor visitNode(Name name)
	{
		this.printIndent();
		System.out.println("Node " + name);
		this.indent++;
		return this;
	}
	
	@Override
	public ValueVisitor visitProperty(Name name)
	{
		this.printIndent();
		System.out.println("Property " + name);
		return this;
	}
	
	@Override
	public NodeVisitor visitNodeAccess(Name name)
	{
		this.printIndent();
		System.out.println("Access " + name);
		return this;
	}
	
	@Override
	public void visitInt(int value)
	{
		this.printIndent();
		System.out.println("Int " + value);
	}
	
	@Override
	public void visitLong(long value)
	{
		this.printIndent();
		System.out.println("Long " + value);
	}
	
	@Override
	public void visitFloat(float value)
	{
		this.printIndent();
		System.out.println("Float " + value);
	}
	
	@Override
	public void visitDouble(double value)
	{
		this.printIndent();
		System.out.println("Double " + value);
	}
	
	@Override
	public void visitString(String value)
	{
		this.printIndent();
		System.out.println("String '" + value + "'");
	}
	
	@Override
	public StringInterpolationVisitor visitStringInterpolation()
	{
		this.indent++;
		return this;
	}
	
	@Override
	public void visitName(Name name)
	{
		this.printIndent();
		System.out.println("Name " + name);
	}
	
	@Override
	public ListVisitor visitList()
	{
		this.printIndent();
		System.out.println("List ");
		this.indent++;
		return this;
	}
	
	@Override
	public MapVisitor visitMap()
	{
		this.printIndent();
		System.out.println("Map ");
		return this;
	}
	
	@Override
	public BuilderVisitor visitBuilder(Name name)
	{
		return null;
	}
	
	@Override
	public ValueVisitor visitElement()
	{
		this.printIndent();
		System.out.print("Element ");
		return this;
	}
	
	@Override
	public ValueVisitor visitKey()
	{
		this.printIndent();
		System.out.print("Key ");
		return this;
	}
	
	@Override
	public ValueVisitor visitValue()
	{
		this.printIndent();
		System.out.print("Value ");
		return this;
	}
	
	@Override
	public void visitEnd()
	{
		this.indent--;
	}
	
	@Override
	public void visitStringPart(String string)
	{
		this.printIndent();
		System.out.print("String '" + string + "'");
		return;
	}
}
