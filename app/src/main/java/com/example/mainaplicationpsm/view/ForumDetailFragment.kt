package com.example.mainaplicationpsm

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainaplicationpsm.adapter.PostAdapter
import com.example.mainaplicationpsm.network.RetrofitClient
import com.example.mainaplicationpsm.view.NewPostFragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class ForumDetailFragment : Fragment() {

    private var forumId: Int = -1
    private var forumName: String? = null
    private var forumDesc: String? = null
    private var forumBanner: String? = null
    private var forumMembers: Int = 0 // NUEVO: Variable para miembros



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

            forumId = it.getInt(ARG_FORUM_ID, -1)
            forumName = it.getString(ARG_FORUM_NAME)
            forumDesc = it.getString(ARG_FORUM_DESC)
            forumBanner = it.getString(ARG_FORUM_BANNER)
            forumMembers = it.getInt(ARG_FORUM_MEMBERS) // NUEVO: Leer argumento
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forum_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val collapsingToolbar = view.findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)
        val ivHeader = view.findViewById<ImageView>(R.id.ivDetailForumBanner)
        val tvDesc = view.findViewById<TextView>(R.id.tvForumDescription)
        val tvMembers = view.findViewById<TextView>(R.id.tvMemberCount) // Referencia al texto de miembros
        val recyclerPosts = view.findViewById<RecyclerView>(R.id.recyclerForumPosts)

        // Configurar datos
        collapsingToolbar.title = forumName
        tvDesc.text = forumDesc
        tvMembers.text = "$forumMembers Miembros" // NUEVO: Mostrar el número real

        // Cargar Banner
        if (!forumBanner.isNullOrEmpty()) {
            try {
                val cleanBase64 = forumBanner!!.substringAfter(",")
                val decodedString = Base64.decode(cleanBase64, Base64.DEFAULT)
                val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                ivHeader.setImageBitmap(decodedByte)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            ivHeader.setImageResource(R.drawable.search_bar_backgorun)
        }

        recyclerPosts.layoutManager = LinearLayoutManager(requireContext())
        fetchForumPosts(recyclerPosts)

        val fabAddPost: FloatingActionButton = view.findViewById(R.id.fabAddPost)
        fabAddPost.setOnClickListener {
            // CORRECCIÓN: Usamos newInstance pasando el ID del foro actual
            val newPostFragment = NewPostFragment.newInstance(forumId)

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, newPostFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun fetchForumPosts(recyclerView: RecyclerView) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getPosts()
                if (response.isSuccessful) {
                    val posts = response.body()?.posts ?: emptyList()
                    recyclerView.adapter = PostAdapter(posts)
                } else {
                    Log.e("API", "Error al cargar posts: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API", "Error de conexión: ${e.message}")
            }
        }
    }

    companion object {

        private const val ARG_FORUM_ID = "forum_id"
        private const val ARG_FORUM_NAME = "forum_name"
        private const val ARG_FORUM_DESC = "forum_desc"
        private const val ARG_FORUM_BANNER = "forum_banner"
        private const val ARG_FORUM_MEMBERS = "forum_members" // NUEVO ID

        @JvmStatic
        fun newInstance(id: Int, name: String, description: String, banner: String?, members: Int?) =
            ForumDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_FORUM_ID, id) // GUARDAR ID
                    putString(ARG_FORUM_NAME, name)
                    putString(ARG_FORUM_DESC, description)
                    putString(ARG_FORUM_BANNER, banner)
                    putInt(ARG_FORUM_MEMBERS, members ?: 0)// Guardar entero
                }
            }
    }
}