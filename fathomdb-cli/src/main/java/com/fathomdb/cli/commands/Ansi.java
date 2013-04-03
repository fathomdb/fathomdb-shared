package com.fathomdb.cli.commands;

import java.io.PrintWriter;

public class Ansi {

	static class AnsiWriter {
		private final PrintWriter writer;

		public AnsiWriter(PrintWriter writer) {
			this.writer = writer;
		}

		void escape(int code) {
			writer.write("\u001b[" + code + "m");
		}
	}

	public static abstract class Action {
		public abstract Action doAction(AnsiWriter writer);
	}

	public static final Action RESET = new Action() {
		@Override
		public Action doAction(AnsiWriter writer) {
			writer.escape(0);
			return null;
		}
	};

	public static final Action TEXT_COLOR_DEFAULT = new Action() {
		@Override
		public Action doAction(AnsiWriter writer) {
			writer.escape(39);
			return null;
		}
	};

	static class ColorAction extends Action {
		final int code;

		public ColorAction(int code) {
			this.code = code;
		}

		@Override
		public Action doAction(AnsiWriter writer) {
			writer.escape(code);
			return TEXT_COLOR_DEFAULT;
		}
	}

	public static enum Color {
		Default(39), Black(30), Red(31), Green(32), Yellow(33), Blue(34), Magenta(35), Cyan(36), White(37);

		final int code;

		private Color(int code) {
			this.code = code;
		}
	}

	public static final Action TEXT_COLOR_BLACK = new ColorAction(30);
	public static final Action TEXT_COLOR_RED = new ColorAction(31);
	public static final Action TEXT_COLOR_GREEN = new ColorAction(32);
	public static final Action TEXT_COLOR_YELLOW = new ColorAction(33);
	public static final Action TEXT_COLOR_BLUE = new ColorAction(34);
	public static final Action TEXT_COLOR_MAGENTA = new ColorAction(35);
	public static final Action TEXT_COLOR_CYAN = new ColorAction(36);
	public static final Action TEXT_COLOR_WHITE = new ColorAction(37);

	private final AnsiWriter writer;

	private boolean needReset;

	public Ansi(PrintWriter writer) {
		this.writer = new AnsiWriter(writer);
	}

	public void reset() {
		if (needReset) {
			doAction(RESET);
			needReset = false;
		}
	}

	public Action doAction(Action action) {
		if (action != RESET) {
			needReset = true;
		}
		return action.doAction(writer);
	}

	@Deprecated
	public Action setColorRed() {
		return doAction(TEXT_COLOR_RED);
	}

	@Deprecated
	public Action setColorGreen() {
		return doAction(TEXT_COLOR_GREEN);
	}

	@Deprecated
	public Action setColorYellow() {
		return doAction(TEXT_COLOR_YELLOW);
	}

	@Deprecated
	public Action setColorBlue() {
		return doAction(TEXT_COLOR_BLUE);
	}

	public void print(String s) {
		writer.writer.print(s);
	}

	public void println() {
		writer.writer.println();
	}

	public void println(Color color, String s) {
		if (color != Color.Default) {
			writer.escape(color.code);
		}

		writer.writer.print(s);

		if (color != Color.Default) {
			writer.escape(Color.Default.code);
		}

		writer.writer.println();
	}

}
