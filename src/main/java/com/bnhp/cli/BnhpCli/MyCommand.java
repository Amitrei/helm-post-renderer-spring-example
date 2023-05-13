package com.bnhp.cli.BnhpCli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.bnhp.cli.BnhpCli.BnhpCliApplication.getUserInputWithTimeout;

@Component
@Command(name = "myCommand", mixinStandardHelpOptions = true)
public class MyCommand implements Callable<Integer> {

    @Option(names = "-v", description = "My Desc")
    boolean verbose;

    @Option(names = "-i", description = "input for manifests")
    boolean shouldInput;

    @Override
    public Integer call() throws IOException {


        if (shouldInput) {
            String input = getUserInputWithTimeout(5);

            if (input != null) {
                List<String> split = List.of(input.split("---"));

                for (String resource : split) {

                    if (resource != null && !resource.equals("null") && !resource.equalsIgnoreCase("")) {
                        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
                        KubernetesDeserializer kubernetesDeserializer = new KubernetesDeserializer();

                        KubernetesResource h = kubernetesDeserializer.deserialize(objectMapper.createParser(resource), objectMapper.getDeserializationContext());
                        if (h instanceof HasMetadata k) {
                            if (k.getKind().equalsIgnoreCase("deployment")) {
                                k.getMetadata().setAnnotations(Map.of("my-anno", "bbbb"));
                            }
                            System.out.println(objectMapper.writeValueAsString(h));
                        }
                    }
                }
            }
        }

        if (verbose) {
            WebClient wb = WebClient.builder()
                    .baseUrl("https://reqres.in/api/users")
                    .build();


            MyResponse block = wb.get().retrieve().bodyToMono(MyResponse.class).block();
            System.out.println(block);

            return 0;
        }
        return 0;

    }
}




