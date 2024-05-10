package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;
import java.util.Set;

import java.time.LocalDate;

@SpringBootTest
class FilmorateApplicationTests {

	@Test
	void contextLoads() {
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		Validator validator = validatorFactory.usingContext().getValidator();
		final Film film = new Film(null, "Интересный фильм", "Описание фильма",
				LocalDate.of(1950, 1, 1), 100);

		Set<ConstraintViolation<Film>> validates = validator.validate(film);
		System.out.println(film);

	}
}
