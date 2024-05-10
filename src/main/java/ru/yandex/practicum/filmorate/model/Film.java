package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.Setter;

import javax.xml.datatype.Duration;
import java.time.LocalDate;

/**
 * Film.
 */
@Getter
@Setter
public class Film {
    Integer id;
    String name;
    String description;
    LocalDate releaseDate;
    Duration duration;
}
