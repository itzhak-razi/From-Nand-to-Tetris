// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[3], respectively.)

// Put your code here.

// Calculate x * y

@R1    // Load R1 to A
D=M    // D = y
@R3    // Load R3 to A
M=D    // R3 = y, so that data in R1 won't be touched
@R2    // Load R2 to A
M=0    // Reset R2 to 0, so the result from previous time won't interfere

(LOOP)
    @R3    // Load R3 to A
    D=M    // D = y
    @END    // Load END to A
    D;JLE    // if y < 0, goto END
    @R3    // Load R3 back to A
    M=D-1    // if y >= 0, y -= 1
    @R0    // Load R0 to A
    D=M    // D = x
    @R2    // Load R2(result) to A
    M=D+M    // R2 += x
    @LOOP
    0;JMP    // Goto LOOP
(END)
    @END
    0;JMP    // Infinite loop