package com.fathomdb.proxy.http.server;

import com.fathomdb.config.ConfigurationManager;

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
		System.out.println("Signal handler called for signal "+sig);
        try {
//            // Output information for each thread
//            Thread[] threadArray = new Thread[Thread.activeCount()];
//            int numThreads = Thread.enumerate(threadArray);
//            System.out.println("Current threads:");
//            for (int i=0; i < numThreads; i++) {
//                System.out.println("    "+threadArray[i]);
//            }
        	ConfigurationManager.INSTANCE.refresh();
            
            // Chain back to previous handler, if one exists
            if ( oldHandler != SIG_DFL && oldHandler != SIG_IGN ) {
                oldHandler.handle(sig);
            }
        } catch (Exception e) {
            System.err.println("Signal handler failure");
            e.printStackTrace(System.err);
        }
	}
}
