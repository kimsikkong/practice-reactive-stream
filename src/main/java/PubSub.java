import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;


public class PubSub {

    private static final Logger log = LoggerFactory.getLogger(PubSub.class);

    public static void main(String[] args) {
        Publisher<Integer> pub = new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                sub.onSubscribe(new Subscription() {
                    List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);

                    @Override
                    public void request(long n) {
                        for (int i : list) {
                            sub.onNext(i);
                        }
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };

        pub.subscribe(new Subscriber<Integer>() {
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
                log.debug("onComplete.");
            }
        });
    }
}
