package dyvil.tools.compiler.backend.visitor;

import org.objectweb.asm.AnnotationVisitor;

import dyvil.tools.compiler.DyvilCompiler;
import dyvil.tools.compiler.ast.constant.EnumValue;
import dyvil.tools.compiler.ast.member.Name;
import dyvil.tools.compiler.ast.structure.Package;
import dyvil.tools.compiler.ast.type.IType;
import dyvil.tools.compiler.ast.value.ArrayValue;
import dyvil.tools.compiler.ast.value.IValue;
import dyvil.tools.compiler.ast.value.IValued;
import dyvil.tools.compiler.backend.ClassFormat;

public class ValueAnnotationVisitor extends AnnotationVisitor
{
	private IValued	valued;
	
	public ValueAnnotationVisitor(IValued valued)
	{
		super(DyvilCompiler.asmVersion);
		this.valued = valued;
	}
	
	@Override
	public void visit(String key, Object value)
	{
		this.valued.setValue(IValue.fromObject(value));
	}
	
	static IValue getEnumValue(String enumClass, String name)
	{
		IType t = ClassFormat.internalToType(enumClass);
		t.resolve(null, Package.rootPackage);
		return new EnumValue(t, Name.getQualified(name));
	}
	
	@Override
	public void visitEnum(String key, String enumClass, String name)
	{
		IValue enumValue = getEnumValue(enumClass, name);
		if (enumValue != null)
		{
			this.valued.setValue(enumValue);
		}
	}
	
	@Override
	public AnnotationVisitor visitArray(String key)
	{
		ArrayValue valueList = new ArrayValue();
		this.valued.setValue(valueList);
		return new ArrayAnnotationVisitor(this.api, valueList);
	}
	
	@Override
	public void visitEnd()
	{
	}
}
