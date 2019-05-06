// See LICENSE.SiFive for license details.

package freechips.rocketchip.diplomaticobjectmodel.model

case class OMSRAM(
  description: String,
  addressWidth: Int,
  dataWidth: Int,
  depth: BigInt,
  writeMaskGranularity: Int,
  _types: Seq[String] = Seq("OMSRAM")
)
