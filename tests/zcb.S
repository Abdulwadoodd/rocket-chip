#define RVTEST_DATA_BEGIN                                               	\
        .pushsection .tohost,"aw",@progbits;                            	\
        .align 6; .global tohost; tohost: .dword 0; .size tohost, 8;    	\
        .align 6; .global fromhost; fromhost: .dword 0; .size fromhost, 8;	\
        .popsection;                                                    	\
        .align 4; .global begin_signature; begin_signature:

#define RVTEST_CODE_BEGIN                                               \
        .section .text.init;                                            \
        .align  6;                                                      \
        .global _start;							\
_start:                                                                 \
        j main;								\
        .align 2;							\
trap_vector:                                                           	\
	csrr t6, mcause;						\
	csrr t5, mtval;							\
	csrr t4, mepc;							\
	nop;								\
	nop;								\
	j write_tohost

RVTEST_CODE_BEGIN
.align 8
main:
	la t1, trap_vector
	csrw mtvec, t1
	nop
	nop
	la x14, base
	nop
	nop
	// Testing `c.lbu`
	c.lbu x8, 0(x14)
	c.lbu x8, 1(x14)
	c.lbu x8, 2(x14)
	c.lbu x8, 3(x14)
	nop
	// testing `c.lhu`
	c.lhu x9, 0(x14)
	c.lhu x9, 2(x14)
	nop
	// testing `c.lh`
	c.lh  x10, 0(x14)
	c.lh  x10, 2(x14)
	nop
	nop
	la    x14, str
	nop
	nop
	// testing `sb`
	li 	  x11, 0x12
	c.sb  x11, 0(x14)
	nop
	li 	  x11, 0x34
	c.sb  x11, 1(x14)
	nop
	li 	  x11, 0x56
	c.sb  x11, 2(x14)
	nop
	li 	  x11, 0x78
	c.sb  x11, 3(x14)
	nop
	nop
	lb    x11, 0(x14)
	lb    x11, 1(x14)
	lb    x11, 2(x14)
	lb    x11, 3(x14)
	nop
	nop
	addi  x15, x14, 4
	nop
	nop
	// testing `sh`
	li    x12, 0xbebe
	c.sh  x12, 0(x15)
	nop
	li    x12, 0xcafe
	c.sh  x12, 2(x15)
	nop
	nop
	lh    x12, 0(x15)
	lh    x12, 2(x15)
	nop
	nop
	li x8,  0xB12
	li x9,  0xBF4
	li x10, 0xF12
	li x11, 0x843F12
	li x12, 0xFF800ABC24
	li x13, 0x8F4B
	li x14, 0x2
	li x15, 0x4
	nop
	nop
	nop
	c.zext.b x8
	c.sext.b x9
	c.zext.h x10
	c.sext.h x11
	c.zext.w x12
	c.not x13
	c.mul x15, x14
	nop
	nop
	add x15, x15, x0
	nop
	j write_tohost
.align 2	
write_tohost:
	li gp, 1
	sw gp, tohost, t5
	j write_tohost

.data
base:
.word 0xdeadbeef
str:
.word 0x0
.word 0x0
RVTEST_DATA_BEGIN
