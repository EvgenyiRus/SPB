package model;

import java.math.BigDecimal;

public class Statistics {
    private int region;
    private long totalPopulation; // общее количество населения
    private long workingPopulation; // количество трудоспособного населения
    private double workingPercentage; // % трудоспособного населения
    private BigDecimal averageIncome;
    private BigDecimal maxIncome;
    private long unemployedPopulation; // количество не трудоспособного населения
    private double unemployedPercentage;

    private Statistics(Builder builder) {
        this.region = builder.region;
        this.totalPopulation = builder.totalPopulation;
        this.workingPopulation = builder.workingPopulation;
        this.workingPercentage = builder.workingPercentage;
        this.averageIncome = builder.averageIncome;
        this.maxIncome = builder.maxIncome;
        this.unemployedPopulation = builder.unemployedCount;
        this.unemployedPercentage = builder.unemployedPercentage;
    }

    public int getRegion() {
        return region;
    }

    public void setRegion(int region) {
        this.region = region;
    }

    public long getTotalPopulation() {
        return totalPopulation;
    }

    public void setTotalPopulation(long totalPopulation) {
        this.totalPopulation = totalPopulation;
    }

    public long getWorkingPopulation() {
        return workingPopulation;
    }

    public void setWorkingPopulation(long workingPopulation) {
        this.workingPopulation = workingPopulation;
    }

    public double getWorkingPercentage() {
        return workingPercentage;
    }

    public void setWorkingPercentage(double workingPercentage) {
        this.workingPercentage = workingPercentage;
    }

    public BigDecimal getAverageIncome() {
        return averageIncome;
    }

    public void setAverageIncome(BigDecimal averageIncome) {
        this.averageIncome = averageIncome;
    }

    public BigDecimal getMaxIncome() {
        return maxIncome;
    }

    public void setMaxIncome(BigDecimal maxIncome) {
        this.maxIncome = maxIncome;
    }

    public long getUnemployedPopulation() {
        return unemployedPopulation;
    }

    public void setUnemployedPopulation(long unemployedPopulation) {
        this.unemployedPopulation = unemployedPopulation;
    }

    public double getUnemployedPercentage() {
        return unemployedPercentage;
    }

    public void setUnemployedPercentage(double unemployedPercentage) {
        this.unemployedPercentage = unemployedPercentage;
    }

    public static class Builder {
        private int region;
        private long totalPopulation;
        private long workingPopulation;
        private double workingPercentage;
        private BigDecimal averageIncome;
        private BigDecimal maxIncome;
        private long unemployedCount;
        private double unemployedPercentage;

        public Builder region(Integer region) {
            this.region = region;
            return this;
        }

        public Builder totalPopulation(Long totalPopulation) {
            this.totalPopulation = totalPopulation;
            return this;
        }

        public Builder workingPopulation(Long workingAgePopulation) {
            this.workingPopulation = workingAgePopulation;
            return this;
        }

        public Builder workingPercentage(Double workingAgePercentage) {
            this.workingPercentage = workingAgePercentage;
            return this;
        }

        public Builder averageIncome(BigDecimal averageIncome) {
            this.averageIncome = averageIncome;
            return this;
        }

        public Builder maxIncome(BigDecimal maxIncome) {
            this.maxIncome = maxIncome;
            return this;
        }

        public Builder unemployedCount(Long unemployedCount) {
            this.unemployedCount = unemployedCount;
            return this;
        }

        public Builder unemployedPercentage(Double unemployedPercentage) {
            this.unemployedPercentage = unemployedPercentage;
            return this;
        }

        public Statistics build() {
            return new Statistics(this);
        }
    }

    // Static method to get builder instance
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "Statistics{" +
                "region=" + region +
                ", totalPopulation=" + totalPopulation +
                ", workingPopulation=" + workingPopulation +
                ", workingPercentage=" + workingPercentage +
                ", averageIncome=" + averageIncome +
                ", maxIncome=" + maxIncome +
                ", unemployedCount=" + unemployedPopulation +
                ", unemployedPercentage=" + unemployedPercentage +
                '}';
    }
}
