/* SAAF: A static analyzer for APK files.
 * Copyright (C) 2013  syssec.rub.de
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.rub.syssec.saaf.application.instructions;

import java.util.Arrays;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.misc.ByteUtils;
import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.application.BasicBlockInterface;
import de.rub.syssec.saaf.model.application.CodeLineInterface;
import de.rub.syssec.saaf.model.application.ConstantInterface;
import de.rub.syssec.saaf.model.application.SyntaxException;
import de.rub.syssec.saaf.model.application.instruction.InstructionInterface;
import de.rub.syssec.saaf.model.application.instruction.InstructionType;

/**
 * This class holds information about the SMALI opcodes which are parsed from
 * the SMALI files.
 * 
 * @author Johannes Hoffmann <johannes.hoffmann@rub.de>
 * 
 */
public class Instruction implements InstructionInterface {

	private static final boolean DEBUG=Boolean.parseBoolean(System.getProperty("debug.slicing","false"));
	private final CodeLineInterface codeLine;
	private byte[] opCode = null;

	private InstructionType type = InstructionType.NOT_YET_PARSED;

	/**
	 * The register where the result of the operation is located, may be null.
	 */
	private byte[] resultRegister = null;

	/**
	 * The field where the result of the operation is located, my be null. The
	 * first parameter is class name, the second one the field name.
	 */
	private byte[][] resultField = null; // TODO: use this to directly linkt to Field.class?

	/**
	 * The involved registers in this operation, eg, when calling a method.
	 */
	private LinkedList<byte[]> involvedRegisters = new LinkedList<byte[]>();

	/**
	 * The involved fields in this operation, eg, when copying a variable into a
	 * register.
	 */
	private LinkedList<byte[]> involvedFields = new LinkedList<byte[]>();

	/**
	 * The class, method and parameters of invoke opcodes. cmpr[0] is the class,
	 * cmpr[1] the method, cmpr[2] the raw parameters and cmpr[3] the return value.
	 */
	private byte[][] cmpr = null;

	/**
	 * This is the value which gets assigned by the const-x opcodes, the
	 * constant which is involved in some binary math opcode or the values
	 * during array initialization. May be null.
	 */
	private ConstantInterface constant = null;
	/**
	 * Denotes whether this Instructions holds a constant
	 */
	private boolean hasConstant = false;

	/**
	 * Denotes the label or some opcode that indicates where to jump to, eg,
	 * fill-array-data.
	 */
	private byte[] label = null;

	private static final Logger LOGGER = Logger.getLogger(Instruction.class);

