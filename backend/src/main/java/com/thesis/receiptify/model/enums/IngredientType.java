package com.thesis.receiptify.model.enums;

import lombok.Getter;

@Getter
public enum IngredientType {
    // VEGETABLES with seasonality
    ARTICHOKE("Artichoke", "Vegetables", IngredientSeasonality.SPRING_SUMMER),
    ARUGULA("Arugula", "Vegetables", IngredientSeasonality.SPRING_AUTUMN),
    ASPARAGUS("Asparagus", "Vegetables", IngredientSeasonality.SPRING),
    AVOCADO("Avocado", "Vegetables", IngredientSeasonality.YEAR_ROUND),
    BAMBOO_SHOOTS("Bamboo Shoots", "Vegetables", IngredientSeasonality.SPRING),
    BEAN_SPROUTS("Bean Sprouts", "Vegetables", IngredientSeasonality.YEAR_ROUND),
    BEETS("Beets", "Vegetables", IngredientSeasonality.AUTUMN_WINTER),
    BELL_PEPPER("Bell Pepper", "Vegetables", IngredientSeasonality.SUMMER_AUTUMN),
    BOK_CHOY("Bok Choy", "Vegetables", IngredientSeasonality.WINTER_SPRING),
    BROCCOLI("Broccoli", "Vegetables", IngredientSeasonality.AUTUMN_WINTER),
    BRUSSELS_SPROUTS("Brussels Sprouts", "Vegetables", IngredientSeasonality.AUTUMN_WINTER),
    CABBAGE("Cabbage", "Vegetables", IngredientSeasonality.AUTUMN_WINTER),
    CARROTS("Carrots", "Vegetables", IngredientSeasonality.YEAR_ROUND),
    CAULIFLOWER("Cauliflower", "Vegetables", IngredientSeasonality.AUTUMN_WINTER),
    CELERY("Celery", "Vegetables", IngredientSeasonality.YEAR_ROUND),
    CELERY_ROOT("Celery Root", "Vegetables", IngredientSeasonality.AUTUMN_WINTER),
    CHARD("Chard", "Vegetables", IngredientSeasonality.SPRING_AUTUMN),
    CHERRY_TOMATOES("Cherry Tomatoes", "Vegetables", IngredientSeasonality.SUMMER),
    CHIVES("Chives", "Vegetables", IngredientSeasonality.SPRING_SUMMER),
    COLLARD_GREENS("Collard Greens", "Vegetables", IngredientSeasonality.WINTER_SPRING),
    CORN("Corn", "Vegetables", IngredientSeasonality.SUMMER_AUTUMN),
    CUCUMBER("Cucumber", "Vegetables", IngredientSeasonality.SUMMER),
    DAIKON_RADISH("Daikon Radish", "Vegetables", IngredientSeasonality.WINTER),
    EGGPLANT("Eggplant", "Vegetables", IngredientSeasonality.SUMMER_AUTUMN),
    ENDIVE("Endive", "Vegetables", IngredientSeasonality.AUTUMN_WINTER),
    FENNEL("Fennel", "Vegetables", IngredientSeasonality.AUTUMN_WINTER),
    GARLIC("Garlic", "Vegetables", IngredientSeasonality.YEAR_ROUND),
    GINGER("Ginger", "Vegetables", IngredientSeasonality.YEAR_ROUND),
    GREEN_BEANS("Green Beans", "Vegetables", IngredientSeasonality.SUMMER),
    GREEN_ONIONS("Green Onions", "Vegetables", IngredientSeasonality.SPRING_SUMMER),
    HORSERADISH("Horseradish", "Vegetables", IngredientSeasonality.AUTUMN_WINTER),
    JICAMA("Jicama", "Vegetables", IngredientSeasonality.YEAR_ROUND),
    KALE("Kale", "Vegetables", IngredientSeasonality.AUTUMN_WINTER),
    KOHLRABI("Kohlrabi", "Vegetables", IngredientSeasonality.SPRING_AUTUMN),
    LEEKS("Leeks", "Vegetables", IngredientSeasonality.AUTUMN_WINTER),
    LETTUCE("Lettuce", "Vegetables", IngredientSeasonality.SPRING_AUTUMN),
    MUSHROOMS("Mushrooms", "Vegetables", IngredientSeasonality.YEAR_ROUND),
    MUSTARD_GREENS("Mustard Greens", "Vegetables", IngredientSeasonality.WINTER_SPRING),
    NAPA_CABBAGE("Napa Cabbage", "Vegetables", IngredientSeasonality.AUTUMN_WINTER),
    OKRA("Okra", "Vegetables", IngredientSeasonality.SUMMER),
    ONIONS("Onions", "Vegetables", IngredientSeasonality.YEAR_ROUND),
    PARSNIPS("Parsnips", "Vegetables", IngredientSeasonality.AUTUMN_WINTER),
    PEAS("Peas", "Vegetables", IngredientSeasonality.SPRING_SUMMER),
    POBLANO_PEPPER("Poblano Pepper", "Vegetables", IngredientSeasonality.SUMMER_AUTUMN),
    POTATOES("Potatoes", "Vegetables", IngredientSeasonality.YEAR_ROUND),
    PUMPKIN("Pumpkin", "Vegetables", IngredientSeasonality.AUTUMN),
    RADICCHIO("Radicchio", "Vegetables", IngredientSeasonality.WINTER),
    RADISHES("Radishes", "Vegetables", IngredientSeasonality.SPRING_AUTUMN),
    RED_ONION("Red Onion", "Vegetables", IngredientSeasonality.YEAR_ROUND),
    RHUBARB("Rhubarb", "Vegetables", IngredientSeasonality.SPRING),
    RUTABAGA("Rutabaga", "Vegetables", IngredientSeasonality.AUTUMN_WINTER),
    SCALLIONS("Scallions", "Vegetables", IngredientSeasonality.SPRING_SUMMER),
    SHALLOTS("Shallots", "Vegetables", IngredientSeasonality.YEAR_ROUND),
    SNAP_PEAS("Snap Peas", "Vegetables", IngredientSeasonality.SPRING_SUMMER),
    SNOW_PEAS("Snow Peas", "Vegetables", IngredientSeasonality.SPRING_SUMMER),
    SPINACH("Spinach", "Vegetables", IngredientSeasonality.SPRING_AUTUMN),
    SUMMER_SQUASH("Summer Squash", "Vegetables", IngredientSeasonality.SUMMER),
    SWEET_POTATOES("Sweet Potatoes", "Vegetables", IngredientSeasonality.AUTUMN_WINTER),
    SWISS_CHARD("Swiss Chard", "Vegetables", IngredientSeasonality.SPRING_AUTUMN),
    TOMATILLOS("Tomatillos", "Vegetables", IngredientSeasonality.SUMMER_AUTUMN),
    TOMATOES("Tomatoes", "Vegetables", IngredientSeasonality.SUMMER),
    TURNIPS("Turnips", "Vegetables", IngredientSeasonality.AUTUMN_WINTER),
    WATER_CHESTNUTS("Water Chestnuts", "Vegetables", IngredientSeasonality.AUTUMN_WINTER),
    WATERCRESS("Watercress", "Vegetables", IngredientSeasonality.SPRING_AUTUMN),
    WINTER_SQUASH("Winter Squash", "Vegetables", IngredientSeasonality.AUTUMN_WINTER),
    ZUCCHINI("Zucchini", "Vegetables", IngredientSeasonality.SUMMER),

