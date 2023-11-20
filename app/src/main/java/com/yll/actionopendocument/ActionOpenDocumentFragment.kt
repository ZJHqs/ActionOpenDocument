package com.yll.actionopendocument

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import java.io.IOException

private const val INITIAL_PAGE_INDEX = 0
private const val TAG = "ActionOpenDocumentFragment"

private const val CURRENT_PAGE_INDEX_KEY =
    "com.yll.actionopendocument.state.CURRENT_PAGE_INDEX_KEY"

class ActionOpenDocumentFragment : Fragment() {

    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var currentPage: PdfRenderer.Page
    private  var currentPageNumber: Int = INITIAL_PAGE_INDEX

    private lateinit var pdfPageView: ImageView
    private lateinit var previousButton: Button
    private lateinit var nextButton: Button

    val pageCount get() = pdfRenderer.pageCount

    companion object {
        private const val DOCUMENT_URI_ARGUMENT =
            "com.yll.androidopendocument.args.DOCUMENT_URI_ARGUMENT"

        fun newInstance(documentUri: Uri): ActionOpenDocumentFragment {
            return ActionOpenDocumentFragment().apply {
                arguments = Bundle().apply {
                    putString(DOCUMENT_URI_ARGUMENT, documentUri.toString())
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_action_open_document, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pdfPageView = view.findViewById(R.id.image)
        previousButton = view.findViewById<Button>(R.id.previous).apply {
            setOnClickListener {
                showPage(currentPage.index - 1)
            }
        }
        nextButton = view.findViewById<Button>(R.id.next).apply {
            setOnClickListener {
                showPage(currentPage.index + 1)
            }
        }

        currentPageNumber = savedInstanceState?.getInt(CURRENT_PAGE_INDEX_KEY, INITIAL_PAGE_INDEX)
            ?: INITIAL_PAGE_INDEX
    }

    override fun onStart() {
        super.onStart()

        val documentUri = arguments?.getString(DOCUMENT_URI_ARGUMENT)?.toUri() ?: return
        try {
            openRenderer(activity, documentUri)
            showPage(currentPageNumber)
        } catch (ioException: IOException) {
            Log.e(TAG, "Exception opening document", ioException)
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            closeRenderer()
        } catch (ioException: IOException) {
            Log.e(TAG, "Exception closing document", ioException)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(CURRENT_PAGE_INDEX_KEY, currentPage.index)
        super.onSaveInstanceState(outState)
    }

    @Throws(IOException::class)
    private fun openRenderer(context: Context?, documentUri: Uri) {
        if (context == null) { return }

        val fileDescriptor = context.contentResolver.openFileDescriptor(documentUri, "r") ?: return

        pdfRenderer = PdfRenderer(fileDescriptor)
        currentPage = pdfRenderer.openPage(currentPageNumber)
    }

    @Throws(IOException::class)
    private fun closeRenderer() {
        currentPage.close()
        pdfRenderer.close()
    }


    private fun showPage(index: Int) {
        if (index < 0 || index >= pdfRenderer.pageCount) {
            return
        }

        currentPage.close()
        currentPage = pdfRenderer.openPage(index)

        val bitmap = createBitmap(currentPage.width, currentPage.height, Bitmap.Config.ARGB_8888)

        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        pdfPageView.setImageBitmap(bitmap)

        val pageCount = pdfRenderer.pageCount
        previousButton.isEnabled = (0 != index)
        nextButton.isEnabled = (index + 1 < pageCount)
        activity?.title = getString(R.string.app_name_with_index, index + 1, pageCount)
    }
}