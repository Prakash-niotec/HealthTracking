package com.healthtrack.app.domain.engine

object StarterRules {
    val defaultRules = listOf(
        // Diabetes: sugar <= 10 g; carbohydrate <= 45 g
        HealthRule(
            id = "diabetes_sugar",
            nutrient = "sugar",
            aliases = listOf("added sugar", "sugars", "sucrose", "glucose", "fructose"),
            condition = "Diabetes",
            threshold = 10.0,
            baseUnit = "g",
            perServingBasis = "per serving",
            explanationTemplate = "For Diabetes, the safe limit is {threshold}. The entered value was {value}.",
            criteriaSource = "Project-defined criteria, not medical advice"
        ),
        HealthRule(
            id = "diabetes_carbs",
            nutrient = "carbohydrate",
            aliases = listOf("carbs", "total carbohydrates", "total carbs"),
            condition = "Diabetes",
            threshold = 45.0,
            baseUnit = "g",
            perServingBasis = "per serving",
            explanationTemplate = "For Diabetes, the safe limit is {threshold}. The entered value was {value}.",
            criteriaSource = "Project-defined criteria, not medical advice"
        ),
        
        // Hypertension: sodium <= 400 mg
        HealthRule(
            id = "hypertension_sodium",
            nutrient = "sodium",
            aliases = listOf("salt", "na"),
            condition = "Hypertension",
            threshold = 400.0,
            baseUnit = "mg",
            perServingBasis = "per serving",
            explanationTemplate = "For Hypertension, the safe limit is {threshold}. The entered value was {value}.",
            criteriaSource = "Project-defined criteria, not medical advice"
        ),

        // High cholesterol: saturated fat <= 5 g; cholesterol <= 100 mg
        HealthRule(
            id = "highcholesterol_satfat",
            nutrient = "saturated fat",
            aliases = listOf("sat fat", "saturated fats", "animal fat"),
            condition = "High cholesterol",
            threshold = 5.0,
            baseUnit = "g",
            perServingBasis = "per serving",
            explanationTemplate = "For High cholesterol, the safe limit is {threshold}. The entered value was {value}.",
            criteriaSource = "Project-defined criteria, not medical advice"
        ),
        HealthRule(
            id = "highcholesterol_cholesterol",
            nutrient = "cholesterol",
            aliases = listOf("dietary cholesterol"),
            condition = "High cholesterol",
            threshold = 100.0,
            baseUnit = "mg",
            perServingBasis = "per serving",
            explanationTemplate = "For High cholesterol, the safe limit is {threshold}. The entered value was {value}.",
            criteriaSource = "Project-defined criteria, not medical advice"
        ),

        // Heart disease: saturated fat <= 5 g; sodium <= 400 mg; trans fat <= 0 g (any amount unsafe)
        HealthRule(
            id = "heartdisease_satfat",
            nutrient = "saturated fat",
            aliases = listOf("sat fat", "saturated fats", "animal fat"),
            condition = "Heart disease",
            threshold = 5.0,
            baseUnit = "g",
            perServingBasis = "per serving",
            explanationTemplate = "For Heart disease, the safe limit is {threshold}. The entered value was {value}.",
            criteriaSource = "Project-defined criteria, not medical advice"
        ),
        HealthRule(
            id = "heartdisease_sodium",
            nutrient = "sodium",
            aliases = listOf("salt", "na"),
            condition = "Heart disease",
            threshold = 400.0,
            baseUnit = "mg",
            perServingBasis = "per serving",
            explanationTemplate = "For Heart disease, the safe limit is {threshold}. The entered value was {value}.",
            criteriaSource = "Project-defined criteria, not medical advice"
        ),
        HealthRule(
            id = "heartdisease_transfat",
            nutrient = "trans fat",
            aliases = listOf("trans fats", "hydrogenated oil", "partially hydrogenated oil"),
            condition = "Heart disease",
            threshold = 0.0,
            baseUnit = "g",
            perServingBasis = "per serving",
            explanationTemplate = "For Heart disease, the safe limit is {threshold} (any amount is unsafe). The entered value was {value}.",
            criteriaSource = "Project-defined criteria, not medical advice"
        ),

        // Kidney disease: potassium <= 200 mg; phosphorus <= 100 mg; sodium <= 400 mg
        HealthRule(
            id = "kidneydisease_potassium",
            nutrient = "potassium",
            aliases = listOf("k"),
            condition = "Kidney disease",
            threshold = 200.0,
            baseUnit = "mg",
            perServingBasis = "per serving",
            explanationTemplate = "For Kidney disease, the safe limit is {threshold}. The entered value was {value}.",
            criteriaSource = "Project-defined criteria, not medical advice"
        ),
        HealthRule(
            id = "kidneydisease_phosphorus",
            nutrient = "phosphorus",
            aliases = listOf("phosphate"),
            condition = "Kidney disease",
            threshold = 100.0,
            baseUnit = "mg",
            perServingBasis = "per serving",
            explanationTemplate = "For Kidney disease, the safe limit is {threshold}. The entered value was {value}.",
            criteriaSource = "Project-defined criteria, not medical advice"
        ),
        HealthRule(
            id = "kidneydisease_sodium",
            nutrient = "sodium",
            aliases = listOf("salt", "na"),
            condition = "Kidney disease",
            threshold = 400.0,
            baseUnit = "mg",
            perServingBasis = "per serving",
            explanationTemplate = "For Kidney disease, the safe limit is {threshold}. The entered value was {value}.",
            criteriaSource = "Project-defined criteria, not medical advice"
        ),

        // Obesity: sugar <= 10 g; total fat <= 10 g; calories <= 200 kcal
        HealthRule(
            id = "obesity_sugar",
            nutrient = "sugar",
            aliases = listOf("added sugar", "sugars", "sucrose"),
            condition = "Obesity",
            threshold = 10.0,
            baseUnit = "g",
            perServingBasis = "per serving",
            explanationTemplate = "For Obesity, the safe limit is {threshold}. The entered value was {value}.",
            criteriaSource = "Project-defined criteria, not medical advice"
        ),
        HealthRule(
            id = "obesity_totalfat",
            nutrient = "total fat",
            aliases = listOf("fat", "lipids"),
            condition = "Obesity",
            threshold = 10.0,
            baseUnit = "g",
            perServingBasis = "per serving",
            explanationTemplate = "For Obesity, the safe limit is {threshold}. The entered value was {value}.",
            criteriaSource = "Project-defined criteria, not medical advice"
        ),
        HealthRule(
            id = "obesity_calories",
            nutrient = "calories",
            aliases = listOf("energy", "kcal", "kilocalories"),
            condition = "Obesity",
            threshold = 200.0,
            baseUnit = "kcal",
            perServingBasis = "per serving",
            explanationTemplate = "For Obesity, the safe limit is {threshold}. The entered value was {value}.",
            criteriaSource = "Project-defined criteria, not medical advice"
        )
    )
}
