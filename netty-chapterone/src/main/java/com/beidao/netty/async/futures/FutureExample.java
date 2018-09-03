package com.beidao.netty.async.futures;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FutureExample {

	public static void main(String[] args) {
		ExecutorService executorService = Executors.newCachedThreadPool();
		Runnable task1 = new Runnable() {
			
			public void run() {
				System.out.println("i am task1...");
			}
		};
		
		Callable<Integer> task2 = new Callable<Integer>() {
			public Integer call() throws Exception{
				return new Integer(100);
			}
		};
		
		Future<?> f1 = executorService.submit(task1);
		Future<Integer> f2 = executorService.submit(task2);
		System.out.println("task1 is completed?" + f1.isDone());
		System.out.println("task2 is completed?" + f2.isDone());
		
		//waiting task1 completed
		while(f1.isDone()){
			//callback code
			System.out.println("task1 completed!");
			break;
		}
		
		//waiting task2 completed
		while (f2.isDone()) {
			//callback code
			System.out.println("task2 completed!");
			break;
		}
	}

}
