package lox;

import java.util.List;

abstract class Expr {
	interface Visitor<R> {
		R visitAssignExpr(Assign expr);
		R visitBinaryExpr(Binary expr);
		R visitGroupingExpr(Grouping expr);
		R visitLiteralExpr(Literal expr);
		R visitUnaryExpr(Unary expr);
		R visitVariableExpr(Variable expr);
	}
	static class Assign extends Expr {
		//Fields
		final Token name;
		final Expr value;

		//constructor
		Assign(Token name, Expr value) {
			this.name = name;
			this.value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			 return visitor.visitAssignExpr(this);
		}
	}

	static class Binary extends Expr {
		//Fields
		final Expr left;
		final Token operator;
		final Expr right;

		//constructor
		Binary(Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			 return visitor.visitBinaryExpr(this);
		}
	}

	static class Grouping extends Expr {
		//Fields
		final Expr expression;

		//constructor
		Grouping(Expr expression) {
			this.expression = expression;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			 return visitor.visitGroupingExpr(this);
		}
	}

	static class Literal extends Expr {
		//Fields
		final Object value;

		//constructor
		Literal(Object value) {
			this.value = value;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			 return visitor.visitLiteralExpr(this);
		}
	}

	static class Unary extends Expr {
		//Fields
		final Token operator;
		final Expr right;

		//constructor
		Unary(Token operator, Expr right) {
			this.operator = operator;
			this.right = right;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			 return visitor.visitUnaryExpr(this);
		}
	}

	static class Variable extends Expr {
		//Fields
		final Token name;

		//constructor
		Variable(Token name) {
			this.name = name;
		}

		@Override
		<R> R accept(Visitor<R> visitor) {
			 return visitor.visitVariableExpr(this);
		}
	}


abstract <R> R accept(Visitor <R> visitor);
}
