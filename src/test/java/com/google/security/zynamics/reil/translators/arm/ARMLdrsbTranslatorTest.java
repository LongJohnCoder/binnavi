/*
Copyright 2014 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.google.security.zynamics.reil.translators.arm;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.TestHelpers;
import com.google.security.zynamics.reil.interpreter.CpuPolicyARM;
import com.google.security.zynamics.reil.interpreter.EmptyInterpreterPolicy;
import com.google.security.zynamics.reil.interpreter.Endianness;
import com.google.security.zynamics.reil.interpreter.InterpreterException;
import com.google.security.zynamics.reil.interpreter.ReilInterpreter;
import com.google.security.zynamics.reil.interpreter.ReilRegisterStatus;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.reil.translators.arm.ARMLdrsbTranslator;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.MockInstruction;
import com.google.security.zynamics.zylib.disassembly.MockOperandTree;
import com.google.security.zynamics.zylib.disassembly.MockOperandTreeNode;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class ARMLdrsbTranslatorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.LITTLE_ENDIAN,
      new CpuPolicyARM(), new EmptyInterpreterPolicy());

  private final StandardEnvironment environment = new StandardEnvironment();

  private final ARMLdrsbTranslator translator = new ARMLdrsbTranslator();

  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  final OperandSize dw = OperandSize.DWORD;
  final OperandSize wd = OperandSize.WORD;
  final OperandSize bt = OperandSize.BYTE;


  @Before
  public void setUp() {
  }

  @Test
  public void testLdrh() throws InternalTranslationException, InterpreterException {
    // LDRH r2, r3, [r1],#8

    interpreter.setRegister("R0", BigInteger.valueOf(0x07FFFFDCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x00008124L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x0000817BL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x00452A02L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x00008124L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x000096A0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x8124L, 0x73726946L, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, ","));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.IMMEDIATE_INTEGER, String.valueOf(8)));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRSB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x07FFFFDCL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x0000812CL), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x00000046L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x00452A02L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x00008124L), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x000096A0L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R9"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(16, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }
}
