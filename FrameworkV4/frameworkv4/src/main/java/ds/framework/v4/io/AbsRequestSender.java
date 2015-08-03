package ds.framework.v4.io;

import ds.framework.v4.data.AbsAsyncData.LoaderThread;

abstract public class AbsRequestSender extends LoaderThread {
		
	private Object mRequestResult;
	
	@Override
	protected boolean runCycle(Thread in) {
		try {
			mRequestResult = createCycle().run();
			return true;
		} catch(Throwable e) {
			mRequestResult = new RequestError(e);
			return false;
		}
	}
	
	@Override
	public Object createResult(boolean success) {
		return mRequestResult;
	}

	/**
	 * create a 'request cycle' depending state
	 * 
	 * @return
	 * @throws Throwable
	 */
	public abstract Cycle createCycle() throws Throwable;
	
	/**
	 * An independent cycle in a request chain 
	 * 
	 * @class Cycle
	 */
	abstract static public class Cycle {
		
		/**
		 * send your request here and return the result
		 * 
		 * @return
		 * @throws Throwable
		 */
		public abstract Object run() throws Throwable;
	}
	
	/**
	 * @class RequestError
	 */
	static public class RequestError {
		Throwable mException;
		
		public RequestError(Throwable exception) {
			mException = exception;
		}
		
		public Throwable getException() {
			return mException;
		}
	}
}
