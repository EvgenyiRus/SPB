package model;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.Period;

public class Person {
    private final long id;
    private Gender gender;
    private LocalDate birthDate;
    private int region;
    private BigDecimal income;
    private LocalDate createdAt;

    public Person() {
        SecureRandom secureRandom = new SecureRandom();
        this.id = Math.abs(secureRandom.nextLong());
    }

    public boolean isWorkingAge() {
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (gender == Gender.MAN) {
            return age >= 18 && age <= 60;
        } else {
            return age >= 18 && age <= 55;
        }
    }

    public boolean isUnemployed() {
        return isWorkingAge() && (income == null || income.longValue() == 0);
    }

    public boolean isValidatePerson(Person person) {
        if (person.getIncome() != null && person.getIncome().longValue() < 0) {
            return false;
        }
        return person.getBirthDate().isAfter(LocalDate.now());
    }

    public Long getId() {
        return id;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public int getRegion() {
        return region;
    }

    public void setRegion(int region) {
        this.region = region;
    }

    public BigDecimal getIncome() {
        return income;
    }

    public void setIncome(BigDecimal income) {
        this.income = income;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }
}
