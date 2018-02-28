/*
 * Copyright (c) 2018, 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.graalvm.compiler.core.aarch64;

import org.graalvm.compiler.core.gen.NodeMatchRules;
import org.graalvm.compiler.core.match.ComplexMatchResult;
import org.graalvm.compiler.core.match.MatchRule;
import org.graalvm.compiler.debug.GraalError;
import org.graalvm.compiler.lir.LIRFrameState;
import org.graalvm.compiler.lir.aarch64.AArch64AddressValue;
import org.graalvm.compiler.lir.gen.LIRGeneratorTool;
import org.graalvm.compiler.nodes.DeoptimizingNode;
import org.graalvm.compiler.nodes.NodeView;
import org.graalvm.compiler.nodes.calc.SignExtendNode;
import org.graalvm.compiler.nodes.calc.ZeroExtendNode;
import org.graalvm.compiler.nodes.memory.Access;

import jdk.vm.ci.aarch64.AArch64Kind;

public class AArch64NodeMatchRules extends NodeMatchRules {

    public AArch64NodeMatchRules(LIRGeneratorTool gen) {
        super(gen);
    }

    protected LIRFrameState getState(Access access) {
        if (access instanceof DeoptimizingNode) {
            return state((DeoptimizingNode) access);
        }
        return null;
    }

    protected AArch64Kind getMemoryKind(Access access) {
        return (AArch64Kind) gen.getLIRKind(access.asNode().stamp(NodeView.DEFAULT)).getPlatformKind();
    }

    @Override
    public AArch64LIRGenerator getLIRGeneratorTool() {
        return (AArch64LIRGenerator) gen;
    }

    protected AArch64ArithmeticLIRGenerator getArithmeticLIRGenerator() {
        return (AArch64ArithmeticLIRGenerator) getLIRGeneratorTool().getArithmetic();
    }

    private ComplexMatchResult emitExtendMemory(Access access, int fromBits, int toBits, boolean signExtend) {
        if (fromBits == toBits) {
            return null;
        }
        AArch64Kind fromKind;
        switch (fromBits) {
            case 8:
                fromKind = AArch64Kind.BYTE;
                break;
            case 16:
                fromKind = AArch64Kind.WORD;
                break;
            case 32:
                fromKind = AArch64Kind.DWORD;
                break;
            case 64:
                fromKind = AArch64Kind.QWORD;
                break;
            default:
                throw GraalError.unimplemented("unsupported extending load (" + fromBits + " bit -> " + toBits + " bit)");
        }
        return builder -> {
            AArch64LIRGenerator lirgen = getLIRGeneratorTool();
            AArch64ArithmeticLIRGenerator arithgen = (AArch64ArithmeticLIRGenerator) lirgen.getArithmetic();
            return arithgen.emitExtendMemory(signExtend, fromKind, toBits, (AArch64AddressValue) operand(access.getAddress()), getState(access));
        };
    }

    @MatchRule("(SignExtend Read=access)")
    @MatchRule("(SignExtend FloatingRead=access)")
    public ComplexMatchResult signExtend(SignExtendNode root, Access access) {
        return emitExtendMemory(access, root.getInputBits(), root.getResultBits(), true);
    }

    @MatchRule("(ZeroExtend Read=access)")
    @MatchRule("(ZeroExtend FloatingRead=access)")
    public ComplexMatchResult zeroExtend(ZeroExtendNode root, Access access) {
        return emitExtendMemory(access, root.getInputBits(), root.getResultBits(), false);
    }
}
