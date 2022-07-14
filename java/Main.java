import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
	public static void main(String[] args) throws InterruptedException {
		int threads = 100000;
		int type = -1;
		int coreSize = 1;
		if (args.length > 0) {
			threads = Integer.parseInt(args[0]);
		}
		if (args.length > 1) {
			type = Integer.parseInt(args[1]);
		}
		if (args.length > 2) {
			coreSize = Integer.parseInt(args[2]);
		}
		int index = 0;
		System.out.printf("threads: %d\ttype: %s\tcoreSize: %d%n", threads, type < 0 ? "-1" : Integer.toBinaryString(type), coreSize);
		ExecuteType[] executeTypes = ExecuteType.values();
		for (ExecuteType executeType : executeTypes) {
			index++;
			if (type < 0 || (type & (1 << executeTypes.length - index)) > 0) {
				run(threads, executeType.withCoreSize(coreSize));
			}
		}
//		Thread.sleep(60000);
	}

	public static void run(int threads, ExecuteType executeType) {
		System.out.println("-----------------" + executeType.name() + "-----------------");
		try {
			run(true, threads, executeType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(LocalDateTime.now());
		try {
			run(false, threads, executeType);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void run(boolean isVirtual, int threads, ExecuteType executeType) {
		LocalDateTime start = LocalDateTime.now();
		AtomicInteger count = new AtomicInteger(0);
		AtomicInteger nowInUse = new AtomicInteger(0);
		AtomicInteger maxInUse = new AtomicInteger(0);
		Runnable task = () -> {
			int[] stoarge = new int[1024];
			nowInUse.incrementAndGet();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			if (nowInUse.get() > maxInUse.get()) {
				maxInUse.set(nowInUse.get());
			}
			count.incrementAndGet();
			nowInUse.decrementAndGet();
//			try {
//				Thread.sleep(10);
//			} catch (InterruptedException e) {
//				throw new RuntimeException(e);
//			}
		};
		ExecutorService executorService = null;
		if (executeType.isPool) {
			executorService = executeType.exec(isVirtual ? Thread.ofVirtual().factory() : Thread.ofPlatform().factory());
			assert executorService != null;
			for (int i = 0; i < threads; i++) {
				executorService
						.submit(task);
			}
		} else {
			if (isVirtual) {
				for (int i = 0; i < threads; i++) {
					Thread.ofVirtual().name("virtual-" + i).start(task);
				}
			} else {
				for (int i = 0; i < threads; i++) {
					Thread.ofPlatform().name("platform-" + i).start(task);
				}
			}
		}
		while (count.get() < threads) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
		if(executorService!=null){
			executorService.shutdown();
		}
		LocalDateTime end = LocalDateTime.now();
		long seconds = Duration.between(start, end).getSeconds();
		long nano = Duration.between(start, end).getNano();
		System.out.printf("isVirtual: %s\tfinal: %ds | %d\tmaxInUse: %d\n", (isVirtual ? "Y" : "N"), seconds, nano, maxInUse.get());
	}


	public static enum ExecuteType {
		THREAD(false), POOL_CACHED, POOL_FIXED, POOL_PER, POOL_SCHEDULED, POOL_SINGLE,POOL_SINGLE_SCHEDULED;

		// 核心线程数量
		public int coreSize;
		// 是否是线程池方式
		public final boolean isPool;

		ExecuteType() {
			isPool = true;
		}

		ExecuteType(boolean isPool) {
			this.isPool = isPool;
		}

		public ExecuteType withCoreSize(int coreSize) {
			this.coreSize = coreSize;
			return this;
		}

		// 池化方式
		public ExecutorService exec(ThreadFactory factory) {
			switch (this) {
				case POOL_CACHED:
					return Executors.newCachedThreadPool(factory);
				case POOL_FIXED:
					return Executors.newFixedThreadPool(coreSize, factory);
				case POOL_PER:
					return Executors.newThreadPerTaskExecutor(factory);
				case POOL_SCHEDULED:
					return Executors.newScheduledThreadPool(coreSize, factory);
				case POOL_SINGLE:
					return Executors.newSingleThreadExecutor(factory);
				case POOL_SINGLE_SCHEDULED:
					return Executors.newSingleThreadScheduledExecutor(factory);
			}
			return null;
		}
	}
}
