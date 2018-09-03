package com.beidao.netty.async.callbacks;

/**
 * @author 0200759
 *
 */
public class Worker {
	
	public void doWork() {
		
		Fetcher fetcher = new MyFetcher(new Data(1, 0));
		//invoking callback
		fetcher.fetchData(new FetcherCallback() {
			
			public void onError(Throwable cause) {
				System.out.println("An error accour: " + cause.getMessage());
			}
			
			public void onData(Data data) throws Exception {
				//System.out.println(xx)，括号里面的“xx”如果不是String类型的话，就自动调用xx的toString()方法
				System.out.println("Data received: " + data);
				
			}
		});
	}

	public static void main(String[] args) {
		Worker w = new Worker();
		w.doWork();
	}

}