	public Instruction(CodeLineInterface codeLine) {
		this.codeLine = codeLine;
		if (codeLine.isEmpty()) {
			type = InstructionType.EMPTY_LINE;
		} else if (codeLine.startsWith(new byte[] { '.' })) {
			type = InstructionType.SMALI_DOT_COMMENT;
		} else if (codeLine.startsWith(new byte[] { ':' })) {
			type = InstructionType.LABEL;
		} else if (codeLine.startsWith(new byte[] { '#' })) {
			type = InstructionType.SMALI_HASH_KEY_COMMENT;
		} else { // a shortcut, opcodes should begin with a lowercase letter
			byte firstByte = codeLine.getLine()[0]; // cannot be empty, see first check
			if (firstByte < 97 || firstByte > 122) { // a and z
				type = InstructionType.UNKNOWN;
			}
		}
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.instructions.InstructionInterface#parseOpCode()
	 */
	@Override
	public void parseOpCode() {

		// Let us define the type of the opcode if we do not already know it is
		// no opcode at all
		if (!(type == InstructionType.EMPTY_LINE
				|| type == InstructionType.SMALI_DOT_COMMENT
				|| type == InstructionType.LABEL
				|| type == InstructionType.SMALI_HASH_KEY_COMMENT || type == InstructionType.UNKNOWN 
		// do not ask the map if we know it does not begin w/ a lowercase letter
		)) {
			LinkedList<byte[]> split = split(codeLine.getLine());
			opCode = split.getFirst();
			type = InstructionMap.getType(opCode);
			// Now let us parse the opcode if it is a opcode that we know of
			if (!(type == InstructionType.UNKNOWN
					|| type == InstructionType.EMPTY_LINE
					|| type == InstructionType.SMALI_DOT_COMMENT
					|| type == InstructionType.LABEL || type == InstructionType.SMALI_HASH_KEY_COMMENT)) {
				parse(split);
			}
		}
	}

	/**
	 * Split a byte[] at ' ' and ',' but do not split between { } and " " ('{',
	 * '}', ',' and ' ' inside quotes are ignored);
	 * 
	 * @return the byte arrays between the above signs, but without them!
	 */
	public static LinkedList<byte[]> split(byte[] input) {
		// if (Config.DBG_SLICING) LOGGER.debug("split: '"+new String(input)+"'");
		LinkedList<byte[]> list = new LinkedList<byte[]>();
		int lastIndex = 0;
		boolean inQuotes = false;
		boolean inKlammer = false;
		boolean copyLastSequence = true;
		/*
		 * Used for special cases like .local v15, list:Ljava/util/Map;,
		 * "Ljava/util/Map<Ljava/lang/String;Ljava/lang/Object;>;" __ ___ We
		 * need to ignore the underlined quotes.
		 */
		boolean skipNextQuote = false;

		for (int i = 0; i < input.length; i++) {
			switch (input[i]) {
			case ' ':
				if (!inQuotes && !inKlammer) { // split it
					if (lastIndex != i)
						list.addLast(ByteUtils.subbytes(input, lastIndex, i));
					lastIndex = i + 1; // do not copy ' ' the next time
				} else {
					// do nothing
				}
				break;

			case ',': // same as ' '
				if (!inQuotes && !inKlammer) { // split it
					/*
					 * Dirty workaround for lines like .local v15,
					 * list:Ljava/util
					 * /Map;,"Ljava/util/Map<Ljava/lang/String;Ljava/lang/Object;>;"
					 * If this is not checked, ___ this would be splitted!, but
					 * the last split should occur before list:Ljave/util....
					 */
					if ((i + 1 < input.length) && input[i + 1] == '"'
							&& (i - 1 >= 0) && input[i - 1] == ';') {
						// first checks are for array boundaries
						break; // do not split here
					}
					if (lastIndex != i)
						list.addLast(ByteUtils.subbytes(input, lastIndex, i));
					lastIndex = i + 1; // do not copy ' ' the next time
					if (i == input.length - 1)
						copyLastSequence = false; // reached the end
				} else {
					// do nothing
				}
				break;

			case '{':
				if (!inQuotes && !inKlammer) { // opening {, therefore aggregate
												// everything between, " "
												// should always be previous
												// char
					inKlammer = true;
					lastIndex = i + 1; // do not copy { the next time something
										// is copied
				} else if (!inQuotes && inKlammer) {
					// break, this should not happen?!
					LOGGER.error("Split CL: Found { although another { was found!");
				}
				break;

			case '}':
				if (inKlammer && !inQuotes) { // found closing }
					// copy all except { and }
					list.addLast(ByteUtils.subbytes(input, lastIndex, i));
					lastIndex = i + 1; // do not copy } the next time something
										// is copied
					inKlammer = false;
					if (i == input.length - 1)
						copyLastSequence = false; // reached the end
				} else if (!inKlammer && !inQuotes) {
					// break, this should not happen?!
					LOGGER.error("Split CL: Found } although !inQuotes && !inKlammer");
				}
				break;

			case '"':
				/*
				 * The two IFs are a workaround for lines like .local v15,
				 * list:Ljava/util/Map;,
				 * "Ljava/util/Map<Ljava/lang/String;Ljava/lang/Object;>;" The
				 * last split should occur before list:Ljave/util.... See
				 * skipNextQuote note.
				 */
				if (skipNextQuote) {
					skipNextQuote = false; // Consider next quote again
					break;
				}
				if (!inKlammer && !inQuotes) { // beginning quotes
					if ((i - 2 >= 0) && input[i - 1] == ','
							&& input[i - 2] == ';') { // first check is for
														// array boundaries
						skipNextQuote = true;
						break; // do not split here AND ignore next '"'
					}
					lastIndex = i;
					inQuotes = true;
				} else if (!inKlammer && inQuotes) {
					if (input[i - 1] == '\\')
						continue; // ignore, " im String
					else { // quotes end/close
						// copy all except the " at the beginning and end
						list.addLast(ByteUtils.subbytes(input, lastIndex, i + 1));
						lastIndex = i + 1;
						copyLastSequence = false;
						inQuotes = false;
					}
				} else {
					// break, this should not happen?!
					LOGGER.error("Split CL: Found unexpected \"!");
				}
				break;

			default:
				// found a normal sign :)
				continue;
			}
		}
		// check if >= 0, otherwise the last element was already copied. this is
		// only relevant if the last part is a "xyz"
		if (copyLastSequence) {
			// copy last or only the one element
			list.addLast(ByteUtils.subbytes(input, lastIndex, input.length));
		}
		// for (byte[] bb : list) {
		// System.out.println(" ] = "+new String(bb));
		// }
		return list;
	}

	/**
	 * Parse something like {v0 .. v5}, {v7} or {v7, v8} or even "v1 v2 v3". '{'
	 * and '}' are optional, but may only occur in a single pair.
	 * 
	 * @param parameters
	 *            the byte array as described above
	 * @return a list w/ all the registers
	 */
	private static LinkedList<byte[]> parseParameter(byte[] parameters) {
		// LOGGER.logDebug(Instruction.class, "parseParameter: '"+new
		// String(parameters)+"'");
		LinkedList<byte[]> result = new LinkedList<byte[]>();
		if (parameters == null || parameters.length == 0) {
			if (DEBUG) LOGGER.debug("parseParameter: empty parameters detected.");
			return result;
		} else if (parameters[0] == '{'
				&& parameters[parameters.length - 1] == '}') { // sanity check
			// strip the '{' and '}'
			parameters = ByteUtils.subbytes(parameters, 1,
					parameters.length - 1);
		}
		int lastIndex = 0;
		if (ByteUtils.contains(parameters, ',')) { // eg {v7, v8}
			boolean found = true;
			for (int i = 0; i < parameters.length; i++) {
				if (found) {
					if (parameters[i] != ',') {
						continue;
					} else {
						found = false;
						result.addLast(ByteUtils.subbytes(parameters,
								lastIndex, i));
					}
				} else { // currently no register is read
					if (parameters[i] != ' ') {
						found = true;
						lastIndex = i;
					} else { // found ' ', do nothing
						continue;
					}
				}
			}
			// copy last (or only one) register
			result.add(ByteUtils.subbytes(parameters, lastIndex,
					parameters.length));
		} else if (ByteUtils.contains(parameters, '.')) { // eg {v0 .. v5}
			byte[] vA = null;
			byte[] vB = null;
			boolean found = true;
			for (int i = 0; i < parameters.length; i++) {
				if (found) {
					if (parameters[i] != ' ') {
						continue;
					} else {
						found = false;
						// lastIndex+1: cut the v from eg v12
						vA = ByteUtils.subbytes(parameters, lastIndex + 1, i);
					}
				} else { // currently no register is read
					if (parameters[i] != ' ' && parameters[i] != '.') {
						found = true;
						lastIndex = i;
					} else { // found ' ', do nothing
						continue;
					}
				}
			}
			// copy last (or only one) register
			vB = ByteUtils.subbytes(parameters, lastIndex + 1,
					parameters.length); // lastIndex+1: cut the v from eg v12.
			// now "create" all intermediate registers and put them into the
			// list
			int fromReg = Integer.parseInt(new String(vA));
			int toReg = Integer.parseInt(new String(vB));
			while (fromReg <= toReg) {
				result.addLast(("v" + fromReg).getBytes());
				fromReg++;
			}
		} else { // eg {v7}
			result.add(parameters);
		}
		return result;
	}

	/**
	 * This method sets everything up, it has to be called in the constructor!
	 * ref:
	 * http://www.milk.com/kodebase/dalvik-docs-mirror/docs/dalvik-bytecode.html
	 * 
	 * @param codeLine
	 * @throws UnknownOpCodeException
	 */
	private void parse(LinkedList<byte[]> split) {
		byte[] opCodeLine = codeLine.getLine();
		switch (type) {

		case NEW_INSTANCE:
			// new-instance vAA, type@BBBB
			resultRegister = parseParameter(split.get(1)).getFirst();
			break;

		case INVOKE_STATIC: // same as INVOKE
		case INVOKE:
			// invoke-virtual/range {v0 .. v5},
			// Landroid/content/ContentResolver;->query(Landroid/net/Uri;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;
			// invoke-interface {v7}, Landroid/database/Cursor;->moveToNext()Z
			// invoke-virtual {v7},
			// Ljava/io/PrintStream;->println(Ljava/lang/String;)V
			// Must not always have a move-result
			// split: 0=opcode, 1=registers, 2=class->method(types)returnType
			involvedRegisters = parseParameter(split.get(1));
			// now parse the class and the method which is called
			cmpr = parseClassAndMethodAndParameterAndReturnValue(split.getLast());
			break;

		case AGET:
			/**
			 * aget-object v0, v0, v1
			 * arrayop vAA, vBB, vCC
			 * Store data from array vBB at index vCC into vAA
			 * 
			 * The array index (vC) is ignored
			 */
			resultRegister = split.get(1); // vA
			involvedRegisters.add(split.get(2)); // vB
			break;

		case GET:
			/**
			 * iinstanceop vA, vB, field@CCCC iget-x sstaticop vAA, field@BBBB
			 * sget-x sget-object v1,
			 * Lcom/andiord/SMSOperator;->CONTENT_URI:Landroid/net/Uri;
			 */
			if (opCodeLine[0] == 'i') { // instance-op
				resultRegister = split.get(1); // vA
				involvedRegisters.add(split.get(2)); // vB
				involvedFields.add(split.get(3));
			} else if (opCodeLine[0] == 's') { // static-op
				resultRegister = split.get(1); // vA
				involvedFields.add(split.get(2)); // field
			}
			break;

		case CONST:
			// const-string v2, ", protocol="
			// const/4 v4, 0x0
			resultRegister = split.get(1); // vA
			hasConstant = true;
			break;

		case APUT:
			/**
			 * Put data from vAA into the array vBB at index vCC arrayop vAA,
			 * vBB, vCC aput-x
			 * 
			 * We do not care about the array index (vC) right now
			 */
			resultRegister = split.get(2); // vB
			involvedRegisters.add(split.get(1)); // vA
			break;

		case PUT:
			/**
			 * Save vA in field CCCC of Object vB iinstanceop vA, vB, field@CCCC
			 * iput-x sstaticop vAA, field@BBBB sput-x sput v0,
			 * Lcom/lohan/crackme1/example;->Counter:I
			 */
			if (opCodeLine[0] == 'i') { // instance-op
				resultField = parseClassAndField(split.get(3)); // field C
				involvedRegisters.add(split.get(1)); // vA
				// vB is the reference to the object of field C
			} else if (opCodeLine[0] == 's') { // static-op
				resultField = parseClassAndField(split.get(2)); // Field
				involvedRegisters.add(split.get(1)); // vA
			}
			break;

		case MATH_1: // unary operations
			/*
			 * unop vA, vB
			 * eg: neg-int, int-to-byte etc
			 */
			resultRegister = split.get(1);
			involvedRegisters.add(split.get(2));
			break;

		case MATH_2: // binary operations solely on registers
			/*
			 * binop vAA, vBB, vCC
			 * eg: add-int, or-int, add-int/2addr etc
			 */
			resultRegister = split.get(1);
			involvedRegisters.add(split.get(2));
			involvedRegisters.add(split.get(3));
			break;

		case MATH_2C: // binary operations on a register and a constant
			/*
			 * binop/lit16 vA, vB, #+CCCC
			 * binop/lit8 vAA, vBB, #+CC
			 */
			resultRegister = split.get(1);
			involvedRegisters.add(split.get(2));
			hasConstant = true;
			break;

		case MOVE:
			/*
			 * move-object vA, vB
			 * Move content from vB into vA.
			 * 
			 * move-wide/16 vAAAA, vBBBB <- These are pairs,
			 * but are only written as eg vX, which means vX and vX+1.
			 * The bytecode interpreter knows that vX and vX+1 are
			 * paired and will access them accordingly if, eg, a long
			 * value is accessed.
			 * 
			 * Do not handle move-exception and move-result here!
			 */
			resultRegister = split.get(1); // vA
			involvedRegisters.add(split.get(2)); // vB
			break;

		case MOVE_RESULT:
			/*
			 * move-result vAA This opcode has either a leading INVOKE or a
			 * leading FILLED_NEW_ARRAY instruction
			 */
			resultRegister = split.get(1);
			break;

		case RETURN:
			// return-void, return vAA, return-wide vAA, return-object vAA
			if (!Arrays.equals(split.getFirst(), "return-void".getBytes())) {
				// return  void has not return value, therefore we're done
				involvedRegisters.add(split.getLast());
			}
			break;

		case NEW_ARRAY:
			/*
			 * new-array vA, vB, type@CCCC vA = array-reference vB = size
			 * ignored, as are the indexes CC = type (eg String)
			 */
			resultRegister = split.get(1);
			break;

		case FILL_ARRAY_DATA:
			/*
			 * byte b[] = { 'x', 'y', 'z'}; ergibt:
			 * 
			 * fill-array-data v0, :array_0
			 * 
			 * .. ..
			 * 
			 * .line 91 return-void .line 88 nop
			 * 
			 * :array_0 .array-data 0x1 0x78t 0x79t 0x7at .end array-data .end
			 * method
			 */
			resultRegister = split.get(1);
			label = split.get(2);
			hasConstant = true;
			break;

		case FILLED_NEW_ARRAY:
			/*
			 * filled-new-array {vD, vE, vF, vG, vA}, type@CCCC
			 * filled-new-array/range {vCCCC .. vNNNN}, type@BBBB
			 * 
			 * This instruction is followed by a MOVE_RESULT instruction
			 */
			involvedRegisters = parseParameter(split.get(1));
			break;

		case GOTO:
			label = split.getLast();
			break;
		case SWITCH:
			// TODO: still need to parse the involved register(s)?
			label = split.getLast();
			break;
		case JMP: // if-nez v0, :cond_0
//			if (Config.DBG_SLICING) LOGGER.debug("TODO: Did not parse instruction of type " + type
//					+ ": " + new String(opCode));
			// TODO: still need to parse the involved register(s)?
			label = split.getLast();
			break;

		case INTERNAL_SMALI_OPCODE:
			/*
			 * If the resultRegister overwrites our tracked register, we have to
			 * stop. The following opcodes are relevant: cmpX vAA, vBB, vCC
			 * (destination, 1st src register, 2nd src register) move-exception
			 * vAA vAA is the register to where the exception caught is moved
			 * array-length vA, vB (destination, array reference register)
			 */
			resultRegister = split.get(1);
			break;

		case IGNORE:
			break;

		default:
			if (DEBUG) LOGGER.debug("Did not parse instruction of type " + type + ": "
					+ new String(opCode));
			break;
		}
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.instructions.InstructionInterface#getType()
	 */
	@Override
	public InstructionType getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.instructions.InstructionInterface#getOpCode()
	 */
	@Override
	public byte[] getOpCode() {
		return opCode;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.instructions.InstructionInterface#getResultRegister()
	 */
	@Override
	public byte[] getResultRegister() {
		return resultRegister;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.instructions.InstructionInterface#getResultField()
	 */
	@Override
	public byte[][] getResultField() {
		return resultField;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.instructions.InstructionInterface#getInvolvedRegisters()
	 */
	@Override
	public LinkedList<byte[]> getInvolvedRegisters() {
		return involvedRegisters;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.instructions.InstructionInterface#getInvolvedFields()
	 */
	@Override
	public LinkedList<byte[]> getInvolvedFields() {
		return involvedFields;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.instructions.InstructionInterface#getCalledClassAndMethodWithParameter()
	 */
	@Override
	public byte[][] getCalledClassAndMethodWithParameter() {
		return cmpr;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.instructions.InstructionInterface#getCalledClassAndMethod()
	 */
	@Override
	public byte[][] getCalledClassAndMethod() {
		return getCalledClassAndMethodWithParameter();
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.instructions.InstructionInterface#dump()
	 */
	@Override
	public void dump() {
		StringBuilder sb = new StringBuilder();
		sb.append("Type: " + type);
		sb.append(" CL  : " + codeLine);
		if (resultRegister != null)
			sb.append(" resultReg: " + new String(resultRegister));
		if (resultField != null)
			sb.append("resultFld: " + new String(resultField[1]) + "."
					+ new String(resultField[1]));
		if (involvedRegisters.size() > 0) {
			sb.append(" invlvdReg: ");
			for (byte[] b : involvedRegisters) {
				sb.append(new String(b));
				sb.append(" ");
			}
		}
		if (involvedFields.size() > 0)
		{
			sb.append(" invlvdFld: ");
			for (byte[] b : involvedFields)
			{
				sb.append(new String(b));
				sb.append(" ");
			}
		}

		if (constant != null)
			sb.append("    const: " + constant);
		if (cmpr != null)
			sb.append(" targetMet: " + new String(cmpr[0]) + "."
					+ new String(cmpr[1]) + "(...)");
		if (DEBUG) LOGGER.debug(sb.toString());
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.instructions.InstructionInterface#getCodeLine()
	 */
	@Override
	public CodeLineInterface getCodeLine() {
		return codeLine;
	}

	/**
	 * Parse the class and the field from a line like this:
	 * Lcom/lohan/crackme1/example;->Counter:I This example returns
	 * [com/lohan/crackme1/example, Counter].
	 * 
	 * @param smaliCode
	 *            see above
	 * @return an array with the class being the first element and the fieldname
	 *         the second one, the type is dropped
	 */
	public static byte[][] parseClassAndField(byte[] smaliCode) {
		byte[][] cf = new byte[2][];
		int classEnd = ByteUtils.indexOf(smaliCode, ';');
		int varName = ByteUtils.indexOf(smaliCode, ':');
		cf[0] = ByteUtils.subbytes(smaliCode, 1, classEnd);
		cf[1] = ByteUtils.subbytes(smaliCode, classEnd + 3, varName);
		return cf;
	}

	/**
	 * Parse the class,the method and its parameters from a line like 1)
	 * Ljava/io/PrintStream;->println(Ljava/lang/String;)V would return [
	 * java/io/PrintStream , println, Ljava/lang/String; ] 2)
	 * code=[B->clone()Ljava/lang/Object; would return [ B , clone, '' ]
	 * 
	 * @param smaliCode
	 *            see above
	 * @return an array with the class being the first element, the method the
	 *         second one and the parameters the third one
	 */
	public static byte[][] parseClassAndMethodAndParameterAndReturnValue(byte[] smaliCode) {
		byte[][] cmpr = new byte[4][];
		int dashPos = ByteUtils.indexOf(smaliCode, '-');
		int classEndOffset = 0;
		if (smaliCode[dashPos - 1] == ';')
			classEndOffset = 1; // if the class it not primitive is terminated
								// with a ';', but we do not want to copy it
		int methodEnd = ByteUtils.indexOf(smaliCode, '(');
		int parametersEnd = ByteUtils.indexOf(smaliCode, ')');
		int offset = 0;
		for (byte b : smaliCode) { // read array dimension: [
			if (b == '[')
				offset++;
			else
				break;
		}
		if (smaliCode[offset] == 'L') {
			offset++; // we have a class an want to also skip the L
		}
		cmpr[0] = ByteUtils
				.subbytes(smaliCode, offset, dashPos - classEndOffset); // class
		cmpr[1] = ByteUtils.subbytes(smaliCode, dashPos + 2, methodEnd); // method
		cmpr[2] = ByteUtils.subbytes(smaliCode, methodEnd + 1, parametersEnd); // parameters
		cmpr[3] = ByteUtils.subbytes(smaliCode, parametersEnd + 1); // return value
		return cmpr;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.instructions.InstructionInterface#getLabel()
	 */
	@Override
	public byte[] getLabel() {
		return label;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.instructions.InstructionInterface#hasConstant()
	 */
	@Override
	public final boolean hasConstant() {
		return hasConstant;
	}

	/* (non-Javadoc)
	 * @see de.rub.syssec.saaf.application.instructions.InstructionInterface#getConstantValue()
	 */
	@Override
	public String getConstantValue() throws SyntaxException {
		if (!hasConstant) return null;
		// this is only a temp constant
		ConstantInterface c = new Constant(codeLine, -1, new LinkedList<BasicBlockInterface>(), -1);
		return c.getValue();
	}
}
