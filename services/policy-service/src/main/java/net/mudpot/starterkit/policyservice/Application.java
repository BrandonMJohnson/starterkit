package net.mudpot.starterkit.policyservice;

import io.micronaut.runtime.Micronaut;

public class Application {
    public static void main(final String[] args) {
        Micronaut.build(args)
            .packages("net.mudpot.starterkit.policyservice")
            .mainClass(Application.class)
            .start();
    }
}
