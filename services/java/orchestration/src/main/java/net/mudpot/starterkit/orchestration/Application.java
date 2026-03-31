package net.mudpot.starterkit.orchestration;

import io.micronaut.runtime.Micronaut;

public class Application {
    public static void main(final String[] args) {
        Micronaut.build(args)
            .packages("net.mudpot.starterkit.orchestration")
            .mainClass(Application.class)
            .start();
    }
}
