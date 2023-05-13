package com.bnhp.cli.BnhpCli;

import java.util.Set;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;


import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@SpringBootApplication

@RegisterReflectionForBinding({ User.class, UsersResponse.class })
@ImportRuntimeHints(MyRuntimeHints.class)
public class BnhpCliApplication implements CommandLineRunner, ExitCodeGenerator {

    private int exitCode;
    private BnhpCliCommand command;
    private IFactory factory;

    public BnhpCliApplication(BnhpCliCommand mCommand, IFactory factory) {
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
}

final class MyRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

        String packageName = "io.fabric8.kubernetes.api.model";
        Set<Class<?>> classes = Utils.getAllClassesOfPackage(packageName);
        classes.stream().forEach(aClass -> hints.reflection().registerType(aClass, MemberCategory.values()));

    }
 }

