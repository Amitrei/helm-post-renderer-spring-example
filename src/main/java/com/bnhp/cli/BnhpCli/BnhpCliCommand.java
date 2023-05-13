package com.bnhp.cli.BnhpCli;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(name = "bnhp-cli", mixinStandardHelpOptions = true)
public class BnhpCliCommand implements Callable<Integer> {

    @Option(names = "--api", description = "Execute an API call to dummy rest service")
    boolean executeApiCall;

    @Option(names = "-i", description = "Enrich manifests from input")
    boolean enrichManifests;

    @Override
    public Integer call() {

        if (enrichManifests) {
            try {
                executeManifestEnrich();
            } catch (IOException e) {
                return 1;
            }
        }

        if (executeApiCall) {
            executeApiRequest();
        }
        return 0;

    }

    public void executeApiRequest() {

        WebClient wb = WebClient.builder()
                .baseUrl("https://reqres.in/api/users")
                .build();

        UsersResponse block = wb.get().retrieve().bodyToMono(UsersResponse.class).block();
        System.out.println(block);
    }

    public void executeManifestEnrich() throws IOException {

        String input = Utils.getUserInputWithTimeout(5);
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        KubernetesDeserializer kubernetesDeserializer = new KubernetesDeserializer();

        if (Objects.nonNull(input)) {
            for (String resource : input.split("---")) {

                if (Objects.nonNull(resource) && !resource.equals("null") && !resource.equals("")) {

                    KubernetesResource kubeResource = kubernetesDeserializer.deserialize(
                            objectMapper.createParser(resource),
                            objectMapper.getDeserializationContext());

                    if (kubeResource instanceof HasMetadata hasMetadata) {
                        if (hasMetadata.getKind().equalsIgnoreCase("deployment")) {
                            hasMetadata.getMetadata()
                                    .setAnnotations(Map.of("bnhp.co.il/custom-annotation", "custom-annotation"));
                        }
                        System.out.println(objectMapper.writeValueAsString(kubeResource));
                    }
                }
            }
        }
    }
}
