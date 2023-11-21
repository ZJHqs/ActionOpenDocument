package com.yll.actionopendocument

import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import java.io.IOException

private const val TAG = "MyFragment"

class MyFragment : Fragment() {

    private lateinit var pdfPageView: ImageView
    private lateinit var previousButton: Button
    private lateinit var nextButton: Button

    companion object {
        private const val DOCUMENT_URI_ARGUMENT =
            "com.yll.androidopendocument.args.DOCUMENT_URI_ARGUMENT"

        fun newInstance(documentUri: Uri): MyFragment {
            return MyFragment().apply {
                arguments = Bundle().apply {
                    putString(DOCUMENT_URI_ARGUMENT, documentUri.toString())
                }
            }
        }
    }

    private lateinit var viewModel: MyViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        viewModel = ViewModelProvider(this)[MyViewModel::class.java]



        return inflater.inflate(R.layout.fragment_my, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pdfPageView = view.findViewById(R.id.image)
        previousButton = view.findViewById<Button>(R.id.previous).apply {
            setOnClickListener {
                viewModel.updateNumber(-1)
            }
        }
        nextButton = view.findViewById<Button>(R.id.next).apply {
            setOnClickListener {
                viewModel.updateNumber(1)
            }
        }

        viewModel.currentPageNumber.observe(viewLifecycleOwner) { currentPageNumber ->
            pdfPageView.setImageBitmap(viewModel.loadPage(currentPageNumber))
            previousButton.isEnabled = (currentPageNumber != 0)
            nextButton.isEnabled = (currentPageNumber + 1 < viewModel.pageCount)
            activity?.title = getString(R.string.app_name_with_index, currentPageNumber + 1, viewModel.pageCount)
        }

    }

    override fun onStart() {
        super.onStart()

        val documentUri = arguments?.getString(DOCUMENT_URI_ARGUMENT)?.toUri() ?: return

        try {
            viewModel.openRenderer(activity, documentUri)
        } catch (e: IOException) {
            Log.e(TAG, "onStart: Exception opening document", e)
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            viewModel.closeRenderer()
        } catch (ioException: IOException) {
            Log.e(TAG, "Exception closing document", ioException)
        }
    }
}