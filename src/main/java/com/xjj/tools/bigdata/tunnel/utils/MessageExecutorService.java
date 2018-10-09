package com.xjj.tools.bigdata.tunnel.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 类说明
 * Created on 2014年5月7日
 * 海南新境软件有限公司
 * @author cjh
 */
public class MessageExecutorService {
	private ExecutorService executorService;// 线程池
	private int POOL_SIZE = 10;// 单个CPU线程池大小
	
	private static MessageExecutorService instance;
	
	public MessageExecutorService(int poolSize){
		this.POOL_SIZE = poolSize;
		//executorService = Executors.newFixedThreadPool(Runtime.getRuntime()
		//		.availableProcessors() * POOL_SIZE);
		executorService =  Executors.newCachedThreadPool();
	}
	
	public static MessageExecutorService getInstance(){
		if(instance==null)
			instance = new MessageExecutorService(10);
		return instance;
	}
	
	public void execute(Runnable runner){
		executorService.execute(runner);
	}
	public void shutdown(){
		executorService.shutdown();
	}
}
