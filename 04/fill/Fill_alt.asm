// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel. When no key is pressed, the
// program clears the screen, i.e. writes "white" in every pixel.

// Put your code here.
@24575
D=A
@last
M=D

(LOOP)
    @SCREEN
    D=A-1
    @R0
    M=D

    @KBD
    D=M
    @FILL
    D;JNE
    @CLEAR
    D;JEQ
    @LOOP
    0;JMP

(FILL)
    @R0
    D=M
    @last
    D=M-D
    @LOOP
    D;JEQ
    @R0
    M=M+1
    @0
    D=!A
    @R0
    A=M
    M=D
    @FILL
    0;JMP


(CLEAR)
    @R0
    D=M
    @last
    D=M-D
    @LOOP
    D;JEQ
    @R0
    M=M+1
    A=M
    M=0
    @CLEAR
    0;JMP