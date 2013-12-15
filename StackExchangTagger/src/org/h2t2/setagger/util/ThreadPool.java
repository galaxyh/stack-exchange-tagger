package org.h2t2.setagger.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ThreadPool {

	private class RunnableWrapper implements Runnable {

		private Runnable command;

		private Runnable callback;

		public RunnableWrapper (Runnable command, Runnable callback) {
			this.command = command;
			this.callback = callback;
		}

		@Override
		public void run () {
			this.command.run();
			this.callback.run();
		}
	}

	private Executor pool;

	public ThreadPool (int nThreads) {
		if (nThreads <= 0) {
			throw new IllegalArgumentException();
		}
		this.pool = Executors.newFixedThreadPool(nThreads);
	}

	public run (Runnable command, Runnable callback) {
		RunnableWrapper wrapper = new RunnableWrapper(command, callback);
		this.pool.execute(wrapper);
	}

	public run (Runnable command) {
		this.pool.execute(command);
	}

}