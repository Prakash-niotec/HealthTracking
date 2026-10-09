# Health Rules & Ingredient Evaluation Criteria

> **IMPORTANT NOTICE**: All rule thresholds, formulas, and criteria contained herein are **project-defined criteria, not medical advice**. The project owner and medical professionals should verify numbers before production deployment.

## Condition Threshold Rules

### 1. Type 2 Diabetes
- **Sugar**: $\le 10\text{ g}$
- **Carbohydrates**: $\le 45\text{ g}$

### 2. Hypertension (High Blood Pressure)
- **Sodium**: $\le 400\text{ mg}$

### 3. High Cholesterol
- **Saturated Fat**: $\le 5\text{ g}$
- **Cholesterol**: $\le 100\text{ mg}$

### 4. Heart Disease
- **Saturated Fat**: $\le 5\text{ g}$
- **Sodium**: $\le 400\text{ mg}$
- **Trans Fat**: $\le 0\text{ g}$ (any amount is considered unsafe)

### 5. Kidney Disease
- **Potassium**: $\le 200\text{ mg}$
- **Phosphorus**: $\le 100\text{ mg}$
- **Sodium**: $\le 400\text{ mg}$

### 6. Obesity
- **Sugar**: $\le 10\text{ g}$
- **Total Fat**: $\le 10\text{ g}$
- **Calories**: $\le 200\text{ kcal}$

---

## Compound Nutrient Conversion Formulas

When a user inputs a compound ingredient instead of a raw nutrient, the engine applies standard chemical conversion factors:

1. **Table Salt ($\text{NaCl}$) to Sodium ($\text{Na}$):**
   $$\text{Sodium (mg)} = \text{Table Salt (g)} \times 393.4\text{ mg/g}$$
   *(Formula: $1\text{ g NaCl} \approx 0.3934\text{ g Na} = 393.4\text{ mg Na}$)*

2. **Baking Soda ($\text{NaHCO}_3$) to Sodium ($\text{Na}$):**
   $$\text{Sodium (mg)} = \text{Baking Soda (g)} \times 273.7\text{ mg/g}$$
   *(Formula: $1\text{ g NaHCO}_3 \approx 0.2737\text{ g Na} = 273.7\text{ mg Na}$)*

3. **Potassium Chloride ($\text{KCl}$) to Potassium ($\text{K}$):**
   $$\text{Potassium (mg)} = \text{Potassium Chloride (g)} \times 524.5\text{ mg/g}$$
   *(Formula: $1\text{ g KCl} \approx 0.5245\text{ g K} = 524.5\text{ mg K}$)*
