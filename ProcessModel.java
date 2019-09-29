import sun.misc.Signal;
import sun.misc.SignalHandler;

public class ProcessModel extends Thread {

	public ProcessModel() {
		SigHandlerUsr2 sigHandlerUsr2 = new SigHandlerUsr2(this);
		SigHandlerInt sigHandlerTerm = new SigHandlerInt(this);
		SigHandlerTerm sigHandlerInt = new SigHandlerTerm(this);

		Signal signalTerm = new Signal("TERM");
		Signal signalInt = new Signal("INT");
		Signal signalUsr2 = new Signal("USR2");

		Signal.handle(signalInt, sigHandlerInt);
		Signal.handle(signalTerm, sigHandlerTerm);
		Signal.handle(signalUsr2, sigHandlerUsr2);

		this.start();
	}

	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(100);
			} catch (Exception e){
				//exception
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static class SigHandlerUsr2 implements SignalHandler {
		ProcessModel p;

		private SigHandlerUsr2(ProcessModel p) {
			super();
			this.p = p;
		}

		@Override
		public void handle(Signal signal) {
			System.out.format("Handling signal: %s\n", signal.toString());
		}
	}

	@SuppressWarnings("deprecation")
	public static class SigHandlerTerm implements SignalHandler {
		ProcessModel p;

		private SigHandlerTerm(ProcessModel p) {
			super();
			this.p = p;
		}

		@Override
		public void handle(Signal signal) {
			System.out.format("Handling signal: %s\n", signal.toString());
			p.interrupt();
		}
	}

	@SuppressWarnings("deprecation")
	public static class SigHandlerInt implements SignalHandler {
		ProcessModel p;

		private SigHandlerInt(ProcessModel p) {
			super();
			this.p = p;
		}

		@Override
		public void handle(Signal signal) {
			System.out.format("Handling signal: %s\n", signal.toString());
			p.interrupt();
		}
	}
}
