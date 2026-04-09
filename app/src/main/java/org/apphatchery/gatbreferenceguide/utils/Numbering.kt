// Static lookup arrays for converting a 1-based integer index into its roman-numeral or
// alphabetic equivalent, mirroring the numbering scheme used in the guide's HTML content.
//
// ROMAN_NUMERALS: lowercase roman numerals i–l (1–50). Index with [n-1] to get the numeral
//                 for position n. Used wherever sub-sections in the guide are numbered with
//                 lowercase roman numerals (e.g. diagnostic criteria lists).
//
// ALPHABET: lowercase a–z. Index with [n-1] to get the letter for position n. Used for
//           alphabetically-labelled sub-items in guide tables and numbered drug lists.
//
// GOTCHA: both arrays are zero-indexed but represent 1-based positions in the guide.
// Always use ROMAN_NUMERALS[position - 1] and ALPHABET[position - 1] to avoid off-by-one errors.
// Related: SubChapterEntity numbering display, guide HTML rendering logic.
package org.apphatchery.gatbreferenceguide.utils

//i=1, v=5, x=10, l=50, c=100
val ROMAN_NUMERALS = arrayOf(
    "i",
    "ii",
    "iii",
    "iv",
    "v",
    "vi",
    "vii",
    "viii",
    "ix",
    "x",
    "xi",
    "xii",
    "xiii",
    "xiv",
    "xv",
    "xvi",
    "xvii",
    "xviii",
    "xix",
    "xx",
    "xxi",
    "xxii",
    "xxiii",
    "xxiv",
    "xxv",
    "xxvi",
    "xxvii",
    "xxviii",
    "xxix",
    "xxx",
    "xxxi",
    "xxxii",
    "xxxiii",
    "xxxiv",
    "xxxv",
    "xxxvi",
    "xxxvii",
    "xxxviii",
    "xxxix",
    "xl",
    "xli",
    "xlii",
    "xliii",
    "xliv",
    "xlv",
    "xlvi",
    "xlvii",
    "xlviii",
    "xlix",
    "l"
)

val ALPHABET = arrayOf(
    "a",
    "b",
    "c",
    "d",
    "e",
    "f",
    "g",
    "h",
    "i",
    "j",
    "k",
    "l",
    "m",
    "n",
    "o",
    "p",
    "q",
    "r",
    "s",
    "t",
    "u",
    "v",
    "w",
    "x",
    "y",
    "z",
)