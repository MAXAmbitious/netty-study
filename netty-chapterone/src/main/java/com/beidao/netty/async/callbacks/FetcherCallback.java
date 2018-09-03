package com.beidao.netty.async.callbacks;

/**
 * @author 0200759
 *
 */
public interface FetcherCallback {

	void onData(Data data) throws Exception;
	void onError(Throwable cause);
}