    // FRUITS with seasonality
    ACAI_BERRIES("Acai Berries", "Fruits", IngredientSeasonality.YEAR_ROUND),
    APPLES("Apples", "Fruits", IngredientSeasonality.AUTUMN_WINTER),
    APRICOTS("Apricots", "Fruits", IngredientSeasonality.SUMMER),
    BANANAS("Bananas", "Fruits", IngredientSeasonality.YEAR_ROUND),
    BLACKBERRIES("Blackberries", "Fruits", IngredientSeasonality.SUMMER),
    BLUEBERRIES("Blueberries", "Fruits", IngredientSeasonality.SUMMER),
    CANTALOUPE("Cantaloupe", "Fruits", IngredientSeasonality.SUMMER),
    CHERRIES("Cherries", "Fruits", IngredientSeasonality.SUMMER),
    CLEMENTINES("Clementines", "Fruits", IngredientSeasonality.WINTER),
    COCONUT("Coconut", "Fruits", IngredientSeasonality.YEAR_ROUND),
    CRANBERRIES("Cranberries", "Fruits", IngredientSeasonality.AUTUMN),
    DATES("Dates", "Fruits", IngredientSeasonality.AUTUMN_WINTER),
    DRAGONFRUIT("Dragonfruit", "Fruits", IngredientSeasonality.SUMMER),
    FIGS("Figs", "Fruits", IngredientSeasonality.SUMMER_AUTUMN),
    GOJI_BERRIES("Goji Berries", "Fruits", IngredientSeasonality.YEAR_ROUND),
    GRAPEFRUIT("Grapefruit", "Fruits", IngredientSeasonality.WINTER),
    GRAPES("Grapes", "Fruits", IngredientSeasonality.AUTUMN),
    GUAVA("Guava", "Fruits", IngredientSeasonality.YEAR_ROUND),
    HONEYDEW_MELON("Honeydew Melon", "Fruits", IngredientSeasonality.SUMMER),
    KIWI("Kiwi", "Fruits", IngredientSeasonality.WINTER_SPRING),
    KUMQUATS("Kumquats", "Fruits", IngredientSeasonality.WINTER),
    LEMONS("Lemons", "Fruits", IngredientSeasonality.YEAR_ROUND),
    LIMES("Limes", "Fruits", IngredientSeasonality.YEAR_ROUND),
    LYCHEE("Lychee", "Fruits", IngredientSeasonality.SUMMER),
    MANGOES("Mangoes", "Fruits", IngredientSeasonality.SUMMER),
    MANDARINS("Mandarins", "Fruits", IngredientSeasonality.WINTER),
    NECTARINES("Nectarines", "Fruits", IngredientSeasonality.SUMMER),
    ORANGES("Oranges", "Fruits", IngredientSeasonality.WINTER),
    PAPAYAS("Papayas", "Fruits", IngredientSeasonality.YEAR_ROUND),
    PASSION_FRUIT("Passion Fruit", "Fruits", IngredientSeasonality.SUMMER_AUTUMN),
    PEACHES("Peaches", "Fruits", IngredientSeasonality.SUMMER),
    PEARS("Pears", "Fruits", IngredientSeasonality.AUTUMN_WINTER),
    PERSIMMONS("Persimmons", "Fruits", IngredientSeasonality.AUTUMN),
    PINEAPPLE("Pineapple", "Fruits", IngredientSeasonality.YEAR_ROUND),
    PLUMS("Plums", "Fruits", IngredientSeasonality.SUMMER_AUTUMN),
    POMEGRANATE("Pomegranate", "Fruits", IngredientSeasonality.AUTUMN_WINTER),
    RASPBERRIES("Raspberries", "Fruits", IngredientSeasonality.SUMMER),
    STARFRUIT("Starfruit", "Fruits", IngredientSeasonality.YEAR_ROUND),
    STRAWBERRIES("Strawberries", "Fruits", IngredientSeasonality.SPRING_SUMMER),
    TANGERINES("Tangerines", "Fruits", IngredientSeasonality.WINTER),
    WATERMELON("Watermelon", "Fruits", IngredientSeasonality.SUMMER),

