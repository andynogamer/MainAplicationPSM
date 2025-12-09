package com.example.mainaplicationpsm.view

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainaplicationpsm.R
import com.example.mainaplicationpsm.adapter.DraftAdapter
import com.example.mainaplicationpsm.model.local.AppDatabase
import com.example.mainaplicationpsm.model.local.Draft
import kotlinx.coroutines.launch

class DraftsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DraftAdapter
    private lateinit var tvEmpty: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Reutilizamos el layout de Favorites porque es idéntico (Lista + Header)
        // O creamos uno nuevo si prefieres (ver análisis abajo)
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ajustar el título del header (ya que reciclamos el layout de favoritos)
        val tvTitle = view.findViewById<TextView>(R.id.textViewHeader) // Asegúrate que el ID sea correcto en fragment_favorites o usa uno nuevo
        // Si usas fragment_favorites.xml, el TextView seguro no tiene ID o es estático.
        // RECOMENDACIÓN: Usa un layout nuevo 'fragment_drafts.xml' (ver abajo).

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        recyclerView = view.findViewById(R.id.recyclerFavorites) // Reutilizando ID
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Cargar borradores
        loadDrafts()
    }

    // Usamos onResume para recargar la lista si volvemos de publicar el post
    override fun onResume() {
        super.onResume()
        loadDrafts()
    }

    private fun loadDrafts() {
        lifecycleScope.launch {
            val drafts = AppDatabase.getDatabase(requireContext()).draftDao().getAllDrafts()

            if (drafts.isEmpty()) {
                Toast.makeText(context, "No tienes borradores", Toast.LENGTH_SHORT).show()
            }

            adapter = DraftAdapter(drafts,
                onDraftClick = { draft ->
                    openDraft(draft)
                },
                onDeleteClick = { draft ->
                    confirmDelete(draft)
                }
            )
            recyclerView.adapter = adapter
        }
    }

    private fun openDraft(draft: Draft) {
        // Llamamos al constructor especial que creamos en NewPostFragment
        val fragment = NewPostFragment.newInstanceFromDraft(draft)

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun confirmDelete(draft: Draft) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Borrador")
            .setMessage("¿Estás seguro de eliminar este borrador permanentemente?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteDraft(draft)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteDraft(draft: Draft) {
        lifecycleScope.launch {
            AppDatabase.getDatabase(requireContext()).draftDao().deleteDraft(draft)
            loadDrafts() // Recargar lista
            Toast.makeText(context, "Borrador eliminado", Toast.LENGTH_SHORT).show()
        }
    }
}