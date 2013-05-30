package com.busdrone;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public abstract class Fetcher extends Thread {
	int sleepSecs = 0;
	BusReportServer server;
	JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");
	
	@Override
	public void run() {
		while (true) {
			Jedis db = jedisPool.getResource();
			try {
				this.runOnce(db);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				jedisPool.returnResource(db);
				try {
					Thread.sleep(this.sleepSecs * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public abstract void runOnce(Jedis db) throws Exception;
}