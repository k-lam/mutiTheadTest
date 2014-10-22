package kl.test.multiThread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
***┏┓           ┏┓
*┏┛┻━━━┛┻┓
*┃                          ┃
*┃           ━           ┃
*┃     ┳┛  ┗┳    ┃
*┃                          ┃
*┃ ``` ┻   ```┃
*┃                         ┃
*┗━┓          ┏━┛
*****┃       ┃Code is far away from bug with the animal protecting.
*****┃       ┃神兽护佑,代码无bug.
*****┃       ┗━━━━  ┓
*****┃                            ┣┓
*****┃                            ┏┛
*****┗ ┓ ┓┏━┳┓┏┛
*******┃┫┫    ┃┫┫
*******┗┻┛    ┗┻┛
*
*基于消息机制的Thread,有消息来时执行，没有时阻塞等待
*
* @author K.L
 */
public class MessageBaseThread extends Thread {

	public static void main(String[] args) {
		MessageBaseThread msgThread = new MessageBaseThread();
		msgThread.start();
		msgThread.sendRunnable(new PrintRunnable("start:"));
		for (int i = 10; i != 0; i--) {
			msgThread.sendRunnable(new PrintRunnable(String
					.valueOf((char) ('A' + i))));
		}
		msgThread.sendRunnable(new PrintRunnable("A"));
	}

	BlockingQueue<Runnable> mRunnableQueue = new ArrayBlockingQueue<Runnable>(
			10);

	public void sendRunnable(Runnable runnable) {
		try {
			mRunnableQueue.put(runnable);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				Runnable runnable = mRunnableQueue.take();//mRunnableQueue空会阻塞，直至有新的Runnable放进队列
				runnable.run();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	static class PrintRunnable implements Runnable {
		String msg;

		public PrintRunnable(String msg) {
			this.msg = msg;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(msg);
		}
	}
}