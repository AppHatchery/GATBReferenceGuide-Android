// Compatibility map that translates stale bookmark/note IDs to current IDs after content updates.
// When guide editors rename or renumber a chapter section or reference table, any user who had
// saved a bookmark or note with the old ID would silently lose it on the next app update.
// This object provides the redirect maps that fix those IDs before the user sees their saved list.
//
// CHART_REDIRECTS: maps old "table_*" chart IDs to their new names. Chart IDs are derived from
// the JSON filenames: lowercased, spaces replaced with underscores, prefixed with "table_".
// When chart JSON files are renumbered in the guide (e.g. table_10 → table_9), add the mapping
// here and release the app update. The entry above the map shows the most recent renaming batch.
//
// SUBCHAPTER_REDIRECTS: maps old subchapter IDs/titles. Currently empty — populate it when any
// subchapter identifier changes between app versions.
//
// redirectBookmarkId(bookmarkId): called during startup migration from SavedFragment and
// LegacyNotesMigrator. Routes to the correct map using the "table_" prefix as a discriminator.
// redirectSubChapterKey(): used by LegacyNotesMigrator to fix note target IDs the same way.
// Related: BookmarkDao.repairRedirect(), LegacyNotesMigrator.kt, SavedFragment, FASavedViewModel.
package org.apphatchery.gatbreferenceguide.utils

/**
 * Redirects for bookmarks/notes created in older app versions.
 *
 * IMPORTANT:
 * - Populate these maps with the real renames
 * - Keys should match what you stored in BookmarkEntity.bookmarkId / BookmarkEntity.subChapter.
 */
object LegacyRedirects {

    /**
     * For normal bookmarks, bookmarkId is typically a SubChapterEntity.subChapterId (string)
     * or sometimes a SubChapterEntity.subChapterTitle.
     */
    private val SUBCHAPTER_REDIRECTS: Map<String, String> = mapOf(
        // "old_subchapter_id_or_title" to "new_subchapter_id_or_title",
    )

    /**
     * For table bookmarks, bookmarkId contains "table_" and typically matches ChartEntity.id.
     */
    private val CHART_REDIRECTS: Map<String, String> = mapOf(
        // old chart.json -> new chart.json renames/renumbering

        "table_10_pediatric_dosages_rifampin_in_children_(birth_to_15_years)" to
            "table_9_pediatric_dosage_isoniazid_in_children_(birth_to_15_years)",

        "table_11_pediatric_dosages_ethambutol_in_children_(birth_to_15_years)" to
            "table_9_pediatric_dosage_isoniazid_in_children_(birth_to_15_years)",

        "table_12_pediatric_dosages_pyrazinamide_in_children_(birth_to_15_years)" to
            "table_9_pediatric_dosage_isoniazid_in_children_(birth_to_15_years)",

        "table_13_antituberculosis_antibiotics_in_adult_patients_with_renal_impairment" to
            "table_10_antituberculosis_antibiotics_in_adult_patients_with_renal_impairment",

        "table_14_antituberculosis_medications_which_may_be_used_for_patients_who_have_contraindications_to_or_intolerance" to
            "table_11_antituberculosis_medications_which_may_be_used_for_patients_who_have_contraindications_to_or_intolerance",

        "table_15_clinical_situations_for_which_standard_therapy_cannot_be_given_or_is_not_well_tolerated" to
            "table_12_clinical_situations_for_which_standard_therapy_cannot_be_given_or_is_not_well_tolerated",

        "table_16_when_to_start_hiv_therapy" to
            "table_13_when_to_start_hiv_therapy",

        "table_17_what_to_start_choice_of_tb_therapy_and_antiretroviral_therapy_(art)_when_treating_co-infected_patients" to
            "table_14_what_to_start_choice_of_tb_therapy_and_antiretroviral_therapy_(art)_when_treating_co-infected_patients",

        "table_18_dosage_adjustments_for_art_and_rifamycins_when_used_in_combination" to
            "table_15_summary_of_recommendations_for_treatment_of_active_tb_disease_in_persons_with_hiv",

        "table_19_guidelines_for_treatment_of_extrapulmonary_tuberculosis" to
            "table_16_guidelines_for_treatment_of_extrapulmonary_tuberculosis",

        "table_20_use_of_anti-tb_medications_in_special_situations_pregnancy_tuberculosis_meningitis_and_renal_failure" to
            "table_17_use_of_anti-tb_medications_in_special_situations_pregnancy_tuberculosis_meningitis_and_renal_failure",

        "table_21_grady_hospital_tb_isolation_policy" to
            "table_18_grady_hospital_tb_isolation_policy",
    )

    fun redirectBookmarkId(bookmarkId: String): String {
        return if (bookmarkId.startsWith("table_")) {
            CHART_REDIRECTS[bookmarkId] ?: bookmarkId
        } else {
            SUBCHAPTER_REDIRECTS[bookmarkId] ?: bookmarkId
        }
    }

    fun redirectSubChapterKey(subChapter: String): String = SUBCHAPTER_REDIRECTS[subChapter] ?: subChapter
}
