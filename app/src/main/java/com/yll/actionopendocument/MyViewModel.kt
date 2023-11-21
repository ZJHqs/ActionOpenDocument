package com.yll.actionopendocument

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.IOException

private const val INITIAL_PAGE_INDEX = 0

class MyViewModel : ViewModel() {
    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var currentPage: PdfRenderer.Page

    private val _currentPageNumber = MutableLiveData(INITIAL_PAGE_INDEX)
    val currentPageNumber = _currentPageNumber

    val pageCount get() = pdfRenderer.pageCount

    @Throws(IOException::class)
    fun openRenderer(context: Context?, documentUri: Uri) {
        if (context == null) return
        val fileDescriptor = context.contentResolver.openFileDescriptor(documentUri, "r") ?: return
        pdfRenderer = PdfRenderer(fileDescriptor)
        currentPage = pdfRenderer.openPage(currentPageNumber.value!!)
    }

    @Throws(IOException::class)
    fun closeRenderer() {
        currentPage.close()
        pdfRenderer.close()
    }

    fun loadPage(index: Int): Bitmap? {
        if (index < 0 || index >= pdfRenderer.pageCount) return null
        currentPage.close()
        currentPage = pdfRenderer.openPage(index)
        val bitmap = Bitmap.createBitmap(currentPage.width, currentPage.height, Bitmap.Config.ARGB_8888)
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        return bitmap
    }

    fun updateNumber(number: Int) {
        currentPageNumber.postValue(currentPageNumber.value!! + number)
    }
}