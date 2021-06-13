import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IntervalEx {

    private static final Logger log = LoggerFactory.getLogger(IntervalEx.class);

    public static void main(String[] args) {
        Publisher<Integer> pub = sub -> {
            sub.onSubscribe(new Subscription() {
                int no = 0;
                final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

                @Override
                public void request(long n) {
                    exec.scheduleAtFixedRate(() -> {
                        sub.onNext(no++);
                    }, 0, 300, TimeUnit.MILLISECONDS);
                }

                @Override
                public void cancel() {
                    exec.shutdown();
                }
            });
        };

        Publisher<Integer> takePub = sub -> {
            take(pub, sub, 5);
        };

        takePub.subscribe(new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription s) {
                log.debug("onSubscribe.");
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer integer) {
                log.debug("onNext : {}", integer);
            }

            @Override
            public void onError(Throwable t) {
                log.debug("onError : {}", t.getMessage());
            }

            @Override
            public void onComplete() {
                log.debug("onComplete");
            }
        });
    }

    private static void take(Publisher<Integer> pub, Subscriber<? super Integer> sub, int take) {
        pub.subscribe(new Subscriber<Integer>() {
            int current = 0;
            Subscription s;

            @Override
            public void onSubscribe(Subscription s) {
                this.s = s;
                sub.onSubscribe(s);
            }

            @Override
            public void onNext(Integer integer) {
                if (current >= take) {
                    sub.onComplete();
                    s.cancel();
                }
                else {
                    sub.onNext(integer);
                    current++;
                }
            }

            @Override
            public void onError(Throwable t) {
                sub.onError(t);
            }

            @Override
            public void onComplete() {
                sub.onComplete();
            }
        });
    }
}
