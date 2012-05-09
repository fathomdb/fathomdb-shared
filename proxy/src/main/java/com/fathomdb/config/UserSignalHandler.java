package com.fathomdb.config;

import sun.misc.Signal;
import sun.misc.SignalHandler;

public class UserSignalHandler implements SignalHandler {
	private SignalHandler oldHandler;

	public static UserSignalHandler install() {
		Signal signal = new Signal("USR2");
		UserSignalHandler handler = new UserSignalHandler();
		handler.oldHandler = Signal.handle(signal, handler);
		return handler;
	}

	@Override
	public void handle(Signal sig) {
		// TODO: What is safe? Can we log? Should we just signal a lock?
		System.out.println("Signal handler called for signal " + sig);
		try {
			ConfigurationManager.INSTANCE.refresh();

			// Chain back to previous handler, if one exists
			if (oldHandler != SIG_DFL && oldHandler != SIG_IGN) {
				oldHandler.handle(sig);
			}
		} catch (Exception e) {
			System.err.println("Signal handler failure");
			e.printStackTrace(System.err);
		}
	}
}
