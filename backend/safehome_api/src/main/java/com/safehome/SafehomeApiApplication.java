package com.safehome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Classe principale de démarrage de l'application Spring Boot SafeHome API.
 * Active les tâches planifiées (@EnableScheduling) pour l'exécution des routines temporelles.
 */
@SpringBootApplication
@EnableScheduling
public class SafehomeApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SafehomeApiApplication.class, args);
	}

}
