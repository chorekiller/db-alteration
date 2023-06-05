package net.isammoc.alterations.util

import scala.util.control.NoStackTrace

class AlterationException(problem: String) extends Exception(problem) with NoStackTrace
