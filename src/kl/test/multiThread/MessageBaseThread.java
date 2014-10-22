package kl.test.multiThread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
***����           ����
*�����ߩ��������ߩ�
*��                          ��
*��           ��           ��
*��     �ש�  ����    ��
*��                          ��
*�� ``` ��   ```��
*��                         ��
*������          ������
*****��       ��Code is far away from bug with the animal protecting.
*****��       �����޻���,������bug.
*****��       ����������  ��
*****��                            �ǩ�
*****��                            ����
*****�� �� �������ש�����
*******���ϩ�    ���ϩ�
*******���ߩ�    ���ߩ�
*
*������Ϣ���Ƶ�Thread,����Ϣ��ʱִ�У�û��ʱ�����ȴ�
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
				Runnable runnable = mRunnableQueue.take();//mRunnableQueue�ջ�������ֱ�����µ�Runnable�Ž�����
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