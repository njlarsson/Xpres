import xpres2.HackGen;

import java.io.*;

// Generates hack code that corresponds to the following:
//
// var i;
// var sum;
// i = 0;
// sum = 0;
// while (i < 100) {
//    i = i+1;
//    sum = sum+i;
// }
//
// The code is far from the tightest possible. Rather, it is what might be
// generated from a simple compiler for stack-based execution, without any
// optimization at all.
//
// Writing this was unbelievably tedious, but then you're not really supposed to
// do this by hand.

public class GenerateSum100 {
    public static void main(String[] args) throws IOException {
        HackGen out = new HackGen(1024, 2048, 1025);

        // var memory addresses
        int iAddr = out.newVarAddr();
        int sumAddr = out.newVarAddr();
        
        // init stack pointer
        out.emitInitSP();

        // i = 0: The value of the expression first gets pushed on the stack.
        out.emitAInstr(0);                                              // load A with 0
        out.emitCInstr(HackGen.DestD, HackGen.CompA, HackGen.NoJump);   // move it to D
        out.emitPushD();                                                // push it

        // Now the value is popped from the stack and stored in i.
        out.emitPopD();                                                 // pop it again (sorry)
        out.emitAInstr(iAddr);                                          // get i's address
        out.emitCInstr(HackGen.DestM, HackGen.CompD, HackGen.NoJump);   // store D in i
        
        // sum = 0, analogously to above.
        out.emitAInstr(0);                                              // load A with 0
        out.emitCInstr(HackGen.DestD, HackGen.CompA, HackGen.NoJump);   // move it to D
        out.emitPushD();                                                // push it
        out.emitPopD();                                                 // pop it again (sorry)
        out.emitAInstr(sumAddr);                                        // get sum's address
        out.emitCInstr(HackGen.DestM, HackGen.CompD, HackGen.NoJump);   // store D in i

        // No we're at the start of the loop. Save the address.
        int loopAddr = out.currentCodeAddress();

        // Compute i < 100. Leave nonzero value on the stack if true, zero if
        // it's false. First push i on stack:
        out.emitAInstr(iAddr);                                          // address of i into A
        out.emitCInstr(HackGen.DestD, HackGen.CompM, HackGen.NoJump);   // content of i into D
        out.emitPushD();                                                // push it

        // Then push 100 on stack:
        out.emitAInstr(100);                                            // 100 into A
        out.emitCInstr(HackGen.DestD, HackGen.CompA, HackGen.NoJump);   // move it to D
        out.emitPushD();                                                // push it

        // Get the operands, subtract them, and use the sign bit as boolean.
        out.emitGetTwoOperands();                                       // 100 in D, i in M
        out.emitCInstr(HackGen.DestD, HackGen.MMinusD, HackGen.NoJump); // i-100 in D
        out.emitAInstr(0b111111111111111);                              // 15 1-bits
        out.emitCInstr(HackGen.DestA, HackGen.NotA, HackGen.NoJump);    // invert, so now only sign bit is 1
        out.emitCInstr(HackGen.DestD, HackGen.DAndA, HackGen.NoJump);   // AND off all bits except sign bit
        out.emitReplaceTopWithD();                                      // "boolean" value of i<100 on top of stack

        // Jump out of the loop if top of stack is 0==false.
        out.emitPopD();
        int jumpEndInstructionAddr = out.emitAInstr(0);                 // emit dummy @-instr, to fix later
        out.emitCInstr(HackGen.DestNone, HackGen.CompD, HackGen.JEQ);   // jump if zero (false)

        // i = i+1. First put i on stack:
        out.emitAInstr(iAddr);                                          // address of i into A
        out.emitCInstr(HackGen.DestD, HackGen.CompM, HackGen.NoJump);   // content of i into D
        out.emitPushD();                                                // push it

        // Then 1:
        out.emitAInstr(1);                                              // 1 into A
        out.emitCInstr(HackGen.DestD, HackGen.CompA, HackGen.NoJump);   // move it to D
        out.emitPushD();                                                // push it

        // Then add them:
        out.emitGetTwoOperands();                                       // 1 in D, i in M
        out.emitCInstr(HackGen.DestD, HackGen.DPlusM, HackGen.NoJump);  // 1+i in D
        out.emitReplaceTopWithD();                                      // result on top of stack

        // Then pop result and save in i.
        out.emitPopD();                                                 // pop it again (sorry)
        out.emitAInstr(iAddr);                                          // get i's address
        out.emitCInstr(HackGen.DestM, HackGen.CompD, HackGen.NoJump);   // store D in i

        // sum = sum+i. First put sum on stack:
        out.emitAInstr(sumAddr);                                        // address of sum into A
        out.emitCInstr(HackGen.DestD, HackGen.CompM, HackGen.NoJump);   // content of sum into D
        out.emitPushD();                                                // push it

        // Then i:
        out.emitAInstr(iAddr);                                          // address of i into A
        out.emitCInstr(HackGen.DestD, HackGen.CompM, HackGen.NoJump);   // content of i into D
        out.emitPushD();                                                // push it

        // Then add them:
        out.emitGetTwoOperands();                                       // i in D, sum in M
        out.emitCInstr(HackGen.DestD, HackGen.DPlusM, HackGen.NoJump);  // i+sum in D
        out.emitReplaceTopWithD();                                      // sum on top of stack

        // Then pop result and save in sum.
        out.emitPopD();                                                 // pop it again (sorry)
        out.emitAInstr(sumAddr);                                        // get sum's address
        out.emitCInstr(HackGen.DestM, HackGen.CompD, HackGen.NoJump);   // store D in sum

        // Now the loop body is done. We just need to add an unconditional jump back...
        out.emitAInstr(loopAddr);
        out.emitCInstr(HackGen.DestNone, HackGen.CompNone, HackGen.JMP);

        // ... and finally modyfy the JEQ jump address to go past the whole loop code.
        int endAddr = out.currentCodeAddress();
        out.reviseAInstr(jumpEndInstructionAddr, endAddr);

        // Output result
        Writer w = new OutputStreamWriter(new FileOutputStream("generatedSum100.hack"), "US-ASCII");
        out.outputCode(w);
        w.close();
        
        // Phew.
    }
}
