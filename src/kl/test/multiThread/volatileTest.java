package kl.test.multiThread;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

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
*N条线程，每条线程都对vi进行perAddCount次加1操作。看看最后结果是否等于N * perAddCount
*由于多线程下，有些结果可能要很多次测试才会出现，所以testVi(int count)就是用来测试count次的
* @author K.L
 */
public class volatileTest {
	
	static volatile int vi = 0;
	static AtomicInteger finishCount = new AtomicInteger(0);
	static final int N = 10;
	static final int preAddCount = 5000;
	static Object vLock = new Object();
	static boolean useSync = false;

	public static void main(String[] args) {
		testVi(30);
	}
	
	static void testVi(int count){
		while(count-- >= 0){
			CountDownLatch startLatch = new CountDownLatch(1);
			for(int i = 0; i != N;i++){
				new Thread(new Sum(count,startLatch)).start();
			}
			startLatch.countDown();
			synchronized (finishCount) {
				if(finishCount.get() != N){
					try {
						finishCount.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					finishCount.set(0);
					vi = 0;
				}
			}
		}
	}
	
	static class Sum implements Runnable{
		int id;
		CountDownLatch startLatch;
		Sum(int id,CountDownLatch startLatch){
			this.startLatch = startLatch;
			this.id = id;
		}
		@Override
		public void run() {
			try {
				startLatch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for(int i = 0;i != preAddCount;i++){
				if(useSync){
					synchronized (vLock) {
						vi++;
					}
				}else {
					vi++;
				}
			}
			synchronized (finishCount) {
				if(finishCount.incrementAndGet() == N){
					System.out.println(id+":"+(vi == preAddCount * N)+"  "+vi);
					finishCount.notify();
				}
			}
		}
	};
}