    // For other categories, we'll default to YEAR_ROUND or UNKNOWN

    // PROTEINS
    BACON("Bacon", "Proteins", IngredientSeasonality.YEAR_ROUND),
    BEEF_BRISKET("Beef Brisket", "Proteins", IngredientSeasonality.YEAR_ROUND),
    BEEF_CHUCK("Beef Chuck", "Proteins", IngredientSeasonality.YEAR_ROUND),
    BEEF_GROUND("Ground Beef", "Proteins", IngredientSeasonality.YEAR_ROUND),
    BEEF_RIBEYE("Beef Ribeye", "Proteins", IngredientSeasonality.YEAR_ROUND),
    BEEF_SIRLOIN("Beef Sirloin", "Proteins", IngredientSeasonality.YEAR_ROUND),
    BEEF_TENDERLOIN("Beef Tenderloin", "Proteins", IngredientSeasonality.YEAR_ROUND),
    BLACK_BEANS("Black Beans", "Proteins", IngredientSeasonality.YEAR_ROUND),
    CANNELLINI_BEANS("Cannellini Beans", "Proteins", IngredientSeasonality.YEAR_ROUND),
    CHICKEN_BREAST("Chicken Breast", "Proteins", IngredientSeasonality.YEAR_ROUND),
    CHICKEN_DRUMSTICKS("Chicken Drumsticks", "Proteins", IngredientSeasonality.YEAR_ROUND),
    CHICKEN_GROUND("Ground Chicken", "Proteins", IngredientSeasonality.YEAR_ROUND),
    CHICKEN_THIGHS("Chicken Thighs", "Proteins", IngredientSeasonality.YEAR_ROUND),
    CHICKEN_WHOLE("Whole Chicken", "Proteins", IngredientSeasonality.YEAR_ROUND),
    CHICKPEAS("Chickpeas", "Proteins", IngredientSeasonality.YEAR_ROUND),
    CHORIZO("Chorizo", "Proteins", IngredientSeasonality.YEAR_ROUND),
    COD("Cod", "Proteins", IngredientSeasonality.WINTER_SPRING),
    CRAB("Crab", "Proteins", IngredientSeasonality.SUMMER_AUTUMN),
    DUCK("Duck", "Proteins", IngredientSeasonality.AUTUMN_WINTER),
    EDAMAME("Edamame", "Proteins", IngredientSeasonality.SUMMER),
    FALAFEL("Falafel", "Proteins", IngredientSeasonality.YEAR_ROUND),
    GROUND_TURKEY("Ground Turkey", "Proteins", IngredientSeasonality.YEAR_ROUND),
    HAM("Ham", "Proteins", IngredientSeasonality.YEAR_ROUND),
    KIDNEY_BEANS("Kidney Beans", "Proteins", IngredientSeasonality.YEAR_ROUND),
    LAMB_CHOPS("Lamb Chops", "Proteins", IngredientSeasonality.SPRING),
    LAMB_GROUND("Ground Lamb", "Proteins", IngredientSeasonality.SPRING),
    LENTILS("Lentils", "Proteins", IngredientSeasonality.YEAR_ROUND),
    LIVER("Liver", "Proteins", IngredientSeasonality.YEAR_ROUND),
    LOBSTER("Lobster", "Proteins", IngredientSeasonality.YEAR_ROUND),
    MUSSELS("Mussels", "Proteins", IngredientSeasonality.AUTUMN_WINTER),
    NAVY_BEANS("Navy Beans", "Proteins", IngredientSeasonality.YEAR_ROUND),
    PINTO_BEANS("Pinto Beans", "Proteins", IngredientSeasonality.YEAR_ROUND),
    PORK_BELLY("Pork Belly", "Proteins", IngredientSeasonality.YEAR_ROUND),
    PORK_CHOPS("Pork Chops", "Proteins", IngredientSeasonality.YEAR_ROUND),
    PORK_GROUND("Ground Pork", "Proteins", IngredientSeasonality.YEAR_ROUND),
    PORK_LOIN("Pork Loin", "Proteins", IngredientSeasonality.YEAR_ROUND),
    PORK_SHOULDER("Pork Shoulder", "Proteins", IngredientSeasonality.YEAR_ROUND),
    PROSCIUTTO("Prosciutto", "Proteins", IngredientSeasonality.YEAR_ROUND),
    SALMON("Salmon", "Proteins", IngredientSeasonality.SUMMER),
    SALAMI("Salami", "Proteins", IngredientSeasonality.YEAR_ROUND),
    SARDINES("Sardines", "Proteins", IngredientSeasonality.SUMMER),
    SAUSAGE("Sausage", "Proteins", IngredientSeasonality.YEAR_ROUND),
    SCALLOPS("Scallops", "Proteins", IngredientSeasonality.AUTUMN_WINTER),
    SEITAN("Seitan", "Proteins", IngredientSeasonality.YEAR_ROUND),
    SHRIMP("Shrimp", "Proteins", IngredientSeasonality.YEAR_ROUND),
    TEMPEH("Tempeh", "Proteins", IngredientSeasonality.YEAR_ROUND),
    TILAPIA("Tilapia", "Proteins", IngredientSeasonality.YEAR_ROUND),
    TOFU("Tofu", "Proteins", IngredientSeasonality.YEAR_ROUND),
    TROUT("Trout", "Proteins", IngredientSeasonality.SPRING_SUMMER),
    TUNA("Tuna", "Proteins", IngredientSeasonality.SUMMER),
    TURKEY_BREAST("Turkey Breast", "Proteins", IngredientSeasonality.AUTUMN_WINTER),
    VEAL("Veal", "Proteins", IngredientSeasonality.YEAR_ROUND),

