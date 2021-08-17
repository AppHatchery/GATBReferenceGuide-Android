package org.apphatchery.gatbreferenceguide.utils


//using firebase
//CSS_JS_FILES.forEach { asset ->
//    val file = ASSETS_DIR + asset
//    val storage = storageRef.reference.child(file)
//    val byte = storage.getBytes(1024 + 10L)
//    byte.addOnSuccessListener {
//        (ByteArrayInputStream(it) as InputStream).use {
//            requireContext().toInternalStorage(it, file)
//        }
//    }
//}


//using download client
//SUBCHAPTER.forEach {
//    val file = PAGES_DIR + it.url + EXTENSION
//    viewModel.downloadFile(file).also { responseBody ->
//        if (responseBody.isSuccessful) {
//            responseBody.body()?.let { body ->
//                requireContext().toInternalStorage(body.byteStream(), file)
//            }
//        } else Log.e("TAG", "onViewCreated: " + responseBody.errorBody())
//    }
//}


//fun <T> Context.readJsonFromAssetToString(file: String): List<T>? {
//    return try {
//        val json = assets.open(JSON_DIR + file).bufferedReader().use { it.readText() }
//        val ofType = object : TypeToken<List<T>>() {}.type
//        (Gson().fromJson(json, ofType) as List<T>)
//    } catch (e: Exception) {
//        null
//    }
//}


//private fun toChapterObj(obj: String): ChapterEntity {
//    var text = obj
//    text = text.replace("chapterId", "")
//    text = text.replace("chapterTitle", "")
//    text = text.replace("{", "")
//    text = text.replace("}", "")
//
//    val chapterId = text.subSequence(
//        text.indexOf("=", 0) + 1,
//        text.indexOf(",", 0)
//    ).toString().toInt()
//
//    val chapterTitle = text.subSequence(
//        text.lastIndexOf("=") + 1,
//        text.length
//    ).toString()
//    return ChapterEntity(chapterId, chapterTitle)
//}
