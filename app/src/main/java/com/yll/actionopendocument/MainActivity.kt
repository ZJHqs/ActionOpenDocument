package com.yll.actionopendocument

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.net.toUri

private const val TAG = "MainActivity"
private const val LAST_OPENED_URI_KEY =
    "com.example.android.actionopendocument.pref.LAST_OPENED_URI_KEY"
const val DOCUMENT_FRAGMENT_TAG = "com.example.android.actionopendocument.tags.DOCUMENT_FRAGMENT"

class MainActivity : AppCompatActivity() {

    private lateinit var noDocumentView: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        noDocumentView = findViewById(R.id.no_document_view)

        val openFileButton: Button = findViewById(R.id.open_file)
        openFileButton.setOnClickListener {
            openDocumentPicker()
        }

        getSharedPreferences(TAG, Context.MODE_PRIVATE).let { sharedPreferences ->
            if (sharedPreferences.contains(LAST_OPENED_URI_KEY)) {
                val documentUri =
                    sharedPreferences.getString(LAST_OPENED_URI_KEY, null)?.toUri() ?: return@let
                openDocument(documentUri)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_info -> {
                AlertDialog.Builder(this)
                    .setMessage(R.string.intro_message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
                return true
            }
            R.id.action_open -> {
                openDocumentPicker()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val launcherActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        it.resultCode
        it.data
    }

    private fun openDocumentPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        launcherActivity.launch(intent)
    }

    private fun openDocument(documentUri: Uri) {
        getSharedPreferences(TAG, Context.MODE_PRIVATE).edit {
            putString(LAST_OPENED_URI_KEY, documentUri.toString())
        }

//        val fragment = ActionOpenDocumentFragment.newInstance(documentUri)
        val fragment = MyFragment.newInstance(documentUri)
        supportFragmentManager.beginTransaction()
            .add(R.id.container, fragment, DOCUMENT_FRAGMENT_TAG)
            .commit()
        noDocumentView.visibility = View.GONE
    }

}