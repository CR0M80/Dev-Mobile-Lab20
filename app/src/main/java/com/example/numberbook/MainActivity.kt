package com.example.numberbook

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.numberbook.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * NumberBook Premium - Une version élégante et moderne du projet NumberBook.
 * Utilise ViewBinding, Coroutines pour le réseau, Search en temps réel avec debounce,
 * et une interface Material Design 3.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val contactAdapter = ContactAdapter()
    private var localContacts = listOf<Contact>()
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "NumberBook Premium"
    }

    private fun setupRecyclerView() {
        binding.rvContacts.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = contactAdapter
        }
    }

    private fun setupListeners() {
        // Bouton pour charger les contacts locaux
        binding.btnLoad.setOnClickListener {
            checkPermissionAndLoad()
        }

        // Bouton pour synchroniser vers le serveur
        binding.btnSync.setOnClickListener {
            syncToCloud()
        }

        // Recherche en temps réel avec "Debounce" pour éviter de saturer le serveur
        binding.etSearch.addTextChangedListener { text ->
            val query = text.toString().trim()
            searchJob?.cancel()
            if (query.isEmpty()) {
                contactAdapter.submitList(localContacts)
            } else {
                searchJob = lifecycleScope.launch {
                    delay(500) // Attend 500ms sans saisie avant de lancer la recherche
                    searchCloud(query)
                }
            }
        }
    }

    private fun checkPermissionAndLoad() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            loadLocalContacts()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            loadLocalContacts()
        } else {
            showSnackbar("Permission refusée. L'accès aux contacts est nécessaire.")
        }
    }

    private fun loadLocalContacts() {
        showLoading(true)
        lifecycleScope.launch(Dispatchers.IO) {
            val list = mutableListOf<Contact>()
            val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )

            val cursor: Cursor? = contentResolver.query(uri, projection, null, null, "${projection[0]} ASC")

            cursor?.use {
                val nameIdx = it.getColumnIndex(projection[0])
                val phoneIdx = it.getColumnIndex(projection[1])

                while (it.moveToNext()) {
                    val name = it.getString(nameIdx) ?: "Inconnu"
                    val phone = it.getString(phoneIdx) ?: ""
                    list.add(Contact(name = name, phone = phone, source = "Mobile"))
                }
            }

            withContext(Dispatchers.Main) {
                showLoading(false)
                localContacts = list
                contactAdapter.submitList(list)
                showSnackbar("${list.size} contacts chargés localement")
            }
        }
    }

    private fun syncToCloud() {
        if (localContacts.isEmpty()) {
            showSnackbar("Aucun contact à synchroniser. Chargez d'abord les contacts.")
            return
        }

        showLoading(true)
        lifecycleScope.launch(Dispatchers.IO) {
            var count = 0
            localContacts.forEach { contact ->
                try {
                    val response = RetrofitClient.instance.insertContact(contact)
                    if (response.isSuccessful && response.body()?.success == true) {
                        count++
                    }
                } catch (e: Exception) {
                    // Erreur ignorée pour continuer la boucle
                }
            }

            withContext(Dispatchers.Main) {
                showLoading(false)
                showSnackbar("Succès : $count contacts synchronisés sur le Cloud !")
            }
        }
    }

    private fun searchCloud(query: String) {
        showLoading(true)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.searchContacts(query)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        val results = response.body() ?: emptyList()
                        // On marque les résultats venant du serveur comme "Cloud"
                        contactAdapter.submitList(results.map { it.copy(source = "Cloud") })
                        if (results.isEmpty()) {
                            Toast.makeText(this@MainActivity, "Aucun résultat trouvé sur le serveur", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(this@MainActivity, "Erreur réseau : ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
