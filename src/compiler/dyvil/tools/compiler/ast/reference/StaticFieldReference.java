package dyvil.tools.compiler.ast.reference;

import dyvil.collection.Map;
import dyvil.collection.Set;
import dyvil.collection.mutable.IdentityHashMap;
import dyvil.collection.mutable.MapBasedSet;
import dyvil.reflect.Modifiers;
import dyvil.reflect.Opcodes;
import dyvil.tools.compiler.ast.context.IContext;
import dyvil.tools.compiler.ast.field.IField;
import dyvil.tools.compiler.ast.structure.IClassCompilableList;
import dyvil.tools.compiler.ast.type.Types;
import dyvil.tools.compiler.backend.ClassWriter;
import dyvil.tools.compiler.backend.IClassCompilable;
import dyvil.tools.compiler.backend.MethodWriter;
import dyvil.tools.compiler.backend.exception.BytecodeException;
import dyvil.tools.parsing.marker.MarkerList;
import dyvil.tools.parsing.position.ICodePosition;

public class StaticFieldReference implements IReference, IClassCompilable
{
	private static Map<String, Set<IField>> map = new IdentityHashMap();
	
	protected IField						field;
	
	private boolean							isUnique;
	private String							className;
	private String							idFieldName;
	private String							refFieldType;
	
	public StaticFieldReference(IField field)
	{
		this.field = field;
	}
	
	private static boolean addToMap(String className, IField field)
	{
		Set<IField> set = map.get(className);
		if (set == null)
		{
			set = new MapBasedSet<IField>(new IdentityHashMap<>());
			map.put(className, set);
		}
		
		return set.add(field);
	}
	
	@Override
	public void check(ICodePosition position, MarkerList markers)
	{
		map.clear();
	}
	
	@Override
	public void cleanup(IContext context, IClassCompilableList compilableList)
	{
		if (addToMap(this.className, this.field))
		{
			compilableList.addCompilable(this);
			this.isUnique = true;
		}
	}
	
	private String getRefFieldName()
	{
		if (this.idFieldName != null)
		{
			return this.idFieldName;
		}
		
		return this.idFieldName = "$fieldRef$" + this.className.replace('/', '$') + "$" + this.field.getName().qualified;
	}
	
	private String getRefFieldType()
	{
		if (this.refFieldType != null)
		{
			return this.refFieldType;
		}
		
		return this.refFieldType = 'L' + Types.getInternalRef(this.field.getType(), "") + ';';
	}
	
	@Override
	public void setInnerIndex(String internalName, int index)
	{
		this.className = internalName;
	}
	
	@Override
	public void write(ClassWriter writer) throws BytecodeException
	{
		if (!this.isUnique)
		{
			return;
		}
		
		String refFieldName = this.getRefFieldName();
		String refFieldType = this.getRefFieldType();
		
		final int modifiers = Modifiers.PRIVATE | Modifiers.STATIC | Modifiers.SYNTHETIC;
		writer.visitField(modifiers, refFieldName, refFieldType, null, null);
	}
	
	@Override
	public void writeStaticInit(MethodWriter writer) throws BytecodeException
	{
		if (!this.isUnique)
		{
			return;
		}
		
		String fieldName = this.field.getName().qualified;
		String fieldClassType = 'L' + this.field.getTheClass().getInternalName() + ';';
		
		String refFieldName = this.getRefFieldName();
		String refFieldType = this.getRefFieldType();
		String wrapperClass = Types.getInternalRef(this.field.getType(), "unsafe/Unsafe");
		
		int var = writer.localCount();
		
		// Create a new wrapper object
		writer.writeTypeInsn(Opcodes.NEW, wrapperClass);
		writer.writeInsn(Opcodes.DUP);
		
		// Get the Unsafe and store it in a local variable
		writer.writeFieldInsn(Opcodes.GETSTATIC, "dyvil/reflect/ReflectUtils", "unsafe", "Lsun/misc/Unsafe;");
		writer.writeInsn(Opcodes.DUP);
		writer.writeVarInsn(Opcodes.ASTORE, var);
		
		// Get the Class of the field container type
		writer.writeLDC(dyvil.tools.asm.Type.getType(fieldClassType));
		writer.writeLDC(fieldName);
		// Get the Field using reflection
		writer.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;", false);
		// Store it in a second local variable
		writer.writeInsn(Opcodes.DUP);
		writer.writeVarInsn(Opcodes.ASTORE, var + 1);
		
		// Get the field base
		writer.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, "sun/misc/Unsafe", "staticFieldBase", "(Ljava/lang/reflect/Field;)Ljava/lang/Object;", false);
		
		// Get the field offset
		writer.writeVarInsn(Opcodes.ALOAD, var);
		writer.writeVarInsn(Opcodes.ALOAD, var + 1);
		writer.writeInvokeInsn(Opcodes.INVOKEVIRTUAL, "sun/misc/Unsafe", "staticFieldOffset", "(Ljava/lang/reflect/Field;)J", false);
		
		// Init the wrapper object
		writer.writeInvokeInsn(Opcodes.INVOKESPECIAL, wrapperClass, "<init>", "(Ljava/lang/Object;J)V", false);
		
		// Assign the reference field
		writer.writeFieldInsn(Opcodes.PUTSTATIC, this.className, refFieldName, refFieldType);
		
		writer.resetLocals(var);
	}
	
	@Override
	public void writeReference(MethodWriter writer) throws BytecodeException
	{
		if (this.field.hasModifier(Modifiers.STATIC))
		{
			writer.writeFieldInsn(Opcodes.GETSTATIC, this.className, this.getRefFieldName(), this.getRefFieldType());
		}
	}
}
