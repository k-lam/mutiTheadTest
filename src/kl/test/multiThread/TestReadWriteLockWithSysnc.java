package kl.test.multiThread;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

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
* @author K.L
* ��synchronized��ɵĶ�д����ֻ��һ��д�������������ʱ�򣬲�����д��д��ʱ�򲻿��Զ�������ʱ�������˿��Զ�
* 
 */
public class TestReadWriteLockWithSysnc {
	static Object readCountLock = new Object();
	static volatile int readCount = 0;
	static Object writeLock = new Object();
	static volatile boolean needWrite = false;
	static StringBuilder book = new StringBuilder("this is a book about...");
	static CountDownLatch startLatch = new CountDownLatch(1);//����һ��ʼ��

	//static Object mutex = new Object();
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Thread(new Writer()).start();
		new Thread(new Reader("Tiffany")).start();
		new Thread(new Reader("YoYo")).start();
		new Thread(new Reader("Kate")).start();
		startLatch.countDown();
	}

	static Random random = new Random();

	public static int getRandom() {
		return random.nextInt(301) + 200;
	}

	public static class Reader implements Runnable {
		String name;

		public Reader(String name) {
			this.name = name;
		}

		@Override
		public void run() {
			try {
				startLatch.await();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			for (int i = 0; i != 20; i++) {
				synchronized (writeLock) {
					if (needWrite) {
						// System.out.println("r needWrite is " + needWrite);
						try {
							writeLock.wait();// �Զ��ػ���
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				//������ܳ������⣬writeLock�ͷź󣬵�readCountδ�ı�ǰ/readCountLockδ��ȡǰ
				reading(i);
			}
		}

		void reading(int i) {
			System.out.print(name + " read ");
			synchronized (readCountLock) {
				readCount++;
			}
			int t = getRandom();
			try {
				Thread.sleep(t);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(name + " read " + i + " times ,content is "
					+ book.toString());
			synchronized (readCountLock) {
				readCount--;
				readCountLock.notify();
			}
		}
	}

	static class Writer implements Runnable {
		String[] contents = new String[] { " a story.", "Long long ago,",
				"a preson", " called xiao ming.", "One day,", "he ", "died." };

		public Writer() {

		}

		@Override
		public void run() {
			try {
				startLatch.await();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			for (int i = 0; i != contents.length; i++) {
				needWrite = true;
				synchronized (writeLock) {
					synchronized (readCountLock) {
						while (readCount != 0) {
							try {
								readCountLock.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
					// while (readCount != 0);
					System.out.print("writing...");
					try {
						Thread.sleep(getRandom() * 3);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println(" i writer " + book.append(contents[i]));
					needWrite = false;
					writeLock.notifyAll();
				}
				try {
					Thread.sleep(700);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
