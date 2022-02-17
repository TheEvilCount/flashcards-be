package cz.cvut.fel.poustka.daniel.flashcards_backend.environment;

import cz.cvut.fel.poustka.daniel.flashcards_backend.model.Role;
import cz.cvut.fel.poustka.daniel.flashcards_backend.model.User;
import net.bytebuddy.utility.RandomString;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Random;

public class Generator
{
    private static final Random RAND = new Random();

    public static int randomInt()
    {
        return RAND.nextInt();
    }

    public static boolean randomBoolean()
    {
        return RAND.nextBoolean();
    }

    public static User generateUser(Role role)
    {
        final User user = new User();
        user.setEmail("pepa.houba" + randomInt() + "@appraven.net");
        user.setPassword("MakeAmericaGreatAgain" + randomInt());
        user.setUsername("Agnakaraara" + randomInt());
        user.setRegistrationDate(Date.valueOf(LocalDate.now()));
        user.setRole(role);
        return user;
    }

    public static String randomString(Integer length)
    {
        return RandomString.make(length);
    }
/*
    public static Country generateCountry() {
        final Country country = new Country();
        country.setId(randomInt());
        country.setTitle("Česká republika" + randomInt());
        return country;
    }

    public static Medium generateMedium() {
        Medium medium = new Medium();
        medium.setMediumType(MediumType.VIDEO);
        medium.setFilename("/adwd/awd.mp4" + randomInt());
        medium.setResolution(randomInt() + "x" + randomInt());
        medium.setSize(randomInt());
        return medium;
    }

    public static Video generateVideo(VideoType type) {

        Country country = generateCountry();

        final Price price = new Price();
        price.setId(randomInt());
        price.setAmount(randomInt());
        price.setDateFrom(Date.valueOf(LocalDate.now()));

        final Video video = new Video();
        video.setId(randomInt());
        video.setTitle("James Bond: Agent " + randomInt());
        video.setDescription("Very cool movie" + randomInt());
        video.setVideoType(type);
        video.setYear(randomInt());
        video.setCountry(country);
        return video;
    }*/
}
