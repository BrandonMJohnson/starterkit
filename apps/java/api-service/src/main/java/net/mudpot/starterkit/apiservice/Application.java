package net.mudpot.starterkit.apiservice;

import io.micronaut.runtime.Micronaut;

public class Application {
    public static void main(final String[] args) {
        Micronaut.build(args)
            .packages("net.mudpot.starterkit.apiservice")
            .mainClass(Application.class)
            .start();
    }
}
