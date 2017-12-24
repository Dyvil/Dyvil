package dyvilx.tools.compiler.ast.field;

import dyvil.annotation.internal.NonNull;
import dyvil.lang.Name;
import dyvil.reflect.Opcodes;
import dyvil.source.position.SourcePosition;
import dyvilx.tools.compiler.ast.attribute.AttributeList;
import dyvilx.tools.compiler.ast.classes.IClass;
import dyvilx.tools.compiler.ast.constructor.IConstructor;
import dyvilx.tools.compiler.ast.context.IContext;
import dyvilx.tools.compiler.ast.expression.IValue;
import dyvilx.tools.compiler.ast.header.IClassCompilableList;
import dyvilx.tools.compiler.ast.header.ICompilableList;
import dyvilx.tools.compiler.ast.member.Member;
import dyvilx.tools.compiler.ast.reference.ReferenceType;
import dyvilx.tools.compiler.ast.type.IType;
import dyvilx.tools.compiler.ast.type.builtin.Types;
import dyvilx.tools.compiler.backend.MethodWriter;
import dyvilx.tools.compiler.backend.exception.BytecodeException;
import dyvilx.tools.compiler.config.Formatting;
import dyvilx.tools.compiler.transform.Names;
import dyvilx.tools.compiler.transform.TypeChecker;
import dyvilx.tools.compiler.util.Markers;
import dyvilx.tools.parsing.marker.MarkerList;

import java.lang.annotation.ElementType;

public class Variable extends Member implements IVariable
{
	protected int    localIndex;
	protected IValue value;

	// Metadata
	protected IType refType;

	/**
	 * Marks if this variable is assigned anywhere. This is used to check if it is effectively final.
	 */
	protected boolean assigned;

	public Variable()
	{
	}

	public Variable(Name name, IType type)
	{
		this.name = name;
		this.type = type;
	}

	public Variable(Name name, IType type, IValue value)
	{
		this.name = name;
		this.type = type;
		this.value = value;
	}

	public Variable(SourcePosition position)
	{
		this.position = position;
	}

	public Variable(SourcePosition position, IType type)
	{
		this.position = position;
		this.type = type;
	}

	public Variable(SourcePosition position, Name name, IType type)
	{
		this.position = position;
		this.name = name;
		this.type = type;
	}

	public Variable(SourcePosition position, Name name, IType type, AttributeList attributes)
	{
		super(position, name, type, attributes);
	}

	@Override
	public ElementType getElementType()
	{
		return ElementType.LOCAL_VARIABLE;
	}

	@Override
	public IValue getValue()
	{
		return this.value;
	}

	@Override
	public void setValue(IValue value)
	{
		this.value = value;
	}

	@Override
	public int getLocalIndex()
	{
		return this.localIndex;
	}

	@Override
	public void setLocalIndex(int index)
	{
		this.localIndex = index;
	}

	@Override
	public boolean isAssigned()
	{
		return this.assigned;
	}

	@Override
	public IValue checkAccess(MarkerList markers, SourcePosition position, IValue receiver, IContext context)
	{
		return receiver;
	}

	@Override
	public IValue checkAssign(MarkerList markers, IContext context, SourcePosition position, IValue receiver,
		                         IValue newValue)
	{
		this.assigned = true;
		return IVariable.super.checkAssign(markers, context, position, receiver, newValue);
	}

	@Override
	public IType getReferenceType()
	{
		return this.refType;
	}

	@Override
	public boolean setReferenceType()
	{
		if (this.refType == null)
		{
			this.refType = this.type.getSimpleRefType();
		}
		return true;
	}

	@Override
	public IType getInternalType()
	{
		return this.refType == null ? this.type : this.refType;
	}

	@Override
	public void resolveTypes(MarkerList markers, IContext context)
	{
		super.resolveTypes(markers, context);

		if (this.value != null)
		{
			this.value.resolveTypes(markers, context);
		}
	}

	@Override
	public void resolve(MarkerList markers, IContext context)
	{
		super.resolve(markers, context);

		if (this.value == null)
		{
			return;
		}

		this.value = this.value.resolve(markers, context);

		boolean inferType = false;
		if (this.type == Types.UNKNOWN)
		{
			inferType = true;
			this.type = this.value.getType();
			if (this.type == Types.UNKNOWN && this.value.isResolved())
			{
				markers.add(Markers.semantic(this.position, "variable.type.infer", this.name.unqualified));
				this.type = Types.ANY;
			}
		}

		final TypeChecker.MarkerSupplier markerSupplier = TypeChecker.markerSupplier("variable.type.incompatible",
		                                                                             "variable.type", "value.type",
		                                                                             this.name);
		this.value = TypeChecker.convertValue(this.value, this.type, this.type, markers, context, markerSupplier);

		if (inferType)
		{
			this.type = this.value.getType();
		}
	}

