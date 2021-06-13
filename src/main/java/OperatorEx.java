import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OperatorEx {

    private static final Logger logger = LoggerFactory.getLogger(OperatorEx.class);

    public static void main(String[] args) {
        Publisher<Integer> pub = new Publisher<Integer>(){
            @Override
            public void subscribe(Subscriber<? super Integer> s) {
                s.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        Iterable<Integer> itr = Stream.iterate(1, i -> i + 1).limit(30).collect(Collectors.toList());
                        for (int i : itr) s.onNext(i);
                        s.onComplete();
                    }

                    @Override
                    public void cancel() {

                    }
                });
            }
        };

        Publisher<Integer> mapPub = new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                pub.subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        sub.onSubscribe(s);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        sub.onNext(integer * 10);
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {
                        sub.onComplete();
                    }
                });
            }
        };

        Publisher<Integer> mapPub2 = mapPub2(mapPub, (Function<Integer, Integer>) a -> a + 50);
        Publisher<String> reduce = reduce(mapPub2, "", (a, b) -> a + "-" + b);
        reduce.subscribe(new Subscriber<String>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(String integer) {
                logger.info("onNext : {}", integer);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {
                logger.info("Done");
            }
        });

        System.out.println("Exit");
    }

    public static Publisher<Integer> mapPub2 (Publisher<Integer> pub, Function<Integer, Integer> f) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                pub.subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        sub.onSubscribe(s);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        sub.onNext(f.apply(integer));
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {
                        sub.onComplete();
                    }
                });
            }
        };
    }

    public static <T, R> Publisher<R> reduce(Publisher<T> pub, R init, BiFunction<R, T, R> f) {
        return new Publisher<R>() {
            @Override
            public void subscribe(Subscriber<? super R> sub) {
                pub.subscribe(new Subscriber<T>() {
                    R r = init;

                    @Override
                    public void onSubscribe(Subscription s) {
                        sub.onSubscribe(s);
                    }

                    @Override
                    public void onNext(T t) {
                        r = f.apply(r, t);
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {
                        sub.onNext(r);
                        sub.onComplete();
                    }
                });
            }
        };
    }

}
