package com.example.mainaplicationpsm.view

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainaplicationpsm.R
import com.example.mainaplicationpsm.adapter.DraftAdapter
import com.example.mainaplicationpsm.model.local.AppDatabase
import com.example.mainaplicationpsm.model.local.Draft
import com.example.mainaplicationpsm.utils.SessionManager
import kotlinx.coroutines.launch

class DraftsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DraftAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // CORRECCIÓN: Usamos el layout específico de borradores
        return inflater.inflate(R.layout.fragment_drafts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inicializar SessionManager
        sessionManager = SessionManager(requireContext())

        // 2. Configurar botón atrás
        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 3. Configurar RecyclerView
        // Asegúrate de que en fragment_drafts.xml el ID sea recyclerDrafts
        recyclerView = view.findViewById(R.id.recyclerDrafts)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 4. Cargar datos
        loadDrafts()
    }

    // Usamos onResume para recargar la lista si volvemos de editar/publicar
    override fun onResume() {
        super.onResume()
        loadDrafts()
    }

    private fun loadDrafts() {
        // Obtener el ID del usuario actual
        val currentUserId = sessionManager.fetchUserId()

        // Seguridad: Si no hay usuario logueado, no cargamos nada
        if (currentUserId == -1) return

        lifecycleScope.launch {
            // CORRECCIÓN: Pasamos el currentUserId al DAO para filtrar
            val drafts = AppDatabase.getDatabase(requireContext())
                .draftDao()
                .getAllDrafts(currentUserId)

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