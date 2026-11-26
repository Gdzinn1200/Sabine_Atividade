package br.edu.cesumar.agendapro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Ponto de entrada da aplicacao AgendaPro. */
@SpringBootApplication
@EnableScheduling   // RF15: rotina diaria de lembretes
public class AgendaProApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgendaProApplication.class, args);
    }
}
