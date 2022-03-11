package cz.cvut.fel.poustka.daniel.flashcards_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableJpaAuditing
public class FlashcardsBackendApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(FlashcardsBackendApplication.class, args);
    }

}