    // For the remaining categories (abbreviated for brevity), we'll use YEAR_ROUND

    // GRAINS & STARCHES
    AMARANTH("Amaranth", "Grains & Starches", IngredientSeasonality.YEAR_ROUND),
    MILLET("Millet", "Grains & Starches", IngredientSeasonality.YEAR_ROUND),
    // Add other grains here...

    // DAIRY & EGGS
    AMERICAN_CHEESE("American Cheese", "Dairy & Eggs", IngredientSeasonality.YEAR_ROUND),
    // Add other dairy products here...

    // HERBS & SPICES
    ALLSPICE("Allspice", "Herbs & Spices", IngredientSeasonality.YEAR_ROUND),
    // Add other herbs and spices here...

    // OILS, VINEGARS & CONDIMENTS
    AIOLI("Aioli", "Oils, Vinegars & Condiments", IngredientSeasonality.YEAR_ROUND),
    // Add other oils and condiments here...

    // NUTS, SEEDS & DRIED FRUITS
    ALMONDS("Almonds", "Nuts, Seeds & Dried Fruits", IngredientSeasonality.YEAR_ROUND),
    // Add other nuts and seeds here...

    // SWEETENERS & BAKING
    AGAVE_NECTAR("Agave Nectar", "Sweeteners & Baking", IngredientSeasonality.YEAR_ROUND),
    // Add other sweeteners here...

