package test;

import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class FutureMain {

    @Test
    public void test0(){
        System.out.println(UUID.randomUUID().toString().replaceAll("-",""));
    }

    @Test
    public void test1() throws ExecutionException, InterruptedException {
        final CompletableFuture<Void> completableFuture = CompletableFuture.allOf(CompletableFuture.runAsync(() -> {
            try {
                System.out.println("线程1开始");
                Thread.sleep(200);
                System.out.println("线程1");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }),CompletableFuture.runAsync(() -> {
            try {
                System.out.println("线程2开始");
                Thread.sleep(50);
                System.out.println("线程2");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }),CompletableFuture.runAsync(() -> {
            try {
                System.out.println("线程3开始");
                Thread.sleep(100);
                System.out.println("线程3");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }));

        completableFuture.get();

    }
}
