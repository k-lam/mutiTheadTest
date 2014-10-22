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
* ��TestReadWriteLockUseSysnc��ͬ���޸���TestReadWriteLockUseSysnc��
* ��//������ܳ������⣬writeLock�ͷź󣬵�readCountδ�ı�ǰ/readCountLockδ��ȡǰ��
* ���֣���ʵֻ��Ҫһ���ٽ��������������readCount��needWrite��needWrite������֪ͨ���߳�
* ��Ҫд�����Զ�Ҫ�����Լ���readCount���ڶ��Ķ��߳�������д�߳�ͨ���鿴����������������д
* ���������Լ���������д���ȣ�����д�߳��Ȱ�needWrite��Ϊtrue�����ж����������߳����ȿ�����û��д
* �߳���Ҫд����readCount++��ע�⿴�����ͻ��ѵ�������
 */
public class TestReadWriteLockWithSysnc2 {

	static volatile int readCount = 0;
	static volatile boolean needWrite = false;
	static StringBuilder book = new StringBuilder("this is a book about...");
	static CountDownLatch startLatch = new CountDownLatch(1);

	/**
	 * �ٽ�����readCount needWrite
	 */
	static Object mutex = new Object();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Thread(new Reader("Tiffany")).start();
		new Thread(new Reader("YoYo")).start();
		new Thread(new Writer()).start();
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
				synchronized (mutex) {
					//synchronized��һ������������notifyAll֮��
					//��Ҫ��While���ٴ��ж��Ƿ���������ˣ�synchronized�����Ⱥܴ�
					//�����Lock�������ΪneedWrite��readCount�ֱ�newһ��Condition���ֱ�ȥ������ȥ���ѣ�
					while (needWrite) {
						try {
							mutex.wait();// �Զ��ػ���
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					readCount++;
				}
				reading(i);
			}
		}

		void reading(int i) {
			//System.out.print(name + " read " + needWrite);
			int t = getRandom();
			try {
				Thread.sleep(t);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(name + " read " + i + " times ,content is "
					+ book.toString());
			synchronized (mutex) {
				readCount--;
				if (readCount == 0) {
					//Ϊʲô��all����Ϊ�п�����reading��ΪneedWrite
					//������
					mutex.notifyAll();
				}
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
				synchronized (mutex) {
					needWrite = true;
					while (readCount != 0) {
						try {
							mutex.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					System.out.print("writing...");
					try {
						Thread.sleep(getRandom() * 3);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println(" i write " + book.append(contents[i]));
					needWrite = false;
					mutex.notifyAll();
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