    // BEVERAGES & ALCOHOLIC INGREDIENTS
    ALMOND_MILK("Almond Milk", "Beverages & Alcoholic Ingredients", IngredientSeasonality.YEAR_ROUND),
    // Add other beverages here...

    // CANNED & JARRED GOODS
    ANCHOVIES("Anchovies", "Canned & Jarred Goods", IngredientSeasonality.YEAR_ROUND),
    // Add other canned goods here...

    // FROZEN FOODS
    ACAI_PUREE_FROZEN("Frozen Acai Puree", "Frozen Foods", IngredientSeasonality.YEAR_ROUND),
    // Add other frozen foods here...

    // INTERNATIONAL INGREDIENTS
    ADOBO_SEASONING("Adobo Seasoning", "International Ingredients", IngredientSeasonality.YEAR_ROUND),
    // Add other international ingredients here...

    // MISCELLANEOUS
    CHICKEN_BROTH("Chicken Broth", "Miscellaneous", IngredientSeasonality.YEAR_ROUND),
    CHOCOLATE_SYRUP("Chocolate Syrup", "Miscellaneous", IngredientSeasonality.YEAR_ROUND),
    COOKING_SPRAY("Cooking Spray", "Miscellaneous", IngredientSeasonality.YEAR_ROUND),
    GELATIN("Gelatin", "Miscellaneous", IngredientSeasonality.YEAR_ROUND),
    HORSERADISH_PREPARED("Prepared Horseradish", "Miscellaneous", IngredientSeasonality.YEAR_ROUND),
    ICE("Ice", "Miscellaneous", IngredientSeasonality.YEAR_ROUND),
    NUTRITIONAL_YEAST("Nutritional Yeast", "Miscellaneous", IngredientSeasonality.YEAR_ROUND),
    OLIVES_STUFFED("Stuffed Olives", "Miscellaneous", IngredientSeasonality.YEAR_ROUND),
    PECTIN("Pectin", "Miscellaneous", IngredientSeasonality.YEAR_ROUND),
    PROTEIN_POWDER("Protein Powder", "Miscellaneous", IngredientSeasonality.YEAR_ROUND),
    VEGETABLE_BROTH("Vegetable Broth", "Miscellaneous", IngredientSeasonality.YEAR_ROUND),
    WATER("Water", "Miscellaneous", IngredientSeasonality.YEAR_ROUND);

    private final String displayName;
    private final String category;
    private final IngredientSeasonality seasonality;

    IngredientType(String displayName, String category, IngredientSeasonality seasonality) {
        this.displayName = displayName;
        this.category = category;
        this.seasonality = seasonality;
    }

    public static IngredientType fromDisplayName(String displayName) {
        for (IngredientType type : values()) {
            if (type.getDisplayName().equalsIgnoreCase(displayName)) {
                return type;
            }
        }
        return null;
    }
}