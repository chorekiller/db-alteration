package net.isammoc.alterations.file

import net.isammoc.alterations.model.Alteration
import net.isammoc.alterations.model.AlterationsReader

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path

case class FileAlterationReader(path: Path) extends AlterationsReader {
  override def getAlterations(): List[Alteration] = {
    if (Files.isDirectory(path)) {
      LazyList
        .from(1)
        .map(i => (i, path.resolve(s"$i.sql")))
        .takeWhile { case (_, p) => Files.isRegularFile(p) }
        .map { case (i, p) =>
          AlterationParser.parseScript(i, Files.readString(p, Charset.forName("UTF-8")), AlterationParser.AllScripts)
        }
        .toList
    } else Nil
  }
}
