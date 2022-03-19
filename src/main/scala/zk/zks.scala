// See LICENSE.SiFive for license details.

package freechips.rocketchip.zk

import chisel3._
import chisel3.util._

object ZKS {
  val SM4ED       = BitPat("b??11000??????????000?????0110011")
  val SM4KS       = BitPat("b??11010??????????000?????0110011")
  val SM3P0       = BitPat("b000100001000?????001?????0010011")
  val SM3P1       = BitPat("b000100001001?????001?????0010011")

  val FN_Len      = 4
  def FN_SM4ED    =  0.U(FN_Len.W)
  def FN_SM4KS    =  1.U(FN_Len.W)
  def FN_SM3P0    =  2.U(FN_Len.W)
  def FN_SM3P1    =  3.U(FN_Len.W)
}

class ZKSInterface(xLen: Int) extends Bundle {
  val zks_fn = Input(UInt(ZKN.FN_Len.W))
  val valid  = Input(Bool())
  val bs     = Input(UInt(2.W))
  val rs1    = Input(UInt(xLen.W))
  val rs2    = Input(UInt(xLen.W))
  val rd     = Output(UInt(xLen.W))
}

object SM4 {
  val sbox: Seq[Int] = Seq(
	0xD6, 0x90, 0xE9, 0xFE, 0xCC, 0xE1, 0x3D, 0xB7, 0x16, 0xB6, 0x14, 0xC2,
	0x28, 0xFB, 0x2C, 0x05, 0x2B, 0x67, 0x9A, 0x76, 0x2A, 0xBE, 0x04, 0xC3,
	0xAA, 0x44, 0x13, 0x26, 0x49, 0x86, 0x06, 0x99, 0x9C, 0x42, 0x50, 0xF4,
	0x91, 0xEF, 0x98, 0x7A, 0x33, 0x54, 0x0B, 0x43, 0xED, 0xCF, 0xAC, 0x62,
	0xE4, 0xB3, 0x1C, 0xA9, 0xC9, 0x08, 0xE8, 0x95, 0x80, 0xDF, 0x94, 0xFA,
	0x75, 0x8F, 0x3F, 0xA6, 0x47, 0x07, 0xA7, 0xFC, 0xF3, 0x73, 0x17, 0xBA,
	0x83, 0x59, 0x3C, 0x19, 0xE6, 0x85, 0x4F, 0xA8, 0x68, 0x6B, 0x81, 0xB2,
	0x71, 0x64, 0xDA, 0x8B, 0xF8, 0xEB, 0x0F, 0x4B, 0x70, 0x56, 0x9D, 0x35,
	0x1E, 0x24, 0x0E, 0x5E, 0x63, 0x58, 0xD1, 0xA2, 0x25, 0x22, 0x7C, 0x3B,
	0x01, 0x21, 0x78, 0x87, 0xD4, 0x00, 0x46, 0x57, 0x9F, 0xD3, 0x27, 0x52,
	0x4C, 0x36, 0x02, 0xE7, 0xA0, 0xC4, 0xC8, 0x9E, 0xEA, 0xBF, 0x8A, 0xD2,
	0x40, 0xC7, 0x38, 0xB5, 0xA3, 0xF7, 0xF2, 0xCE, 0xF9, 0x61, 0x15, 0xA1,
	0xE0, 0xAE, 0x5D, 0xA4, 0x9B, 0x34, 0x1A, 0x55, 0xAD, 0x93, 0x32, 0x30,
	0xF5, 0x8C, 0xB1, 0xE3, 0x1D, 0xF6, 0xE2, 0x2E, 0x82, 0x66, 0xCA, 0x60,
	0xC0, 0x29, 0x23, 0xAB, 0x0D, 0x53, 0x4E, 0x6F, 0xD5, 0xDB, 0x37, 0x45,
	0xDE, 0xFD, 0x8E, 0x2F, 0x03, 0xFF, 0x6A, 0x72, 0x6D, 0x6C, 0x5B, 0x51,
	0x8D, 0x1B, 0xAF, 0x92, 0xBB, 0xDD, 0xBC, 0x7F, 0x11, 0xD9, 0x5C, 0x41,
	0x1F, 0x10, 0x5A, 0xD8, 0x0A, 0xC1, 0x31, 0x88, 0xA5, 0xCD, 0x7B, 0xBD,
	0x2D, 0x74, 0xD0, 0x12, 0xB8, 0xE5, 0xB4, 0xB0, 0x89, 0x69, 0x97, 0x4A,
	0x0C, 0x96, 0x77, 0x7E, 0x65, 0xB9, 0xF1, 0x09, 0xC5, 0x6E, 0xC6, 0x84,
	0x18, 0xF0, 0x7D, 0xEC, 0x3A, 0xDC, 0x4D, 0x20, 0x79, 0xEE, 0x5F, 0x3E,
	0xD7, 0xCB, 0x39, 0x48
  )
}

class ZKSImp(xLen:Int) extends Module {
  val io = IO(new ZKSInterface(xLen))

  // helper
  def sext(in: UInt): UInt = if (xLen == 32) in
    else {
      require(xLen == 64)
      val in_hi_32 = Fill(32, in(31))
      Cat(in_hi_32, in)
    }
  def asBytes(in: UInt): Vec[UInt] = VecInit(in.asBools.grouped(8).map(VecInit(_).asUInt).toSeq)

  // sm4
  // dynamic selection should be merged into aes rv32 logic!
  val si = asBytes(io.rs2(31,0))(io.bs)
  val so = {
    val m = Module(new SBox(SM4.sbox))
    m.io.in := si
    m.io.out
  }
  val x = Cat(0.U(24.W), so)
  val y = Mux(io.zks_fn === ZKS.FN_SM4ED,
    x ^ (x << 8) ^ (x << 2) ^ (x << 18) ^ ((x & 0x3F.U) << 26) ^ ((x & 0xC0.U) << 10),
    x ^ ((x & 0x7.U) << 29) ^ ((x & 0xFE.U) << 7) ^ ((x & 0x1.U) << 23) ^ ((x & 0xF8.U) << 13))(31,0)
  // dynamic rotate should be merged into aes rv32 logic!
  // Vec rightRotate = UInt rotateLeft as Vec is big endian while UInt is little endian
  val z = barrel.rightRotate(asBytes(y), io.bs).asUInt
  val sm4 = sext(z ^ io.rs1(31,0))

  // sm3
  val r1 = io.rs1(31,0)
  val sm3 = sext(Mux(io.zks_fn === ZKS.FN_SM3P0,
    r1 ^ r1.rotateLeft(9) ^ r1.rotateLeft(17),
    r1 ^ r1.rotateLeft(15) ^ r1.rotateLeft(23)))

  // according to FN_xxx above
  io.rd := VecInit(Seq(
    sm4, sm4,
    sm3, sm3))(io.zks_fn)
}
