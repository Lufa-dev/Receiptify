package com.thesis.receiptify.model.enums;

import lombok.Getter;

import java.time.Month;
import java.util.EnumSet;
import java.util.Set;

@Getter
public enum IngredientSeasonality {
    // Year-round ingredients
    YEAR_ROUND("Available year-round", EnumSet.allOf(Month.class)),

    // Spring ingredients (March, April, May)
    SPRING("Spring seasonal", EnumSet.of(Month.MARCH, Month.APRIL, Month.MAY)),

    // Summer ingredients (June, July, August)
    SUMMER("Summer seasonal", EnumSet.of(Month.JUNE, Month.JULY, Month.AUGUST)),

    // Autumn ingredients (September, October, November)
    AUTUMN("Autumn seasonal", EnumSet.of(Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER)),

    // Winter ingredients (December, January, February)
    WINTER("Winter seasonal", EnumSet.of(Month.DECEMBER, Month.JANUARY, Month.FEBRUARY)),

    // Spring-Summer ingredients
    SPRING_SUMMER("Spring and Summer seasonal", EnumSet.of(
            Month.MARCH, Month.APRIL, Month.MAY, Month.JUNE, Month.JULY, Month.AUGUST)),

    // Summer-Autumn ingredients
    SUMMER_AUTUMN("Summer and Autumn seasonal", EnumSet.of(
            Month.JUNE, Month.JULY, Month.AUGUST, Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER)),

    // Autumn-Winter ingredients
    AUTUMN_WINTER("Autumn and Winter seasonal", EnumSet.of(
            Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER, Month.JANUARY, Month.FEBRUARY)),

    // Winter-Spring ingredients
    WINTER_SPRING("Winter and Spring seasonal", EnumSet.of(
            Month.DECEMBER, Month.JANUARY, Month.FEBRUARY, Month.MARCH, Month.APRIL, Month.MAY)),

    SPRING_AUTUMN("Spring and Autumn seasonal", EnumSet.of(Month.MARCH, Month.APRIL, Month.MAY, Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER)),

    // Unknown seasonality (for ingredients with no seasonal data)
    UNKNOWN("Unknown seasonality", EnumSet.noneOf(Month.class));

    private final String displayName;
    private final Set<Month> months;

    IngredientSeasonality(String displayName, Set<Month> months) {
        this.displayName = displayName;
        this.months = months;
    }

    /**
     * Checks if the ingredient is in season for the specified month
     * @param month The month to check
     * @return true if the ingredient is in season during that month
     */
    public boolean isInSeason(Month month) {
        return months.contains(month);
    }

    /**
     * Determines the current seasonality status (IN_SEASON, COMING_SOON, OUT_OF_SEASON)
     * @param currentMonth The current month
     * @return The SeasonalityStatus
     */
    public SeasonalityStatus getStatus(Month currentMonth) {
        if (this == YEAR_ROUND || isInSeason(currentMonth)) {
            return SeasonalityStatus.IN_SEASON;
        }

        // Check if this ingredient will be in season next month
        Month nextMonth = currentMonth.plus(1);
        if (isInSeason(nextMonth)) {
            return SeasonalityStatus.COMING_SOON;
        }

        return SeasonalityStatus.OUT_OF_SEASON;
    }

    public enum SeasonalityStatus {
        IN_SEASON("In Season"),
        COMING_SOON("Coming Soon"),
        OUT_OF_SEASON("Out of Season");

        @Getter
        private final String displayName;

        SeasonalityStatus(String displayName) {
            this.displayName = displayName;
        }
    }
}