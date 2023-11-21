package com.example.pppbroom

import android.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SimpleAdapter
import com.example.pppbroom.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var mNotesDao : NoteDao
    private lateinit var executorService : ExecutorService
    private var updateId : Int = 0
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        executorService = Executors.newSingleThreadExecutor()

        val db = NoteRoomDatabase.getDatabase(this)
        mNotesDao = db!!.noteDao()!!

        with(binding){
            btnAdd.setOnClickListener {
//                if (edtTitle.text.isNotEmpty() )
                insert(Note(title = edtTitle.text.toString(), description = edtDesc.text.toString()))
                setEmptyField()
            }
            listView.setOnItemClickListener { adapterView, view, position, id ->
                val item = adapterView.adapter.getItem(position) as Note
                updateId = item.id
                edtTitle.setText(item.title)
                edtDesc.setText(item.description)

//                val notes = adapterView.adapter as SimpleAdapter
//                val item = notes.getItem(position) as Map<*, *>
//                val note = Note(
//                    title = item["title"].toString(),
//                    description = item["description"].toString()
//                )
//                updateId = note.id
//                edtTitle.setText(note.title)
//                edtDesc.setText(note.description)
            }
            btnUpdate.setOnClickListener {
//                if (edtTitle.text.isNotEmpty() )
                update(Note(id = updateId, title = edtTitle.text.toString(), description = edtDesc.text.toString()))
                updateId = 0
                setEmptyField()
            }
            listView.onItemLongClickListener =
                AdapterView.OnItemLongClickListener {
                        adapterView, view, position, id ->
                    val item = adapterView.adapter.getItem(position) as Note
                    delete(item)
                    true
                }
        }
    }

    override fun onResume() {
        super.onResume()
        getAllNotes()
    }

    private fun getAllNotes() {
        mNotesDao.allNotes.observe(this) {
            notes ->
            val adapter : ArrayAdapter<Note> = ArrayAdapter<Note>(this, android.R.layout.simple_list_item_1, notes)
            binding.listView.adapter = adapter
        }

//        mNotesDao.allNotes.observe(this) { notes ->
//            val noteList = notes.map {
//                mapOf("title" to it.title, "description" to it.description)
//            }
//
//            val adapter = SimpleAdapter(
//                this,
//                noteList,
//                R.layout.simple_list_item_2,
//                arrayOf("title", "description"),
//                intArrayOf(R.id.text1, R.id.text2)
//            )
//
//            binding.listView.adapter = adapter
//        }
    }

    private fun insert(note: Note) {
        executorService.execute { mNotesDao.insert(note) }
    }
    private fun update(note: Note) {
        executorService.execute { mNotesDao.update(note) }
    }
    private fun delete(note: Note) {
        executorService.execute { mNotesDao.delete(note) }
    }

    fun setEmptyField() {
        with(binding) {
            edtTitle.setText("")
            edtDesc.setText("")
        }
    }

}