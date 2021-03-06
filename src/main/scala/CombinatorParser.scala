package edu.luc.cs.laufer.cs473.expressions

import scala.util.parsing.combinator.JavaTokenParsers
import ast._

object CombinatorParser extends JavaTokenParsers {

  /** expr ::= term { { "+" | "-" } term }* */
  def topLevel: Parser[Expr] =
    rep1(statement) ^^ { case a => Block(a: _*) }

  def expr: Parser[Expr] =
    term ~! rep(("+" | "-") ~ term) ^^ {
      case l ~ r => r.foldLeft(l) {
        case (a, "+" ~ t) => Plus(a, t)
        case (a, "-" ~ t) => Minus(a, t)
      }
    }

  /** term ::= factor { { "*" | "/" | "%" } factor }* */
  def term: Parser[Expr] =
    factor ~! rep(("*" | "/" | "%") ~ factor) ^^ {
      case l ~ r => r.foldLeft(l) {
        case (a, "*" ~ t) => Times(a, t)
        case (a, "/" ~ t) => Div(a, t)
        case (a, "%" ~ t) => Mod(a, t)
      }
    }

  /** factor ::=ident { "." ident }* | number | "+" factor | "-" factor | "(" expr ")" | struct  */
  def factor: Parser[Expr] = (
    rep1sep(ident, ".") ^^ { case v => Select(v: _*) }
    | wholeNumber ^^ { case s => Constant(s.toInt) }
    | "+" ~> factor ^^ { case e => e }
    | "-" ~> factor ^^ { case e => UMinus(e) }
    // | "(" ~ expr ~ ")" ^^ { case _ ~ e ~ _ => e }
    | "(" ~> expr <~ ")"
    | ident ^^ { case s => Var(s) }
    | struct
  )

  //statement   ::= expression ";" | assignment | conditional | loop | block
  def statement: Parser[Expr] =
    expr ~ ";" ^^ { case e ~ _ => e } | assignment | conditional | loop | block

  //assignment  ::= ident "=" expression ";"
  def assignment: Parser[Expr] =
    rep1sep(ident, ".") ~ "=" ~ expr ~ ";" ^^ { case i ~ _ ~ e ~ _ => Assignment(Var(i: _*), e) }

  def loop: Parser[Expr] =
    "while" ~ "(" ~ expr ~ ")" ~ block ^^ {
      case "while" ~ _ ~ e ~ _ ~ b => Loop(e, b)
    }

  def block: Parser[Expr] =
    "{" ~ rep(statement) ~ "}" ^^ {
      case _ ~ statements ~ _ => Block(statements: _*)
    }

  //conditional ::= "if" "(" expression ")" block [ "else" block ]
  def conditional: Parser[Expr] =
    "if" ~ "(" ~ expr ~ ")" ~ block ~ opt("else" ~ block) ^^ {
      case "if" ~ _ ~ e ~ _ ~ l ~ None             => Conditional(e, l, Block())
      case "if" ~ _ ~ e ~ _ ~ l ~ Some("else" ~ r) => Conditional(e, l, r)
    }

  //TODO struct ::= "{" "}" | "{" field { "," field }* "}"

  def struct: Parser[Expr] = (
    "{" ~ "}" ^^ { case _ ~ _ => Struct() }
    | "{" ~! rep1sep(field, ",") ~ "}" ^^ {
      case _ ~ fields ~ _ => Struct(fields: _*)
    }
  )

  //TODO field  ::= ident ":" expr
  def field: Parser[(String, Expr)] = (
    ident ~ ":" ~ expr ^^ { case i ~ _ ~ e => (i, e) }
  )

}