	@Override
	public void checkTypes(MarkerList markers, IContext context)
	{
		super.checkTypes(markers, context);

		if (this.value != null)
		{
			this.value.checkTypes(markers, context);
		}
	}

	@Override
	public void check(MarkerList markers, IContext context)
	{
		super.check(markers, context);

		if (this.value != null)
		{
			this.value.check(markers, context);
		}

		if (Types.isVoid(this.type))
		{
			markers.add(Markers.semantic(this.position, "variable.type.void"));
		}
	}

	@Override
	public void foldConstants()
	{
		super.foldConstants();

		if (this.value != null)
		{
			this.value = this.value.foldConstants();
		}
	}

	@Override
	public void cleanup(ICompilableList compilableList, IClassCompilableList classCompilableList)
	{
		super.cleanup(compilableList, classCompilableList);

		if (this.value != null)
		{
			this.value = this.value.cleanup(compilableList, classCompilableList);
		}
	}

	@Override
	public void writeInit(MethodWriter writer, IValue value) throws BytecodeException
	{
		if (this.refType != null)
		{
			writeRefInit(this, writer, value);
			return;
		}

		if (value != null)
		{
			value.writeExpression(writer, this.type);
		}

		this.localIndex = writer.localCount();
		writer.visitVarInsn(this.type.getStoreOpcode(), this.localIndex);
		writer.setLocalType(this.localIndex, this.type.getFrameType());
	}

	public static void writeRefInit(IVariable variable, MethodWriter writer, IValue value)
	{
		final IType type = variable.getType();
		final IType refType = variable.getReferenceType();

		final IConstructor constructor = refType.getTheClass().getBody().getConstructor(0);
		writer.visitTypeInsn(Opcodes.NEW, refType.getInternalName());

		if (value != null)
		{
			// new
			writer.visitInsn(Opcodes.DUP);
			// new, new
			value.writeExpression(writer, type);
			// new, new, value
		}
		else
		{
			// value, new
			writer.visitInsn(Opcodes.AUTO_DUP_X1);
			// new, value, new
			writer.visitInsn(Opcodes.AUTO_SWAP);
			// new, new, value
		}

		constructor.writeInvoke(writer, variable.lineNumber());

		final int localIndex = writer.localCount();
		variable.setLocalIndex(localIndex);

		writer.setLocalType(localIndex, refType.getInternalName());
		writer.visitVarInsn(Opcodes.ASTORE, localIndex);
	}

	@Override
	public void writeGet_Get(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		if (this.refType != null)
		{
			writer.visitVarInsn(Opcodes.ALOAD, this.localIndex);
			return;
		}
		writer.visitVarInsn(this.type.getLoadOpcode(), this.localIndex);
	}

	@Override
	public void writeGet_Unwrap(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		if (this.refType != null)
		{
			final IClass refClass = this.refType.getTheClass();
			final IDataMember refField = refClass.resolveField(Names.value);
			refField.writeGet_Get(writer, lineNumber);

			if (refClass == ReferenceType.LazyFields.OBJECT_SIMPLE_REF_CLASS)
			{
				Types.OBJECT.writeCast(writer, this.type, lineNumber);
			}
		}
	}

	@Override
	public boolean writeSet_PreValue(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		if (this.refType != null)
		{
			writer.visitVarInsn(Opcodes.ALOAD, this.localIndex);
			return true;
		}
		return false;
	}

	@Override
	public void writeSet_Wrap(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		if (this.refType != null)
		{
			final IDataMember refField = this.refType.getTheClass().resolveField(Names.value);
			refField.writeSet_Set(writer, lineNumber);
		}
	}

	@Override
	public void writeSet_Set(MethodWriter writer, int lineNumber) throws BytecodeException
	{
		if (this.refType == null)
		{
			writer.visitVarInsn(this.type.getStoreOpcode(), this.localIndex);
		}
	}

	@Override
	public void toString(@NonNull String indent, @NonNull StringBuilder buffer)
	{
		super.toString(indent, buffer);
		IDataMember.toString(indent, buffer, this, "variable.type_ascription");

		if (this.value != null)
		{
			Formatting.appendSeparator(buffer, "field.assignment", '=');
			this.value.toString(indent, buffer);
		}
	}
}
