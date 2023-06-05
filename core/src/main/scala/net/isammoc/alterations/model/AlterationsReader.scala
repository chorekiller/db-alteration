package net.isammoc.alterations.model

trait AlterationsReader {
  def getAlterations(): Seq[Alteration]
}
