package xpres2;

import java.util.*;
import java.io.*;

/**
 * A utility class for generating hack code.
 */
public class HackGen {
    private final int SPAddr;    // memory address of the stack pointer (SP)
    private final int stackBase; // memory address of the stack (that the SP points to)

    // Constants used for the first parameter of emitCInstr (the "destination"
    // part). This can be combined with | to make the result go to more than one
    // place.
    public static final int DestNone = 0b000;
    public static final int DestM = 0b001;
    public static final int DestD = 0b010;
    public static final int DestA = 0b100;

    // Constants used for the second parameter of emitCInstr (the "computation" part)
    public static final int CompNone = 0b0000000;
    public static final int Comp0 = 0b0101010;
    public static final int Comp1 = 0b0111111;
    public static final int Minus1 = 0b0111010;
    public static final int CompD = 0b0001100;
    public static final int CompA = 0b0110000;
    public static final int NotD = 0b0001101;
    public static final int NotA = 0b0110001;
    public static final int MinusD = 0b0001111;
    public static final int MinusA = 0b0110011;
    public static final int DPlus1 = 0b0011111;
    public static final int APlus1 = 0b0110111;
    public static final int DMinus1 = 0b0001110;
    public static final int AMinus1 = 0b0110010;
    public static final int DPlusA = 0b0000010;
    public static final int DMinusA = 0b0010011;
    public static final int AMinusD = 0b0000111;
    public static final int DAndA = 0b0000000;
    public static final int DOrA = 0b0010101;
    public static final int CompM = 0b1110000;
    public static final int NotM = 0b1110001;
    public static final int MinusM = 0b1110011;
    public static final int MPlus1 = 0b1110111;
    public static final int MMinus1 = 0b1110010;
    public static final int DPlusM = 0b1000010;
    public static final int DMinusM = 0b1010011;
    public static final int MMinusD = 0b1000111;
    public static final int DAndM = 0b1000000;
    public static final int DOrM = 0b1010101;

    // Constants used for the third parameter of emitCInstr (the "jump" part)
    public static final int NoJump = 0b000;
    public static final int JGT = 0b001;
    public static final int JEQ = 0b010;
    public static final int JGE = 0b011;
    public static final int JLT = 0b100;
    public static final int JNE = 0b101;
    public static final int JLE = 0b110;
    public static final int JMP = 0b111;

    // Keeps track of memory addresses of variables.
    private int curVarAddr;

    // Output buffer, where the instructions are stored.
    private ArrayList<Integer> code = new ArrayList<Integer>();

    /** Creates a code generator object, given memory addresses of stack and
     *  variable storage. (You might expect a memory address for the code as
     *  well, but this class always places the code starting from memory address
     *  0.)
     *
     * @param SPAddr       memory address of the stack pointer (SP)
     * @param stackBase    memory address of the stack (that the SP points to)
     * @param varBase      memory address of area for user variables */
    public HackGen(int SPAddr, int stackBase, int varBase) {
        this.SPAddr = SPAddr;
        this.stackBase = stackBase;
        curVarAddr = varBase;
    }

    /** Makes room for a user variable, and returns its memory address. */
    public int newVarAddr() {
        return curVarAddr++;
    }

    /** Adds an @-instruction to the output buffer. Returns its designated memory address. */
    public int emitAInstr(int a) {
        int pos = code.size();
        code.add(a);
        return pos;
    }

    /** Gets the current size of the output buffer == the memory address of the next intstruction.*/
    public int currentCodeAddress() {
        return code.size();
    }

    /** Changes an @-instruction already emitted to the output buffer.
      *
      * @param pos The memory address of the instruction.
      * @param a   The new address to refer to in the instruction. */
    public void reviseAInstr(int pos, int a) {
        code.set(pos, a);
    }

    /** Adds a C-instruction to the output buffer. See Nisan/Schocken for
      * specification of what a C-instruction consists of. Constants provided in
      * this class can be used to fill in the three parts. */
    public void emitCInstr(int dest, int comp, int jump) {
        int c = 0b1110000000000000 | comp << 6 | dest << 3 | jump;
        code.add(c);
    }

    /** Emits a sequence of instructions with the effect of setting the stack
      * pointer to its initial value (destroying the contents of both A and D in
      * the process). */
    public void emitInitSP() {
        emitAInstr(stackBase);       // @stackBase
        emitCInstr(DestD, CompA, 0); // D=stackBase
        emitAInstr(SPAddr);          // @SP
        emitCInstr(DestM, CompD, 0); // [SP]=stackBase
    }

    /** Emits a sequence of instructions with the effect of pushing the contents
      * of the D register onto the stack (destroying the contents of both A and
      * D in the process). */
    public void emitPushD() {
        emitAInstr(SPAddr);           // @SP
        emitCInstr(DestA, CompM, 0);  // A=SP
        emitCInstr(DestM, CompD, 0);  // [SP]=D
        emitCInstr(DestD, APlus1, 0); // D=SP+1
        emitAInstr(SPAddr);           // @SP
        emitCInstr(DestM, CompD, 0);  // [SP]=SP+1
    }
    
    /** Emits a sequence of instructions with the effect of popping one value
      * from the stack and placing it i the D register (destroying the contents
      * of A in the process). */
    public void emitPopD() {
        emitAInstr(SPAddr);                  // @SP
        emitCInstr(DestD, MMinus1, 0);       // D=SP-1
        emitCInstr(DestM | DestA, CompD, 0); // SP=SP-1; A=SP
        emitCInstr(DestD, CompM, 0);         // D=[SP]
    }

    /** Emits a sequence of instructions to prepare for a two-operand operation,
      * by getting the operands from the stack. After this, the address of the
      * left operand is in A (and can this be referred to as M), and the right
      * operand is in D. */
    public void emitGetTwoOperands() {
        emitAInstr(SPAddr);                  // @SP
        emitCInstr(DestD, CompM, 0);         // D=SP
        emitCInstr(DestM|DestA, DMinus1, 0); // A=SP=SP-1
        emitCInstr(DestD, CompM, 0);         // D=[SP]
        emitCInstr(DestA, AMinus1, 0);       // A=SP-1
    }

    /** Emits a sequence of instructions to replace the value on the top of the
      * stack with the contents of D, i.e., the same effect as first popping a
      * value off the stack (ignoring it) and then pushing D. */
    public void emitReplaceTopWithD() {
        emitAInstr(SPAddr);            // @SP
        emitCInstr(DestA, MMinus1, 0); // A=SP-1
        emitCInstr(DestM, CompD, 0);   // [SP-1]=D
    }

    /** Outputs the contents of the code buffer as binary-number strings (i.e.,
      * the .hack file format). */
    public void outputCode(Writer w) throws IOException {
        for (int op : code) {
            String binstr = Integer.toBinaryString(op);
            for (int i = binstr.length(); i < 16; i++) {
                w.write("0"); // pad with initial zeros
            }
            w.write(binstr);
            w.write("\n"); 
        }
    }
}
