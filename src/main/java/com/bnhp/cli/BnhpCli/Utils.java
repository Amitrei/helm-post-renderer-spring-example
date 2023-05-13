package com.bnhp.cli.BnhpCli;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

public class Utils {

    public static void populateWithNestedClasses(Collection<Class<?>> classes, Class<?> clazz, String packageName) {
        if (clazz.getClasses().length == 0) {
            classes.add(clazz);
            return;
        }

        if (clazz.getCanonicalName().contains(packageName)) {
            classes.add(clazz);

            for (Class<?> aClass : clazz.getClasses()) {
                populateWithNestedClasses(classes, aClass, packageName);
            }
        }

    }

    public static Set<Class<?>> getAllClassesOfPackage(String packageName) {
        Reflections reflections = new Reflections(packageName, new SubTypesScanner(false));
        Set<Class<?>> classes = new HashSet<>();
        for (Class<?> clazz : reflections.getSubTypesOf(Object.class)) {
            Utils.populateWithNestedClasses(classes, clazz, packageName);
        }
        return classes;

    }

    public static String getUserInputWithTimeout(int timeoutInSec) {

        final ExecutorService l = Executors.newFixedThreadPool(1);

        Callable<String> k = () -> {
            String content = "";
            try (Scanner scanner = new Scanner(System.in)) {
                while (scanner.hasNextLine()) {
                    content += scanner.nextLine() + "\n";
                }
            } catch (Exception e) {
            }
            return content;
        };

        LocalDateTime start = LocalDateTime.now();
        Future<String> g = l.submit(k);
        while (ChronoUnit.SECONDS.between(start, LocalDateTime.now()) < timeoutInSec) {
            if (g.isDone()) {
                try {
                    return g.get();
                } catch (InterruptedException | ExecutionException | IllegalArgumentException e) {
                    g = l.submit(k);
                }
            }
        }
        g.cancel(true);
        return null;
    }

}
