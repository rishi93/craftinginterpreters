package lox;

import java.util.List;

abstract class Stmt {
	interface Visitor<R> {
		R visitExpressionStmt(Expression stmt);
		R visitPrintStmt(Print stmt);
		R visitVarStmt(Var stmt);
	}
	static class Expression extends Stmt {
		//Fields
		final Expr expression;

		//constructor
		Expression(Expr expression) {
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			 return visitor.visitExpressionStmt(this);
		}
	}

	static class Print extends Stmt {
		//Fields
		final Expr expression;

		//constructor
		Print(Expr expression) {
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			 return visitor.visitPrintStmt(this);
		}
	}

	static class Var extends Stmt {
		//Fields
		final Token name;
		final Expr initializer;

		//constructor
		Var(Token name, Expr initializer) {
			this.name = name;
			this.initializer = initializer;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			 return visitor.visitVarStmt(this);
		}
	}


abstract <R> R accept(Visitor <R> visitor);
}
