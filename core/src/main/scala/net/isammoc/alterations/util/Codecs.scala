package net.isammoc.alterations.util

import java.security.MessageDigest

object Codecs {
  def toHexString(buf: Array[Byte]): String = buf.map("%02x".format(_)).mkString
  def sha1(str: String): String             = toHexString(MessageDigest.getInstance("SHA-1").digest(str.getBytes("UTF-8")))
}
