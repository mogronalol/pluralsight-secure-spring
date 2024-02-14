package pluralsight.m2.domain;

import java.time.LocalDateTime;

public record Transaction(long id, LocalDateTime date, String description, double amount) {}

