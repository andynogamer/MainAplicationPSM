package com.example.mainaplicationpsm

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainaplicationpsm.adapter.PostAdapter
import com.example.mainaplicationpsm.model.PostProvider
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.mainaplicationpsm.view.NewPostFragment

class ForumDetailFragment : Fragment() {

    private var forumName: String? = null
    private var forumDesc: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            forumName = it.getString(ARG_FORUM_NAME)
            forumDesc = it.getString(ARG_FORUM_DESC)
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


        val collapsingToolbarLayout: CollapsingToolbarLayout = view.findViewById(R.id.collapsing_toolbar)
        collapsingToolbarLayout.title = forumName // Establece el título del foro aquí

        val tvDesc: TextView = view.findViewById(R.id.tvForumDescription)
        val ivMemberIcon: ImageView = view.findViewById(R.id.ivMemberIcon)
        val tvMemberCount: TextView = view.findViewById(R.id.tvMemberCount)
        val recyclerPosts: RecyclerView = view.findViewById(R.id.recyclerForumPosts)

        tvDesc.text = forumDesc
        tvMemberCount.text = "1,234 Miembros"


        //tvDesc.translationY = -70f // Mueve la descripción hacia arriba
        //ivMemberIcon.translationY = -70f // Mueve el icono de miembros hacia arriba
        //tvMemberCount.translationY = -70f // Mueve el contador de miembros hacia arriba


        recyclerPosts.layoutManager = LinearLayoutManager(requireContext())
        recyclerPosts.adapter = PostAdapter(PostProvider.postList)


        // val ivForumBanner: ImageView = view.findViewById(R.id.ivForumBanner)
        // Glide.with(requireContext()).load("URL_DEL_BANNER_DEL_FORO").into(ivForumBanner)
        val fabAddPost: FloatingActionButton = view.findViewById(R.id.fabAddPost)


        fabAddPost.setOnClickListener {
            // 4. Crea la instancia del nuevo fragmento
            val newPostFragment = NewPostFragment() //

            // 5. Reemplaza el fragmento actual por el de "NewPostFragment"
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, newPostFragment) //
                .addToBackStack(null) // Para que el usuario pueda volver atrás
                .commit()
        }
    }

    companion object {
        private const val ARG_FORUM_NAME = "forum_name"
        private const val ARG_FORUM_DESC = "forum_desc"

        @JvmStatic
        fun newInstance(name: String, description: String) =
            ForumDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_FORUM_NAME, name)
                    putString(ARG_FORUM_DESC, description)
                }
            }
    }
}