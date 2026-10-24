# Privacy Policy for Diet Tracker

**Effective Date:** October 12, 2026  
**Last Updated:** October 12, 2026  
**Application ID:** `com.aistudio.diettracker.nvkqwz`  
**Contact Email:** macrodiettracker@gmail.com  

---

## 1. Introduction

Welcome to **Diet Tracker** ("we", "our", or "the App"). We are firmly committed to safeguarding your privacy and ensuring that your dietary and personal health information remains secure, confidential, and under your control.

This Privacy Policy explains how information is collected, used, processed, and safeguarded when you use the Diet Tracker mobile application.

---

## 2. Core Privacy Principle: Local-First & Hardware Encrypted

Diet Tracker is designed from the ground up with a **local-first privacy architecture**:
- **On-Device Storage:** Your meals, macronutrients, calorie tracking, weight logs, and personal notes are stored locally on your device within a secured SQLite database.
- **Hardware-Backed AES-256-GCM Encryption:** Personal notes and sensitive log details are protected using the Android KeyStore and AES-256-GCM hardware encryption.
- **No Account Required:** You can use Diet Tracker completely without creating a third-party account or providing personal credentials.
- **No Data Selling:** We do not sell, rent, monetize, or broker your personal data, dietary habits, or health information to advertisers, data brokers, or third parties.

---

## 3. Information Collected and Processed

### A. Information You Provide
1. **Meal Logs and Nutritional Data:** Meal names, meal categories (Breakfast, Lunch, Dinner, Snack), estimated or custom calories, macronutrient values (protein, carbs, and fat in grams), and optional meal notes.
2. **Body Weight and Target Goals:** Daily calorie goals, target macronutrient ratios, target body weight, weight log entries, and unit preferences (pounds or kilograms).
3. **Display and Theme Preferences:** Theme settings (System, Calming Light, Forest Dark) and unit toggles.

### B. Device Permissions and Media
1. **Camera Permission (`android.permission.CAMERA`):**
   - **Purpose:** Used strictly when you choose to photograph a meal using the Food Macro Scanner.
   - **Handling:** Food photographs are analyzed in real time to detect nutritional components. Images are not used for facial recognition, biometric identity profiling, or marketing purposes.
2. **Photo Gallery Access:**
   - **Purpose:** Handled via the system photo picker if you select an existing food image from your device gallery.
3. **Internet Access (`android.permission.INTERNET`):**
   - **Purpose:** Used strictly to retrieve nutritional data and real-world nutrition facts via Google Gemini AI and Google Search Grounding when you scan a meal or perform an online food search.

---

## 4. Third-Party Services and AI Processing

To provide food recognition and nutritional facts, the App interacts with:

1. **Google Gemini Generative AI & Google Search Grounding:**
   - When you scan a food photo or submit a query/correction via the search feature, meal visual representations or textual food queries are transmitted securely via HTTPS to Google Gemini API services.
   - This service identifies the food item, estimates nutritional content (calories, protein, carbohydrates, fat), and retrieves verified nutritional data.
   - For more details, consult the [Google Privacy Policy](https://policies.google.com/privacy).

The App does **not** integrate third-party advertising SDKs, tracking pixels, or cross-app analytics trackers.

---

## 5. Data Sharing and User-Initiated Exports

- **No Unauthorized Sharing:** We do not share your private logs, weight history, or photos with any external entities.
- **User-Initiated Social Sharing:** When you choose to tap **"Share Achievements with Friends"**, the App generates a plain-text summary (e.g., today's total calories and protein goal status) and opens the standard Android share sheet. You have full discretion over which platform or contact you share this information with.

---

## 6. Data Retention, Modification, and Deletion

- **Editing Records:** You can edit any logged meal's name, total calories, and macronutrient values (protein, carbs, fat) directly on the Log screen at any time.
- **Deleting Records:** You can delete individual meal entries or weight records at any time. Deleted records are immediately removed from the local database.
- **Complete Deletion:** Because all data resides locally on your device, uninstalling the App or clearing the App's storage via Android Settings permanently deletes all stored meals, weight records, encryption keys, and preferences.

---

## 7. Children's Privacy

Diet Tracker does not address or knowingly collect personally identifiable information from children under the age of 13. If you are a parent or guardian and believe your child has provided personal information, please contact us at macrodiettracker@gmail.com so we can take appropriate measures.

---

## 8. Security Measures

We employ industry-standard security safeguards to protect your information:
- Local database storage protected by Android's application sandbox.
- AES-256-GCM authenticated encryption for private notes and logs.
- All network interactions with external nutritional AI services utilize encrypted Transport Layer Security (TLS/HTTPS).

---

## 9. Changes to This Privacy Policy

We may update this Privacy Policy periodically to reflect app enhancements or legal requirements. Any modifications will be reflected with an updated "Last Updated" date at the top of this policy. We encourage you to review this policy periodically.

---

## 10. Contact Us

If you have questions, comments, or requests regarding this Privacy Policy or your data privacy, please contact:

**Developer Email:** [macrodiettracker@gmail.com](mailto:macrodiettracker@gmail.com)  
**Application:** Diet Tracker (`com.aistudio.diettracker.nvkqwz`)  
