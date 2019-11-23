package edu.luc.cs.laufer.cs473.expressions

import org.scalatest.FunSuite
import TestFixtures._
import edu.luc.cs.laufer.cs473.expressions.behaviors.Cell
import edu.luc.cs.laufer.cs473.expressions.ast.Expr

class TestExecute extends FunSuite {
  def testExecute(description: String, ast: Expr, s: store) = {
    val s1 = result.get
    evaluate(s1)(ast)
    test("Execution -> " + description) { assert(s1 === s) }
  }

  testExecute("assignment", simple1, store1)
  testExecute("multiple assignments", simple2, store2)
  testExecute("assignment in conditional", simple4, store4)
  testExecute("assignment in else branch", simple6, store6)
  testExecute("assignment of struct", simple11, store11)