package com.bnhp.cli.BnhpCli;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.runtime.RawExtension;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.yaml.snakeyaml.Yaml;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@SpringBootApplication

@RegisterReflectionForBinding({MyObject.class, MyResponse.class})
@ImportRuntimeHints(MyRuntimeHints.class)
public class BnhpCliApplication implements CommandLineRunner, ExitCodeGenerator {
    private static final ExecutorService l = Executors.newFixedThreadPool(1);

    private int exitCode;
    private MyCommand command;
    private IFactory factory;

    public BnhpCliApplication(MyCommand mCommand, IFactory factory) {
        this.command = mCommand;
        this.factory = factory;
    }

    public static void main(String[] args) {


        System.exit(SpringApplication.exit(SpringApplication.run(BnhpCliApplication.class, args)));

    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

    @Override
    public void run(String... args) {
        exitCode = new CommandLine(command, factory).execute(args);

    }

    public static String getUserInputWithTimeout(int timeout) {
        Callable<String> k = () -> {
            String content = "";
            Scanner scanner = new Scanner(System.in);

            while (scanner.hasNextLine()) {
                content += scanner.nextLine() + "\n";
            }
            return content;
        };
        LocalDateTime start = LocalDateTime.now();
        Future<String> g = l.submit(k);
        while (ChronoUnit.SECONDS.between(start, LocalDateTime.now()) < timeout) {
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


    public static void addClassToList(List<Class<?>> list, Class<?> clazz) {
        if (clazz.getClasses().length == 0) {
            list.add(clazz);
        } else {

            if (clazz.getCanonicalName().contains("fabric8")) {
                list.add(clazz);
                Arrays.stream(clazz.getClasses()).forEach(aClass -> addClassToList(list, aClass));
            }
        }

    }
}

class MyRuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

        Reflections reflections = new Reflections("io.fabric8.kubernetes.api.model", new SubTypesScanner(false));
        List<Class<?>> classes = new ArrayList<>();
        for (Class<?> clazz : reflections.getSubTypesOf(Object.class)) {
            BnhpCliApplication.addClassToList(classes, clazz);
        }

        classes.stream().forEach(aClass -> hints.reflection().registerType(aClass, MemberCategory.values()));

        hints.reflection().registerType(AnyType.class, MemberCategory.values())
                .registerType(AnyType.Serializer.class, MemberCategory.values());


    }
}

