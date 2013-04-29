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
package de.rub.syssec.saaf.misc;

import java.util.Vector;

/**
 * FIXME: Remove Enum? Immutable vector?
 */
public abstract class Highlight {

	public static final Vector<String> OP_CODES = new Vector<String>();
	public static final Vector<String> ANNOTATIONS = new Vector<String>();

	static {
		// opcodes
		// OP_CODES.add("move-result ");
		// OP_CODES.add("invoke-interface ");
		// OP_CODES.add("const-string");
		// OP_CODES.add("move");
		// OP_CODES.add("return-void");
		// OP_CODES.add("if-eqz");
		// OP_CODES.add("goto");
		// OP_CODES.add("invoke-virtual");
		// OP_CODES.add("return");
		// OP_CODES.add("if-nez");
		// OP_CODES.add("move-result-object");
		// OP_CODES.add("const/4");
		// OP_CODES.add("iget-object");
		// OP_CODES.add("new-array");
		// OP_CODES.add("aput-object");
		// OP_CODES.add("invoke-virtual/range");
		// OP_CODES.add("move-object");

		OP_CODES.add("add-double/2addr ");
		OP_CODES.add("add-double ");
		OP_CODES.add("add-float/2addr ");
		OP_CODES.add("add-float ");
		OP_CODES.add("add-int/2addr ");
		OP_CODES.add("add-int/lit16 ");
		OP_CODES.add("add-int/lit8 ");
		OP_CODES.add("add-int ");
		OP_CODES.add("add-long/2addr ");
		OP_CODES.add("add-long ");
		OP_CODES.add("aget-boolean ");
		OP_CODES.add("aget-byte ");
		OP_CODES.add("aget-char ");
		OP_CODES.add("aget-object ");
		OP_CODES.add("aget-short ");
		OP_CODES.add("aget-wide ");
		OP_CODES.add("and-int ");
		OP_CODES.add("aget ");
		OP_CODES.add("and-int/2addr ");
		OP_CODES.add("and-int/lit16 ");
		OP_CODES.add("and-int/lit8 ");
		OP_CODES.add("and-long ");
		OP_CODES.add("and-long/2addr ");
		OP_CODES.add("aput ");
		OP_CODES.add("aput-boolean ");
		OP_CODES.add("aput-byte ");
		OP_CODES.add("aput-char ");
		OP_CODES.add("aput-object ");
		OP_CODES.add("aput-short ");
		OP_CODES.add("aput-wide ");
		OP_CODES.add("array-length ");
		OP_CODES.add("check-cast ");
		OP_CODES.add("cmp-long ");
		OP_CODES.add("cmpg-double ");
		OP_CODES.add("cmpg-float ");
		OP_CODES.add("cmpl-double ");
		OP_CODES.add("cmpl-float ");
		OP_CODES.add("const ");
		OP_CODES.add("const-class ");
		OP_CODES.add("const-string ");
		OP_CODES.add("const-string-jumbo ");
		OP_CODES.add("const-wide ");
		OP_CODES.add("const-wide/16 ");
		OP_CODES.add("const-wide/32 ");
		OP_CODES.add("const-wide/high16 ");
		OP_CODES.add("const/16 ");
		OP_CODES.add("const/4 ");
		OP_CODES.add("const/high16 ");
		OP_CODES.add("div-double ");
		OP_CODES.add("div-double/2addr ");
		OP_CODES.add("div-float ");
		OP_CODES.add("div-float/2addr ");
		OP_CODES.add("div-int ");
		OP_CODES.add("div-int/2addr ");
		OP_CODES.add("div-int/lit16 ");
		OP_CODES.add("div-int/lit8 ");
		OP_CODES.add("div-long ");
		OP_CODES.add("div-long/2addr ");
		OP_CODES.add("double-to-float ");
		OP_CODES.add("double-to-int ");
		OP_CODES.add("double-to-long ");
		OP_CODES.add("execute-inline ");
		OP_CODES.add("fill-array-data ");
		OP_CODES.add("filled-new-array ");
		OP_CODES.add("filled-new-array/range ");
		OP_CODES.add("float-to-double ");
		OP_CODES.add("float-to-int ");
		OP_CODES.add("float-to-long ");
		OP_CODES.add("goto ");
		OP_CODES.add("goto/16 ");
		OP_CODES.add("goto/32 ");
		OP_CODES.add("if-eq ");
		OP_CODES.add("if-eqz ");
		OP_CODES.add("if-ge ");
		OP_CODES.add("if-gez ");
		OP_CODES.add("if-gt ");
		OP_CODES.add("if-gtz ");
		OP_CODES.add("if-le ");
		OP_CODES.add("if-lez ");
		OP_CODES.add("if-lt ");
		OP_CODES.add("if-ltz ");
		OP_CODES.add("if-ne ");
		OP_CODES.add("if-nez ");
		OP_CODES.add("iget-boolean ");
		OP_CODES.add("iget-byte ");
		OP_CODES.add("iget-char ");
		OP_CODES.add("iget-object-quick ");
		OP_CODES.add("iget-object ");
		OP_CODES.add("iget-quick ");
		OP_CODES.add("iget-short ");
		OP_CODES.add("iget-wide-quick ");
		OP_CODES.add("iget-wide ");
		OP_CODES.add("iget ");
		OP_CODES.add("instance-of ");
		OP_CODES.add("int-to-byte ");
		OP_CODES.add("int-to-char ");
		OP_CODES.add("int-to-double ");
		OP_CODES.add("int-to-float ");
		OP_CODES.add("int-to-long ");
		OP_CODES.add("int-to-short ");
		OP_CODES.add("invoke-direct ");
		OP_CODES.add("invoke-direct-empty ");
		OP_CODES.add("invoke-direct/range ");
		OP_CODES.add("invoke-interface ");
		OP_CODES.add("invoke-interface/range ");
		OP_CODES.add("invoke-static ");
		OP_CODES.add("invoke-static/range ");
		OP_CODES.add("invoke-super ");
		OP_CODES.add("invoke-super-quick ");
		OP_CODES.add("invoke-super-quick/range ");
		OP_CODES.add("invoke-super/range ");
		OP_CODES.add("invoke-virtual ");
		OP_CODES.add("invoke-virtual-quick ");
		OP_CODES.add("invoke-virtual-quick/range ");
		OP_CODES.add("invoke-virtual/range ");
		OP_CODES.add("iput ");
		OP_CODES.add("iput-boolean ");
		OP_CODES.add("iput-byte ");
		OP_CODES.add("iput-char ");
		OP_CODES.add("iput-object ");
		OP_CODES.add("iput-object-quick ");
		OP_CODES.add("iput-quick ");
		OP_CODES.add("iput-short ");
		OP_CODES.add("iput-wide ");
		OP_CODES.add("iput-wide-quick ");
		OP_CODES.add("long-to-double ");
		OP_CODES.add("long-to-float ");
		OP_CODES.add("long-to-int ");
		OP_CODES.add("monitor-enter ");
		OP_CODES.add("monitor-exit ");
		OP_CODES.add("move ");
		OP_CODES.add("move-exception ");
		OP_CODES.add("move-object ");
		OP_CODES.add("move-object/16 ");
		OP_CODES.add("move-object/from16 ");
		OP_CODES.add("move-result ");
		OP_CODES.add("move-result-object ");
		OP_CODES.add("move-result-wide ");
		OP_CODES.add("move-wide ");
		OP_CODES.add("move-wide/16 ");
		OP_CODES.add("move-wide/from16 ");
		OP_CODES.add("move/16 ");
		OP_CODES.add("move/from16 ");
		OP_CODES.add("mul-double ");
		OP_CODES.add("mul-double/2addr ");
		OP_CODES.add("mul-float ");
		OP_CODES.add("mul-float/2addr ");
		OP_CODES.add("mul-int ");
		OP_CODES.add("mul-int/2addr ");
		OP_CODES.add("mul-int/lit8 ");
		OP_CODES.add("mul-int/lit16 ");
		OP_CODES.add("mul-long ");
		OP_CODES.add("mul-long/2addr ");
		OP_CODES.add("neg-double ");
		OP_CODES.add("neg-float ");
		OP_CODES.add("neg-int ");
		OP_CODES.add("neg-long ");
		OP_CODES.add("new-array ");
		OP_CODES.add("new-instance ");
		OP_CODES.add("nop ");
		OP_CODES.add("not-int ");
		OP_CODES.add("not-long ");
		OP_CODES.add("or-int ");
		OP_CODES.add("or-int/2addr ");
		OP_CODES.add("or-int/lit16 ");
		OP_CODES.add("or-int/lit8 ");
		OP_CODES.add("or-long ");
		OP_CODES.add("or-long/2addr ");
		OP_CODES.add("rem-double ");
		OP_CODES.add("rem-double/2addr ");
		OP_CODES.add("rem-float ");
		OP_CODES.add("rem-float/2addr ");
		OP_CODES.add("rem-int ");
		OP_CODES.add("rem-int/2addr ");
		OP_CODES.add("rem-int/lit16 ");
		OP_CODES.add("rem-int/lit8 ");
		OP_CODES.add("rem-long ");
		OP_CODES.add("rem-long/2addr ");
		OP_CODES.add("return-void");
		OP_CODES.add("return-object ");
		OP_CODES.add("return-wide ");
		OP_CODES.add("return");
		OP_CODES.add("sget ");
		OP_CODES.add("sget-boolean ");
		OP_CODES.add("sget-byte ");
		OP_CODES.add("sget-char ");
		OP_CODES.add("sget-object ");
		OP_CODES.add("sget-short ");
		OP_CODES.add("sget-wide ");
		OP_CODES.add("shl-int ");
		OP_CODES.add("shl-int/2addr ");
		OP_CODES.add("shl-int/lit8 ");
		OP_CODES.add("shl-long ");
		OP_CODES.add("shl-long/2addr ");
		OP_CODES.add("shr-int ");
		OP_CODES.add("shr-int/2addr ");
		OP_CODES.add("shr-int/lit8 ");
		OP_CODES.add("shr-long ");
		OP_CODES.add("shr-long/2addr ");
		OP_CODES.add("sparse-switch ");
		OP_CODES.add("sput ");
		OP_CODES.add("sput-boolean ");
		OP_CODES.add("sput-byte ");
		OP_CODES.add("sput-char ");
		OP_CODES.add("sput-object ");
		OP_CODES.add("sput-short ");
		OP_CODES.add("sput-wide ");
		OP_CODES.add("sub-double ");
		OP_CODES.add("sub-double/2addr ");
		OP_CODES.add("sub-float ");
		OP_CODES.add("sub-float/2addr ");
		OP_CODES.add("sub-int ");
		OP_CODES.add("sub-int/2addr ");
		OP_CODES.add("sub-int/lit16 ");
		OP_CODES.add("sub-int/lit8 ");
		OP_CODES.add("sub-long ");
		OP_CODES.add("sub-long/2addr ");
		OP_CODES.add("throw ");
		OP_CODES.add("ushr-int ");
		OP_CODES.add("ushr-int/2addr ");
		OP_CODES.add("ushr-int/lit8 ");
		OP_CODES.add("ushr-long ");
		OP_CODES.add("ushr-long/2addr ");
		OP_CODES.add("xor-int ");
		OP_CODES.add("xor-int/2addr ");
		OP_CODES.add("xor-int/lit16 ");
		OP_CODES.add("xor-int/lit8 ");
		OP_CODES.add("xor-long ");
		OP_CODES.add("xor-long/2addr ");

		// annotations
		ANNOTATIONS.add(".locals ");
		ANNOTATIONS.add(".local ");
		ANNOTATIONS.add(".parameter ");
		ANNOTATIONS.add(".prologue");
		ANNOTATIONS.add(".line ");
		ANNOTATIONS.add(".class ");
		ANNOTATIONS.add(".super ");
		ANNOTATIONS.add(".source ");
		ANNOTATIONS.add("# instance ");
		ANNOTATIONS.add("# virtual");
		ANNOTATIONS.add("# direct");
		ANNOTATIONS.add(".method ");
		ANNOTATIONS.add(".end ");
		ANNOTATIONS.add(".field ");
		ANNOTATIONS.add(".catch ");
		ANNOTATIONS.add(".catchall ");
		ANNOTATIONS.add(":try");
		ANNOTATIONS.add("# static ");
		ANNOTATIONS.add("# annotations");
		ANNOTATIONS.add(".annotation ");
	}
}
