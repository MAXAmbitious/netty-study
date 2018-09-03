package com.beidao.netty.async.callbacks;

public class MyFetcher implements Fetcher{

	final Data data;
	
	public MyFetcher(Data data){
		this.data = data;
	}
	/* 
	 * excute method
	 * @see com.beidao.netty.async.callbacks.Fetcher#fetchData(com.beidao.netty.async.callbacks.FetcherCallback)
	 */
	public void fetchData(FetcherCallback callback) {
		try{
			//callback method
			callback.onData(data);
		} catch (Exception e) {
			//callback method
			callback.onError(e);
		}
	}

}
