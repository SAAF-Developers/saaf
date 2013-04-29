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

import java.util.HashMap;

import org.apache.log4j.Logger;

import de.rub.syssec.saaf.misc.config.Config;
import de.rub.syssec.saaf.model.application.InstructionType;

/**
 * A small helper class which assigns a type to a SAMLI opcode.
 * 
 * @author Johannes Hoffmann <johannes.hoffmann@rub.de>
 *
 */
public class InstructionMap {
	
	private static final boolean DEBUG=Boolean.parseBoolean(System.getProperty("debug.slicing","false"));
	private static final Logger LOGGER = Logger.getLogger(InstructionMap.class);
	
	private static final HashMap<ByteArrayWrapper, InstructionType> INSTRUCTIONS = new HashMap<ByteArrayWrapper, InstructionType>();
	
	static {
		/* Initialize all the stuff */
		INSTRUCTIONS.put(new ByteArrayWrapper("nop".getBytes()), InstructionType.NOP);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("move".getBytes()), InstructionType.MOVE);
		INSTRUCTIONS.put(new ByteArrayWrapper("move/from16".getBytes()), InstructionType.MOVE);
		INSTRUCTIONS.put(new ByteArrayWrapper("move/16".getBytes()), InstructionType.MOVE);
		INSTRUCTIONS.put(new ByteArrayWrapper("move-wide".getBytes()), InstructionType.MOVE);
		INSTRUCTIONS.put(new ByteArrayWrapper("move-wide/from16".getBytes()), InstructionType.MOVE);
		INSTRUCTIONS.put(new ByteArrayWrapper("move-wide/16".getBytes()), InstructionType.MOVE);
		INSTRUCTIONS.put(new ByteArrayWrapper("move-object".getBytes()), InstructionType.MOVE);
		INSTRUCTIONS.put(new ByteArrayWrapper("move-object/from16".getBytes()), InstructionType.MOVE);
		INSTRUCTIONS.put(new ByteArrayWrapper("move-object/16".getBytes()), InstructionType.MOVE);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("move-result".getBytes()), InstructionType.MOVE_RESULT);
		INSTRUCTIONS.put(new ByteArrayWrapper("move-result-wide".getBytes()), InstructionType.MOVE_RESULT);
		INSTRUCTIONS.put(new ByteArrayWrapper("move-result-object".getBytes()), InstructionType.MOVE_RESULT);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("move-exception".getBytes()), InstructionType.INTERNAL_SMALI_OPCODE);

		INSTRUCTIONS.put(new ByteArrayWrapper("return-void".getBytes()), InstructionType.RETURN);
		INSTRUCTIONS.put(new ByteArrayWrapper("return".getBytes()), InstructionType.RETURN);
		INSTRUCTIONS.put(new ByteArrayWrapper("return-wide".getBytes()), InstructionType.RETURN);
		INSTRUCTIONS.put(new ByteArrayWrapper("return-object".getBytes()), InstructionType.RETURN);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("const/4".getBytes()), InstructionType.CONST);
		INSTRUCTIONS.put(new ByteArrayWrapper("const/16".getBytes()), InstructionType.CONST);
		INSTRUCTIONS.put(new ByteArrayWrapper("const".getBytes()), InstructionType.CONST);
		INSTRUCTIONS.put(new ByteArrayWrapper("const/high16".getBytes()), InstructionType.CONST);
		INSTRUCTIONS.put(new ByteArrayWrapper("const-wide/16".getBytes()), InstructionType.CONST);
		INSTRUCTIONS.put(new ByteArrayWrapper("const-wide/32".getBytes()), InstructionType.CONST);
		INSTRUCTIONS.put(new ByteArrayWrapper("const-wide".getBytes()), InstructionType.CONST);
		INSTRUCTIONS.put(new ByteArrayWrapper("const-wide/high16".getBytes()), InstructionType.CONST);
		INSTRUCTIONS.put(new ByteArrayWrapper("const-string".getBytes()), InstructionType.CONST);
		INSTRUCTIONS.put(new ByteArrayWrapper("const-string/jumbo".getBytes()), InstructionType.CONST);
		INSTRUCTIONS.put(new ByteArrayWrapper("const-class".getBytes()), InstructionType.CONST);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("monitor-enter".getBytes()), InstructionType.IGNORE);
		INSTRUCTIONS.put(new ByteArrayWrapper("monitor-exit".getBytes()), InstructionType.IGNORE);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("check-cast".getBytes()), InstructionType.IGNORE);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("instance-of".getBytes()), InstructionType.IGNORE);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("array-length".getBytes()), InstructionType.INTERNAL_SMALI_OPCODE);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("new-instance".getBytes()), InstructionType.NEW_INSTANCE);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("new-array".getBytes()), InstructionType.NEW_ARRAY);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("filled-new-array".getBytes()), InstructionType.FILLED_NEW_ARRAY);
		INSTRUCTIONS.put(new ByteArrayWrapper("filled-new-array/range".getBytes()), InstructionType.FILLED_NEW_ARRAY);
		INSTRUCTIONS.put(new ByteArrayWrapper("fill-array-data".getBytes()), InstructionType.FILL_ARRAY_DATA);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("throw".getBytes()), InstructionType.IGNORE);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("goto".getBytes()), InstructionType.GOTO);
		INSTRUCTIONS.put(new ByteArrayWrapper("goto/16".getBytes()), InstructionType.GOTO);
		INSTRUCTIONS.put(new ByteArrayWrapper("goto/32".getBytes()), InstructionType.GOTO);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("packed-switch".getBytes()), InstructionType.SWITCH);
		INSTRUCTIONS.put(new ByteArrayWrapper("sparse-switch".getBytes()), InstructionType.SWITCH);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("cmpkind".getBytes()), InstructionType.INTERNAL_SMALI_OPCODE);
		INSTRUCTIONS.put(new ByteArrayWrapper("cmpl-float".getBytes()), InstructionType.INTERNAL_SMALI_OPCODE);
		INSTRUCTIONS.put(new ByteArrayWrapper("cmpg-float".getBytes()), InstructionType.INTERNAL_SMALI_OPCODE);
		INSTRUCTIONS.put(new ByteArrayWrapper("cmpl-double".getBytes()), InstructionType.INTERNAL_SMALI_OPCODE);
		INSTRUCTIONS.put(new ByteArrayWrapper("cmpg-double".getBytes()), InstructionType.INTERNAL_SMALI_OPCODE);
		INSTRUCTIONS.put(new ByteArrayWrapper("cmp-long".getBytes()), InstructionType.INTERNAL_SMALI_OPCODE);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("if-test".getBytes()), InstructionType.JMP);
		INSTRUCTIONS.put(new ByteArrayWrapper("if-eq".getBytes()), InstructionType.JMP);
		INSTRUCTIONS.put(new ByteArrayWrapper("if-ne".getBytes()), InstructionType.JMP);
		INSTRUCTIONS.put(new ByteArrayWrapper("if-lt".getBytes()), InstructionType.JMP);
		INSTRUCTIONS.put(new ByteArrayWrapper("if-ge".getBytes()), InstructionType.JMP);
		INSTRUCTIONS.put(new ByteArrayWrapper("if-gt".getBytes()), InstructionType.JMP);
		INSTRUCTIONS.put(new ByteArrayWrapper("if-le".getBytes()), InstructionType.JMP);
		INSTRUCTIONS.put(new ByteArrayWrapper("if-testz".getBytes()), InstructionType.JMP);
		INSTRUCTIONS.put(new ByteArrayWrapper("if-eqz".getBytes()), InstructionType.JMP);
		INSTRUCTIONS.put(new ByteArrayWrapper("if-nez".getBytes()), InstructionType.JMP);
		INSTRUCTIONS.put(new ByteArrayWrapper("if-ltz".getBytes()), InstructionType.JMP);
		INSTRUCTIONS.put(new ByteArrayWrapper("if-gez".getBytes()), InstructionType.JMP);
		INSTRUCTIONS.put(new ByteArrayWrapper("if-gtz".getBytes()), InstructionType.JMP);
		INSTRUCTIONS.put(new ByteArrayWrapper("if-lez".getBytes()), InstructionType.JMP);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("aget".getBytes()), InstructionType.AGET);
		INSTRUCTIONS.put(new ByteArrayWrapper("aget-wide".getBytes()), InstructionType.AGET);
		INSTRUCTIONS.put(new ByteArrayWrapper("aget-object".getBytes()), InstructionType.AGET);
		INSTRUCTIONS.put(new ByteArrayWrapper("aget-boolean".getBytes()), InstructionType.AGET);
		INSTRUCTIONS.put(new ByteArrayWrapper("aget-byte".getBytes()), InstructionType.AGET);
		INSTRUCTIONS.put(new ByteArrayWrapper("aget-char".getBytes()), InstructionType.AGET);
		INSTRUCTIONS.put(new ByteArrayWrapper("aget-short".getBytes()), InstructionType.AGET);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("aput".getBytes()), InstructionType.APUT);
		INSTRUCTIONS.put(new ByteArrayWrapper("aput-wide".getBytes()), InstructionType.APUT);
		INSTRUCTIONS.put(new ByteArrayWrapper("aput-object".getBytes()), InstructionType.APUT);
		INSTRUCTIONS.put(new ByteArrayWrapper("aput-boolean".getBytes()), InstructionType.APUT);
		INSTRUCTIONS.put(new ByteArrayWrapper("aput-byte".getBytes()), InstructionType.APUT);
		INSTRUCTIONS.put(new ByteArrayWrapper("aput-char".getBytes()), InstructionType.APUT);
		INSTRUCTIONS.put(new ByteArrayWrapper("aput-short".getBytes()), InstructionType.APUT);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("iget".getBytes()), InstructionType.GET);
		INSTRUCTIONS.put(new ByteArrayWrapper("iget-wide".getBytes()), InstructionType.GET);
		INSTRUCTIONS.put(new ByteArrayWrapper("iget-object".getBytes()), InstructionType.GET);
		INSTRUCTIONS.put(new ByteArrayWrapper("iget-boolean".getBytes()), InstructionType.GET);
		INSTRUCTIONS.put(new ByteArrayWrapper("iget-byte".getBytes()), InstructionType.GET);
		INSTRUCTIONS.put(new ByteArrayWrapper("iget-char".getBytes()), InstructionType.GET);
		INSTRUCTIONS.put(new ByteArrayWrapper("iget-short".getBytes()), InstructionType.GET);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("iput".getBytes()), InstructionType.PUT);
		INSTRUCTIONS.put(new ByteArrayWrapper("iput-wide".getBytes()), InstructionType.PUT);
		INSTRUCTIONS.put(new ByteArrayWrapper("iput-object".getBytes()), InstructionType.PUT);
		INSTRUCTIONS.put(new ByteArrayWrapper("iput-boolean".getBytes()), InstructionType.PUT);
		INSTRUCTIONS.put(new ByteArrayWrapper("iput-byte".getBytes()), InstructionType.PUT);
		INSTRUCTIONS.put(new ByteArrayWrapper("iput-char".getBytes()), InstructionType.PUT);
		INSTRUCTIONS.put(new ByteArrayWrapper("iput-short".getBytes()), InstructionType.PUT);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("sget".getBytes()), InstructionType.GET);
		INSTRUCTIONS.put(new ByteArrayWrapper("sget-wide".getBytes()), InstructionType.GET);
		INSTRUCTIONS.put(new ByteArrayWrapper("sget-object".getBytes()), InstructionType.GET);
		INSTRUCTIONS.put(new ByteArrayWrapper("sget-boolean".getBytes()), InstructionType.GET);
		INSTRUCTIONS.put(new ByteArrayWrapper("sget-byte".getBytes()), InstructionType.GET);
		INSTRUCTIONS.put(new ByteArrayWrapper("sget-char".getBytes()), InstructionType.GET);
		INSTRUCTIONS.put(new ByteArrayWrapper("sget-short".getBytes()), InstructionType.GET);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("sput".getBytes()), InstructionType.PUT);
		INSTRUCTIONS.put(new ByteArrayWrapper("sput-wide".getBytes()), InstructionType.PUT);
		INSTRUCTIONS.put(new ByteArrayWrapper("sput-object".getBytes()), InstructionType.PUT);
		INSTRUCTIONS.put(new ByteArrayWrapper("sput-boolean".getBytes()), InstructionType.PUT);
		INSTRUCTIONS.put(new ByteArrayWrapper("sput-byte".getBytes()), InstructionType.PUT);
		INSTRUCTIONS.put(new ByteArrayWrapper("sput-char".getBytes()), InstructionType.PUT);
		INSTRUCTIONS.put(new ByteArrayWrapper("sput-short".getBytes()), InstructionType.PUT);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("invoke-virtual".getBytes()), InstructionType.INVOKE);
		INSTRUCTIONS.put(new ByteArrayWrapper("invoke-super".getBytes()), InstructionType.INVOKE);
		INSTRUCTIONS.put(new ByteArrayWrapper("invoke-direct".getBytes()), InstructionType.INVOKE);
		INSTRUCTIONS.put(new ByteArrayWrapper("invoke-interface".getBytes()), InstructionType.INVOKE);
		INSTRUCTIONS.put(new ByteArrayWrapper("invoke-virtual/range".getBytes()), InstructionType.INVOKE);
		INSTRUCTIONS.put(new ByteArrayWrapper("invoke-super/range".getBytes()), InstructionType.INVOKE);
		INSTRUCTIONS.put(new ByteArrayWrapper("invoke-direct/range".getBytes()), InstructionType.INVOKE);
		INSTRUCTIONS.put(new ByteArrayWrapper("invoke-interface/range".getBytes()), InstructionType.INVOKE);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("invoke-static".getBytes()), InstructionType.INVOKE_STATIC);
		INSTRUCTIONS.put(new ByteArrayWrapper("invoke-static/range".getBytes()), InstructionType.INVOKE_STATIC);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("neg-int".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("not-int".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("neg-long".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("not-long".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("neg-float".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("neg-double".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("int-to-long".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("int-to-float".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("int-to-double".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("long-to-int".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("long-to-float".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("long-to-double".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("float-to-int".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("float-to-long".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("float-to-double".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("double-to-int".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("double-to-long".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("double-to-float".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("int-to-byte".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("int-to-char".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("int-to-short".getBytes()), InstructionType.MATH_1);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("add-int".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("sub-int".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("mul-int".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("div-int".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("rem-int".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("and-int".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("or-int".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("xor-int".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("shl-int".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("shr-int".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("ushr-int".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("add-long".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("sub-long".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("mul-long".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("div-long".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("rem-long".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("and-long".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("or-long".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("xor-long".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("shl-long".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("shr-long".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("ushr-long".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("add-float".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("sub-float".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("mul-float".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("div-float".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("rem-float".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("add-double".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("sub-double".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("mul-double".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("div-double".getBytes()), InstructionType.MATH_2);
		INSTRUCTIONS.put(new ByteArrayWrapper("rem-double".getBytes()), InstructionType.MATH_2);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("add-int/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("sub-int/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("mul-int/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("div-int/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("rem-int/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("and-int/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("or-int/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("xor-int/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("shl-int/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("shr-int/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("ushr-int/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("add-long/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("sub-long/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("mul-long/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("div-long/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("rem-long/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("and-long/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("or-long/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("xor-long/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("shl-long/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("shr-long/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("ushr-long/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("add-float/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("sub-float/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("mul-float/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("div-float/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("rem-float/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("add-double/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("sub-double/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("mul-double/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("div-double/2addr".getBytes()), InstructionType.MATH_1);
		INSTRUCTIONS.put(new ByteArrayWrapper("rem-double/2addr".getBytes()), InstructionType.MATH_1);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("add-int/lit16".getBytes()), InstructionType.MATH_2C);
		INSTRUCTIONS.put(new ByteArrayWrapper("rsub-int".getBytes()), InstructionType.MATH_2C);
		INSTRUCTIONS.put(new ByteArrayWrapper("mul-int/lit16".getBytes()), InstructionType.MATH_2C);
		INSTRUCTIONS.put(new ByteArrayWrapper("div-int/lit16".getBytes()), InstructionType.MATH_2C);
		INSTRUCTIONS.put(new ByteArrayWrapper("rem-int/lit16".getBytes()), InstructionType.MATH_2C);
		INSTRUCTIONS.put(new ByteArrayWrapper("and-int/lit16".getBytes()), InstructionType.MATH_2C);
		INSTRUCTIONS.put(new ByteArrayWrapper("or-int/lit16".getBytes()), InstructionType.MATH_2C);
		INSTRUCTIONS.put(new ByteArrayWrapper("xor-int/lit16".getBytes()), InstructionType.MATH_2C);
		
		INSTRUCTIONS.put(new ByteArrayWrapper("add-int/lit8".getBytes()), InstructionType.MATH_2C);
		INSTRUCTIONS.put(new ByteArrayWrapper("rsub-int/lit8".getBytes()), InstructionType.MATH_2C);
		INSTRUCTIONS.put(new ByteArrayWrapper("mul-int/lit8".getBytes()), InstructionType.MATH_2C);
		INSTRUCTIONS.put(new ByteArrayWrapper("div-int/lit8".getBytes()), InstructionType.MATH_2C);
		INSTRUCTIONS.put(new ByteArrayWrapper("rem-int/lit8".getBytes()), InstructionType.MATH_2C);
		INSTRUCTIONS.put(new ByteArrayWrapper("and-int/lit8".getBytes()), InstructionType.MATH_2C);
		INSTRUCTIONS.put(new ByteArrayWrapper("or-int/lit8".getBytes()), InstructionType.MATH_2C);
		INSTRUCTIONS.put(new ByteArrayWrapper("xor-int/lit8".getBytes()), InstructionType.MATH_2C);
		INSTRUCTIONS.put(new ByteArrayWrapper("shl-int/lit8".getBytes()), InstructionType.MATH_2C);
		INSTRUCTIONS.put(new ByteArrayWrapper("shr-int/lit8".getBytes()), InstructionType.MATH_2C);
		INSTRUCTIONS.put(new ByteArrayWrapper("ushr-int/lit8".getBytes()), InstructionType.MATH_2C);
	}
	
	
	public static InstructionType getType(byte[] opCode) {
		InstructionType t = INSTRUCTIONS.get(new ByteArrayWrapper(opCode));
		if (t == null) {
			if (DEBUG) LOGGER.debug("Found unkown opcode: "+new String(opCode)+" -- assigning UNKNOWN type.");
			return InstructionType.UNKNOWN;
		}
		else return t;
	}
}
