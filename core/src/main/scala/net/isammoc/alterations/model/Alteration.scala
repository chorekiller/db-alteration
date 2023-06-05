package net.isammoc.alterations.model

import net.isammoc.alterations.util.Codecs.sha1

case class Alteration(version: Int, sqlRaise: String, sqlDecline: String) {
  val hash: String = sha1(sqlRaise.trim + sqlDecline.trim)
}
